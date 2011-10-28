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
import java.util.HashMap;

import com.dnikulin.vijil.index.MatchVisitor;
import com.dnikulin.vijil.model.TextModel;

final class ViewToTexts {
    protected final String         hash1;
    protected final SpanToTexts [] spans;

    public ViewToTexts(String hash1, int nlemmas) {
        this.hash1 = hash1;
        this.spans = new SpanToTexts[nlemmas];
    }

    public SpanToTexts makeSpan(ArrayList<SpanToTexts> buffer, int[] codes, int min, int max) {
        buffer.clear();

        SpanToTexts span1 = null;
        int         min1  = min;
        int         max1  = max;

        // Find all spans overlapping this new span.
        for (int i = min; i < max; i++) {
            SpanToTexts span2 = spans[i];

            // Ignore un-spanned positions.
            if (span2 == null)
                continue;

            // Ensure no repeated spans due to skip.
            assert (span2 != span1);

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

        if (buffer.isEmpty()) {
            // No spans here, create a new span.
            final int code = codes[0]++;
            span1 = new SpanToTexts(code, hash1, spans.length, min1, max1);
        }

        // Make latest span all-inclusive.
        span1.min = min1;
        span1.max = max1;

        // Consume text2s from all other spans.
        for (SpanToTexts span2 : buffer) {
            if (span1 != span2)
                span1.hashes.addAll(span2.hashes);
        }

        // Mark text positions for this span.
        for (int i = span1.min; i < span1.max; i++)
            spans[i] = span1;

        buffer.clear();
        return span1;
    }
}

public final class ModelSpanTexts implements MatchVisitor {
    private final ArrayList<SpanToTexts>       spans;
    private final HashMap<String, ViewToTexts> views;
    private final int []                       codes;

    public ModelSpanTexts() {
        this.spans = new ArrayList<SpanToTexts>();
        this.views = new HashMap<String, ViewToTexts>();
        this.codes = new int[1];
    }

    private ViewToTexts makeView(String hash1, int nlemmas) {
        ViewToTexts view = views.get(hash1);
        if (view == null) {
            view = new ViewToTexts(hash1, nlemmas);
            views.put(hash1, view);
        }
        return view;
    }

    public void include(SpanToTexts[] spans) {
        for (SpanToTexts span : spans)
            include(span);
    }

    public void include(SpanToTexts span) {
        // Make or get view for text1.
        final ViewToTexts view1 = makeView(span.hash, span.nlemmas);

        // Connect span for view1.
        SpanToTexts span2 = view1.makeSpan(spans, codes, span.min, span.max);

        // Record hashes in span hash set.
        span2.hashes.addAll(span.hashes);
    }

    public void matched(TextModel text1, int min1, int max1, String text2) {
        // Verify parameters.
        assert (text1        != null);
        assert (text2        != null);
        assert (text1.hash.equals(text2) == false);
        assert (min1 >= 0);
        assert (max1 >  min1);
        assert (max1 <= text1.size);

        // Make or get view for text1.
        final ViewToTexts view1 = makeView(text1.hash, text1.size);

        // Connect span for view1.
        final SpanToTexts span1 = view1.makeSpan(spans, codes, min1, max1);

        // Record hash in span hash set.
        span1.hashes.add(text2);
    }

    @Override
    public void matched(TextModel text1, TextModel text2, int min1, int min2, int len1, int len2) {
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

        matched(text1, min1, max1, text2.hash);
    }

    public HashMap<String, SpanToTexts[]> result() {
        final HashMap<String, SpanToTexts[]> map = new HashMap<String, SpanToTexts[]>();

        for (ViewToTexts view : views.values()) {
            final ArrayList<SpanToTexts> out = new ArrayList<SpanToTexts>();

            int i = 0;
            while (i < view.spans.length) {
                SpanToTexts span = view.spans[i];

                // Skip null spans.
                if (span == null) {
                    i++;
                    continue;
                }

                // Record and skip past real spans.
                out.add(span);
                i = span.max;
            }

            map.put(view.hash1, out.toArray(SpanToTexts.none));
        }

        return map;
    }

    public static HashMap<String, SpanToTexts[]> merge(SpanToTexts[][] spanss) {
        ModelSpanTexts spanner = new ModelSpanTexts();
        for (SpanToTexts[] spans : spanss)
            spanner.include(spans);
        return spanner.result();
    }
}
