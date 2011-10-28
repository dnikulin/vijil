// Copyright (C) 2011  Dmitri Nikulin
//
// This file is part of Vijil.
//
// Vijil is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Vijil is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Vijil.  If not, see <http://www.gnu.org/licenses/>.
//
// Repository:     https://github.com/dnikulin/vijil
// Email:          dnikulin+vijil@gmail.com

package com.dnikulin.vijil.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.dnikulin.vijil.index.MatchVisitor;
import com.dnikulin.vijil.model.TextModel;

final class GraphSet {
    public final int                code;
    public final SpanDomain         domain;
    public final HashSet<GraphSpan> spans;

    public GraphSet(int code, SpanDomain domain) {
        this.code    = code;
        this.domain  = domain;
        this.spans   = new HashSet<GraphSpan>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if ((obj instanceof GraphSet) == false) return false;
        final GraphSet that = (GraphSet) obj;
        return (this.code == that.code);
    }

    @Override
    public int hashCode() {
        return code;
    }
}

final class GraphSpan {
    public final String   hash;
    public final int      code;

    public       GraphSet set;
    public       int      min;
    public       int      max;

    private final HashSet<GraphSpan> links;

    private LinkSpan result;

    public GraphSpan(String hash, int code, GraphSet set, int min, int max) {
        this.hash = hash;
        this.code = code;

        this.set  = set;
        this.min  = min;
        this.max  = max;

        this.links  = new HashSet<GraphSpan>();
        this.result = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if ((obj instanceof GraphSpan) == false) return false;
        final GraphSpan that = (GraphSpan) obj;
        return (this.code == that.code);
    }

    @Override
    public int hashCode() {
        return code;
    }

    public Iterable<GraphSpan> links() {
        return links;
    }

    public void link(GraphSpan that) {
        result = null;
        links.add(that);
    }

    public boolean linked(GraphSpan that) {
        return links.contains(that);
    }

    public void unlink(GraphSpan that) {
        result = null;
        links.remove(that);
    }

    public LinkSpan result(LinkSpanSet dset, int dcode) {
        if (result == null) {
            LinkSpan [] dlinks = new LinkSpan[links.size()];
            result = new LinkSpan(dset, dcode, hash, min, max, dlinks);
        }
        return result;
    }

    public LinkSpan result() {
        assert (result != null);
        return result;
    }
}

final class GraphView {
    private final String               hash;
    private final SpanDomain           domain;
    private final ArrayList<GraphSpan> buffer;
    private final HashSet<GraphSet>    sets;
    private final int []               codes;

    // Keyed by indices in the span domain.
    private       GraphSpan []         spans;

    public GraphView(String hash, SpanDomain domain, ArrayList<GraphSpan> buffer, HashSet<GraphSet> sets, int[] codes) {
        this.hash    = hash;
        this.domain  = domain;
        this.buffer  = buffer;
        this.sets    = sets;
        this.codes   = codes;

        this.spans   = new GraphSpan[2048];
    }

    public GraphSpan makeSpan(int min, int max, GraphSpan span0) {
        buffer.clear();

        GraphSpan span1 = null;
        int       min1  = min;
        int       max1  = max;

        // Expand to new max if necessary.
        expand(max1);

        // Take final reference to own span array, to help JIT.
        final GraphSpan [] myspans = spans;

        // Find all spans overlapping this new span.
        for (int i = min; i < max; i++) {
            GraphSpan span2 = myspans[i];

            // Ignore un-spanned positions.
            if (span2 == null)
                continue;

            // Ensure no repeated spans due to skip.
            assert (span2 != span1);
            if (span1 != null)
                assert (span2.min >= span1.max);

            // Check the domain.
            assert (span2.set.domain == domain);

            // Record this as the latest span.
            // It may be expanded later.
            span1 = span2;

            // Record the span as overlapping the new span.
            buffer.add(span1);

            // Record known extents.
            // These may be beyond the new span.
            min1 = Math.min(span1.min, min1);
            max1 = Math.max(span1.max, max1);

            // Skip ahead to known extent.
            i = span1.max - 1;
        }

        // Start with no set.
        GraphSet set = null;

        // If a span was given, use its set.
        if (span0 != null) {
            set = span0.set;
            assert (set.domain == domain);
        }

        if (buffer.isEmpty()) {
            // No spans here, create a new graph span for the set.
            final int code = codes[0]++;

            if (set == null) {
                // No set was given, create a new one.
                // It is safe to share the code for the set and span.
                set = new GraphSet(code, domain);
            }

            // Create new span.
            span1 = new GraphSpan(hash, code, set, min1, max1);
            set.spans.add(span1);
        }

        // Make latest span all-inclusive.
        span1.min = min1;
        span1.max = max1;

        // Create symmetric link to designated linked span, if any.
        if (span0 != null) {
            span1.link(span0);
            span0.link(span1);
        }

        if (set == null) {
            // No set was given, use the latest span's one.
            set = span1.set;
            assert (set.domain == domain);
        }

        // Clean and combine graph sets.
        for (GraphSpan span2 : buffer) {
            // Reference set from old span.
            GraphSet set2 = span2.set;
            assert (set2.domain == domain);
            span2.set = set;

            // If sets are separate, combine them.
            if (set != set2) {
                // Add all set2 spans to set1.
                for (GraphSpan span3 : set2.spans) {
                    assert ((span3.set == set) || (span3.set == set2));
                    assert (span3.set.domain == domain);
                    set.spans.add(span3);
                    span3.set = set;
                }

                // Remove set2 from the set-set.
                sets.remove(set2);
            }

            set.spans.remove(span2);
        }

        // Consume links from all absorbed spans.
        for (GraphSpan span2 : buffer) {
            // Verify set identity.
            assert (span2.set == set);
            assert (span2.set.domain == domain);

            // Ignore the latest span.
            if (span2 == span1)
                continue;

            for (GraphSpan span3 : span2.links()) {
                // Verify set identity.
                assert (span3.set == set);
                assert (span3.set.domain == domain);

                // Verify link symmetry.
                assert (span3.linked(span2));

                // Remove old link.
                span3.unlink(span2);

                // Create symmetric link to latest span.
                span1.link(span3);
                span3.link(span1);
            }
        }

        // Verify link symmetry.
        for (GraphSpan span2 : span1.links())
            assert (span2.linked(span1));

        // Mark text positions for this span.
        for (int i = span1.min; i < span1.max; i++)
            myspans[i] = span1;

        // Ensure the span is in the set.
        set.spans.add(span1);
        span1.set = set;

        // Ensure the set is in the set-set.
        sets.add(set);

        buffer.clear();
        return span1;
    }

    private void expand(int max) {
        if (max > spans.length) {
            // Grow array size by powers of 2.
            int nmax = spans.length;
            while (max > nmax)
                nmax *= 2;

            // Extend array.
            spans = Arrays.copyOf(spans, nmax);
        }
    }
}

public final class LinkSpanGraph implements MatchVisitor {
    public  final SpanDomain                 domain;

    private final HashSet<GraphSet>          sets;
    private final ArrayList<GraphSpan>       buffer;
    private final HashMap<String, GraphView> views;
    private final int []                     codes;

    public LinkSpanGraph(SpanDomain domain) {
        this.domain  = domain;

        this.sets    = new HashSet<GraphSet>();
        this.buffer  = new ArrayList<GraphSpan>();
        this.views   = new HashMap<String, GraphView>();
        this.codes   = new int[1];
    }

    private GraphView makeView(String hash) {
        GraphView view = views.get(hash);
        if (view == null) {
            view = new GraphView(hash, domain, buffer, sets, codes);
            views.put(hash, view);
        }
        return view;
    }

    public void include(LinkSpan lspan1) {
        assert (lspan1.set.domain == domain);

        final GraphView view1 = makeView(lspan1.hash);
        final GraphSpan span1 = view1.makeSpan(lspan1.min, lspan1.max, null);

        for (LinkSpan lspan2 : lspan1.links) {
            assert (lspan2.set == lspan1.set);

            final GraphView view2 = makeView(lspan2.hash);
            view2.makeSpan(lspan2.min, lspan2.max, span1);
        }
    }

    public void include(LinkSpan[] lspans) {
        for (LinkSpan span : lspans)
            include(span);
    }

    public void include(LinkSpanSet[] sets) {
        for (LinkSpanSet set : sets)
            include(set.spans);
    }

    public void normalise(LinkSpanSet[] sets, TextModel[] models) {
        // Only usable in character domain.
        assert (domain == SpanDomain.CHARACTERS);

        for (LinkSpanSet set : sets) {
            // Only usable for symbol spans.
            assert (set.domain == SpanDomain.SYMBOLS);

            for (LinkSpan lspan1 : set.spans) {
                // Find matching text model.
                for (TextModel model1 : models) {
                    if (model1.hash.equals(lspan1.hash)) {
                        final int cmin1 = model1.minChar(lspan1.min);
                        final int cmax1 = model1.maxChar(lspan1.max - 1);

                        // Create view and linked span.
                        final GraphView view1 = makeView(lspan1.hash);
                        final GraphSpan span1 = view1.makeSpan(cmin1, cmax1, null);

                        for (LinkSpan lspan2 : lspan1.links) {
                            assert (lspan2.set == lspan1.set);
                            assert (lspan2.hash.equals(lspan1.hash) == false);

                            // Find matching text model.
                            for (TextModel model2 : models) {
                                if (model2.hash.equals(lspan2.hash)) {
                                    final int cmin2 = model2.minChar(lspan2.min);
                                    final int cmax2 = model2.maxChar(lspan2.max - 1);

                                    // Create view and linked span.
                                    final GraphView view2 = makeView(lspan2.hash);
                                    view2.makeSpan(cmin2, cmax2, span1);

                                    // Stop searching for model 2.
                                    break;
                                }
                            }
                        }

                        // Stop searching for model 1.
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void matched(TextModel text1, TextModel text2, int min1, int min2, int len1, int len2) {
        // Only usable in symbol domain.
        assert (domain == SpanDomain.SYMBOLS);

        // Calculate end of each matching span.
        final int max1 = min1 + len1;
        final int max2 = min2 + len2;

        // Verify parameters.
        assert (text1 != null);
        assert (text2 != null);
        assert (min1  >= 0);
        assert (min2  >= 0);
        assert (len1  >= 1);
        assert (len2  >= 1);
        assert (max1  <= text1.size);
        assert (max2  <= text2.size);

        // Make or get views for given texts.
        final GraphView view1 = makeView(text1.hash);
        final GraphView view2 = makeView(text2.hash);

        // Make or get graph spans for given data spans.
        final GraphSpan span1 = view1.makeSpan(min1, max1, null);
        final GraphSpan span2 = view2.makeSpan(min2, max2, span1);

        // Refer directly to graph sets for the spans.
        final GraphSet  set1  = span1.set;
        final GraphSet  set2  = span2.set;

        // Verify both sets are the same.
        assert (set1 == set2);

        // Verify the set-set has the set.
        assert (sets.contains(set1));

        // Verify link symmetry.
        assert (span1.linked(span2));
        assert (span2.linked(span1));
    }

    public LinkSpanSet[] result() {
        LinkSpanSet [] out = new LinkSpanSet[sets.size()];
        int iout = 0;

        int setCode  = 0;
        int spanCode = 0;

        for (GraphSet set : sets) {
            final int nspans = set.spans.size();

            // Create set that each node will reference.
            LinkSpanSet dset = new LinkSpanSet(++setCode, nspans, domain);

            // Create each data span.
            int iout2 = 0;
            for (GraphSpan span : set.spans) {
                // Create data span with set but null links.
                final LinkSpan lspan = span.result(dset, ++spanCode);
                dset.spans[iout2++] = lspan;
            }

            iout2 = 0;
            for (GraphSpan span : set.spans) {
                // Populate links.
                final LinkSpan lspan = dset.spans[iout2++];
                int ilink = 0;
                for (GraphSpan link : span.links())
                    lspan.links[ilink++] = link.result();
            }

            out[iout++] = dset;
        }

        return out;
    }

    public static LinkSpanSet[] merge(LinkSpanSet[] sets) {
        if (sets.length < 1)
            return LinkSpanSet.none;

        LinkSpanGraph graph = new LinkSpanGraph(sets[0].domain);
        graph.include(sets);
        return graph.result();
    }

    public static LinkSpanSet[] normaliseSets(LinkSpanSet[] sets, TextModel[] models) {
        if (sets.length < 1)
            return LinkSpanSet.none;

        LinkSpanGraph graph = new LinkSpanGraph(SpanDomain.CHARACTERS);
        graph.normalise(sets, models);
        return graph.result();
    }
}
