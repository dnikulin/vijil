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

import java.util.Arrays;

import com.dnikulin.vijil.index.MatchVisitor;
import com.dnikulin.vijil.model.TextModel;

public final class SpanMerger implements MatchVisitor {
    private static final int startSize =  512;
    private static final int cleanSize = 8192;

    public final TextModel text1;
    public final TextModel text2;

    private int [] off1s;
    private int [] off2s;
    private int [] len1s;
    private int [] len2s;
    private int    npairs;
    private int    nclean;

    public SpanMerger(TextModel text1, TextModel text2) {
        assert(text1 !=  null);
        assert(text2 !=  null);
        assert(text1 != text2);

        this.text1 = text1;
        this.text2 = text2;

        this.off1s  = new int [startSize];
        this.off2s  = new int [startSize];
        this.len1s  = new int [startSize];
        this.len2s  = new int [startSize];
        this.npairs = 0;
        this.nclean = 0;
    }

    public synchronized void clear() {
        // Reset number of pairs.
        npairs = 0;
        nclean = 0;
    }

    public synchronized int count() {
        return npairs;
    }

    public synchronized void cleanup() {
        // Verify internal consistency.
        assert (nclean >= 0);
        assert (npairs >= nclean);
        assert (npairs <= off1s.length);

        // Check that cleanup is actually necessary.
        if (npairs <= nclean)
            return;

        // Sort all pairs.
        quicksort(0, npairs - 1);

        // Verify the sort.
        for (int i = 1; i < npairs; i++)
            assert (more(i - 1, i) == false);

        // Keep track of pairs already cleared.
        boolean[] clear = new boolean[npairs];

        // Merge pairs going forward.
        int o = 0;

        for (int i = 0; i < npairs; i++) {
            // Ignore pairs already cleared.
            if (clear[i] == true)
                continue;

            // Note the limit of the spans.
            int min1 = off1s[i];
            int min2 = off2s[i];
            int max1 = (min1 + len1s[i]);
            int max2 = (min2 + len2s[i]);
            clear[i] = true;

            // Find matching pairs past this pair.
            for (int j = i + 1; j < npairs; j++) {
                // Ignore pairs already cleared.
                if (clear[j] == true)
                    continue;

                // Note the limit of the new spans.
                final int min1j = off1s[j];
                final int min2j = off2s[j];
                final int max1j = (min1j + len1s[j]);
                final int max2j = (min2j + len2s[j]);

                // Check that the entry is from the same span pair.
                assert (min1j >= min1);
                if (max1j < min1) break;
                if (max2j < min2) continue;
                if (min1j > max1) break;
                if (min2j > max2) continue;

                // Consume this pair, extending current spans.
                min1 = Math.min(min1, min1j);
                min2 = Math.min(min2, min2j);
                max1 = Math.max(max1, max1j);
                max2 = Math.max(max2, max2j);
                clear[j] = true;
            }

            // Update first pair with expanded ranges.
            off1s[o] = min1;
            off2s[o] = min2;
            len1s[o] = (max1 - min1);
            len2s[o] = (max2 - min2);
            o++;
        }

        // Verify that all pairs were cleared.
        for (int i = 0; i < npairs; i++)
            assert (clear[i] == true);

        // Truncate arrays post-merge.
        npairs = o;
        nclean = o;
    }

    public synchronized void replay(MatchVisitor each) {
        // Verify internal consistency.
        assert (nclean >= 0);
        assert (npairs >= nclean);
        assert (npairs <= off1s.length);

        for (int i = 0; i < npairs; i++) {
            each.matched(
                text1    , text2    ,
                off1s [i], off2s [i],
                len1s [i], len2s [i]
            );
        }
    }

    @Override
    public synchronized void matched(TextModel text1, TextModel text2, int offset1, int offset2, int length1, int length2) {
        // Verify internal consistency.
        assert (nclean >= 0);
        assert (npairs >= nclean);
        assert (npairs <= off1s.length);

        // Verify parameters.
        assert (text1   == this.text1);
        assert (text2   == this.text2);
        assert (offset1 >= 0);
        assert (offset2 >= 0);
        assert (length1 >= 1);
        assert (length2 >= 1);
        assert ((offset1 + length1) <= text1.size);
        assert ((offset2 + length2) <= text2.size);

        // Ignore matches within the same text.
        if (text1.hash.equals(text2.hash))
            return;

        // Cleanup if many matches added without clean.
        if ((npairs - nclean) >= cleanSize) {
            cleanup();
            assert (npairs == nclean);
        }

        // Grow arrays if necessary.
        grow();

        // Record in arrays.
        off1s [npairs] = offset1;
        off2s [npairs] = offset2;
        len1s [npairs] = length1;
        len2s [npairs] = length2;

        // Advance cursor.
        npairs++;
        assert (npairs > nclean);
    }

    private void grow() {
        if (npairs >= off1s.length) {
            final int nlength = (off1s.length * 2);

            // Allocate all arrays before assigning any.
            // If an allocation fails, they remain equally unassigned.
            final int [] _off1s = Arrays.copyOf(this.off1s, nlength);
            final int [] _off2s = Arrays.copyOf(this.off2s, nlength);
            final int [] _len1s = Arrays.copyOf(this.len1s, nlength);
            final int [] _len2s = Arrays.copyOf(this.len2s, nlength);

            // Assign arrays without risk of exceptions.
            off1s = _off1s;
            off2s = _off2s;
            len1s = _len1s;
            len2s = _len2s;
        }
    }

    private void quicksort(int i1, int i2) {
        // Pivot element is the middle of the array.
        int pivot = (i1 + ((i2 - i1) >> 1));

        int i = i1;
        int j = i2;

        while (i <= j) {
            while (less(i, pivot))
                i++;

            while (more(j, pivot))
                j--;

            if (i <= j) {
                if (i != j) {
                    swap(i, j);

                    if (pivot == i) pivot = j;
                    else
                    if (pivot == j) pivot = i;
                }

                i++;
                j--;
            }
        }

        if (i1 <  j) quicksort(i1, j);
        if (i  < i2) quicksort(i, i2);
    }

    private boolean less(int a, int b) {
        final int cmpO1 = off1s[a] - off1s[b];
        if (cmpO1 < 0) return true;
        if (cmpO1 > 0) return false;
        assert (off1s[a] == off1s[b]);

        final int cmpO2 = off2s[a] - off2s[b];
        if (cmpO2 < 0) return true;
        if (cmpO2 > 0) return false;
        assert (off2s[a] == off2s[b]);

        // Note that order is reversed for lengths,
        // i.e. longer spans are sorted first.

        final int cmpL1 = len1s[b] - len1s[a];
        if (cmpL1 < 0) return true;
        if (cmpL1 > 0) return false;
        assert (len1s[a] == len1s[b]);

        final int cmpL2 = len2s[b] - len2s[a];
        if (cmpL2 < 0) return true;
        if (cmpL2 > 0) return false;
        assert (len2s[a] == len2s[b]);

        return false;
    }

    private boolean more(int a, int b) {
        return less(b, a);
    }

    private void swap(int a, int b) {
        if (a == b)
            return;

        final int off1 = off1s[a];
        final int off2 = off2s[a];
        final int len1 = len1s[a];
        final int len2 = len2s[a];

        off1s[a] = off1s[b];
        off2s[a] = off2s[b];
        len1s[a] = len1s[b];
        len2s[a] = len2s[b];

        off1s[b] = off1;
        off2s[b] = off2;
        len1s[b] = len1;
        len2s[b] = len2;
    }
}
