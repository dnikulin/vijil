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
import java.util.Collections;
import java.util.HashMap;

import com.dnikulin.vijil.index.MatchVisitor;
import com.dnikulin.vijil.model.TextModel;

final class SpanTextBuffer {
    /** Special value for SpanText.text2, indicating a black-hole span. */
    public static final String blackHash = "blackhole";

    /** Special value for SpanText.meta2, indicating a black-hole span. */
    public static final byte[] blackMeta = "[\"blackhole\",[]]".getBytes();

    /** Maximum number of texts to match a lemma position. */
    public static final byte maxTexts = 5;

    /** Minimum number of lemmas to keep a span. */
    public static final int minLemmas = 5;

    public final TextModel text;

    final ArrayList<SpanText> spans;
    final byte[] counts;
    boolean dirty;

    SpanTextBuffer(TextModel text) {
        this.text = text;
        this.spans = new ArrayList<SpanText>();
        this.counts = new byte[text.size];
        this.dirty = false;
    }

    void matched(int offset, int length, TextModel text2) {
        SpanText span = new SpanText(text2.hash, text2.meta(), offset, offset + length);
        spans.add(span);
        dirty = true;
    }

    void merge(SpanTextBuffer that) {
        if (that.spans.isEmpty() == false) {
            spans.addAll(that.spans);
            dirty = true;
        }
    }

    void compact(ArrayList<SpanText> nspans) {
        if (dirty == false)
            return;

        nspans.clear();
        Collections.sort(spans);

        int i = 0;
        while (i < spans.size()) {
            final SpanText span1 = spans.get(i);
            int min = span1.min;
            int max = span1.max;
            assert (max > min);

            i++;
            while (i < spans.size()) {
                final SpanText span2 = spans.get(i);
                assert (span2 != span1);

                assert (span2.text2.compareTo(span1.text2) >= 0);

                if (span2.text2.equals(span1.text2) == false)
                    break;

                assert (span2.max > span2.min);
                assert (span2.min >= min);

                if (span2.min > max)
                    break;

                min = Math.min(min, span2.min);
                max = Math.max(max, span2.max);
                assert (max > min);

                i++;
            }

            assert (min == span1.min);

            if (max > span1.max) {
                // Span was extended, create new object.
                SpanText nspan = new SpanText(span1.text2, span1.meta2(), min, max);
                nspans.add(nspan);
            } else {
                // Span has not changed, keep old object.
                nspans.add(span1);
            }
        }

        spans.clear();
        spans.addAll(nspans);
        nspans.clear();

        countTexts();
        punchHoles(nspans);

        dirty = false;
    }

    void countTexts() {
        // Reset counts.
        for (int i = 0; i < counts.length; i++)
            counts[i] = (byte) 0;

        for (SpanText span : spans) {
            if (span.text2 == blackHash) {
                // The span is already a black-hole, count it as many texts.
                // Assign instead of adding to avoid overflow.
                for (int i = span.min; i < span.max; i++)
                    counts[i] = (maxTexts + 1);
            } else {
                // The span is not a black-hole, count it as a single text.
                for (int i = span.min; i < span.max; i++) {
                    // Check total count to avoid overflow.
                    if (counts[i] <= maxTexts)
                        counts[i]++;
                }
            }
        }
    }

    void punchHoles(ArrayList<SpanText> nspans) {
        nspans.clear();

        for (SpanText span : spans) {
            // Drop existing black-hole spans.
            // They will be re-created from counts anyway.
            if (span.text2 == blackHash)
                continue;

            boolean good = false;
            int last = 0;

            for (int i = span.min; i <= span.max; i++) {
                final boolean isBlack = ((i >= span.max) || (counts[i] > maxTexts));

                if (isBlack == false) {
                    if (good == false) {
                        good = true;
                        last = i;
                    }
                } else if (good == true) {
                    good = false;

                    if ((i >= span.max) && (last == span.min)) {
                        // The span was not split by black-holes,
                        // so keep the original span object.
                        nspans.add(span);
                    } else if ((i - last) >= minLemmas) {
                        // The span has been split, but this part is
                        // long enough, so make a new span object.
                        nspans.add(new SpanText(span.text2, span.meta2(), last, i));
                    }
                }
            }
        }

        spans.clear();
        spans.addAll(nspans);
        nspans.clear();

        for (int i = 0; i < counts.length; i++) {
            // Ignore non-black counts.
            if (counts[i] <= maxTexts)
                continue;

            // Note start of black-hole.
            final int min = i;

            // Find end of black-hole.
            int max = i + 1;
            while ((max < counts.length) && (counts[max] > maxTexts))
                max++;

            // Start next search from the end of this black-hole.
            i = max - 1;

            // Record the black-hole as a special span.
            spans.add(new SpanText(blackHash, blackMeta, min, max));

            // Assert no loss of lemmas.
            for (int j = min; j < max; j++)
                assert (counts[j] > maxTexts);
        }
    }
}

public final class SpanTextMatches implements MatchVisitor {
    private final static SpanText[] noSpans = new SpanText[0];

    private final HashMap<String, SpanTextBuffer> textSpans;
    private final ArrayList<SpanText> nspans;

    public SpanTextMatches() {
        this.textSpans = new HashMap<String, SpanTextBuffer>();
        this.nspans = new ArrayList<SpanText>();
    }

    @Override
    public void matched(TextModel text1, TextModel text2, int offset1, int offset2, int length1, int length2) {
        if (text1.hash.equals(text2.hash))
            return;

        assert (offset1 >= 0);
        assert (offset2 >= 0);
        assert (length1 >= 1);
        assert (length2 >= 1);
        assert ((offset1 + length1) <= text1.size);
        assert ((offset2 + length2) <= text2.size);

        SpanTextBuffer buffer = bufferFor(text1);
        buffer.matched(offset1, length1, text2);
    }

    public void merge(SpanTextMatches that) {
        for (SpanTextBuffer buffer2 : that.textSpans.values()) {
            SpanTextBuffer buffer1 = this.bufferFor(buffer2.text);
            buffer1.merge(buffer2);
            buffer1.compact(nspans);
        }
    }

    public void compact() {
        for (SpanTextBuffer spans : textSpans.values())
            spans.compact(nspans);
    }

    SpanTextBuffer bufferFor(TextModel text) {
        SpanTextBuffer spans = textSpans.get(text.hash);
        if (spans == null) {
            spans = new SpanTextBuffer(text);
            textSpans.put(text.hash, spans);
        }
        return spans;
    }

    public String[] queryHashes() {
        return textSpans.keySet().toArray(new String[0]);
    }

    public SpanText[] spansFor(String hash) {
        SpanTextBuffer buffer = textSpans.get(hash);
        if (buffer == null)
            return noSpans;
        return buffer.spans.toArray(noSpans);
    }
}
