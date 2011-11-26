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

package com.dnikulin.vijil.index;

import java.util.Arrays;

import com.dnikulin.vijil.model.TextModel;

public final class RadixSearch {
    public final MatchVisitor  visitor;
    public final TextModelTape tape;
    public final int           maxDepth;
    public final int           nsuff;
    public final int           maxsym;

    /** "Stack" of bucket counts. */
    private final int [][] countStack;

    /** Prefix sum at this level. */
    private final int []   index;

    /** Depth-limited suffix array. */
    private final int []   suffix;

    /** Buffer for updated suffix array. */
    private final int []   buffer;

    /** Oracle for storing depth-offset values. */
    private final int []   oracle;

    public RadixSearch(MatchVisitor visitor, TextModelTape tape, int maxDepth) {
        this.visitor  = visitor;
        this.tape     = tape;
        this.maxDepth = maxDepth;
        this.nsuff    = tape.nsymbols - maxDepth;
        this.maxsym   = tape.maximum + 1;

        // Allocate arrays.
        this.countStack = new int[maxDepth][maxsym];
        this.index      = new int          [maxsym];
        this.suffix     = new int[nsuff];
        this.buffer     = new int[nsuff];
        this.oracle     = new int[nsuff];
    }

    public void search() {
        // Initialise suffix array.
        for (int i = 0; i < nsuff; i++)
            suffix[i] = i;

        radixSort(0, nsuff, 0);
    }

    private void radixSort(int min, int max, int depth) {
        assert(min   >= 0);
        assert(max   >= min);
        assert(max   <= nsuff);
        assert(depth >= 0);
        assert(depth <= maxDepth);

        // Calculate length of this sub-array.
        final int len = (max - min);

        // No possible matches, return now.
        if (len < 2)
            return;

        // Check if full depth reached.
        if (depth >= maxDepth) {
            // Loop over all pairs.
            for (int i1 = min; i1 < max; i1++) {
                final int isymbol1 = suffix[i1];
                final int imodel1  = tape.modelIndex(isymbol1);
                final TextModel model1 = tape.model(imodel1);
                final int ioffset1 = tape.modelOffset(isymbol1);

                // Check if the span crosses models.
                if (imodel1 != tape.modelIndex(isymbol1 + depth - 1))
                    continue;

                for (int i2 = (i1 + 1); i2 < max; i2++) {
                    final int isymbol2 = suffix[i2];
                    final int imodel2  = tape.modelIndex(isymbol2);

                    // Check if the match is within the same model.
                    if (imodel1 == imodel2)
                        continue;

                    // Check if the span crosses models.
                    if (imodel2 != tape.modelIndex(isymbol2 + depth - 1))
                        continue;

                    final TextModel model2 = tape.model(imodel2);
                    final int ioffset2 = tape.modelOffset(isymbol2);

                    // Notify listener of match.
                    visitor.matched(model1, model2, ioffset1, ioffset2, depth, depth);
                }
            }

            // Exit here.
            return;
        }

        // For short lengths, use insertion sort.
//        if (len <= 16) {
//            insertionSort(min, max, depth);
//            return;
//        }

        // Refer to bucket at this depth.
        final int [] bucket = countStack[depth];
        assert(bucket.length == maxsym);
        assert(index.length == maxsym);

        // Clear bucket.
        Arrays.fill(bucket, 0);

        // Copy into oracle.
        for (int i = min; i < max; i++)
            oracle[i] = tape.symbol(suffix[i] + depth);

        // Count into bucket.
        for (int i = min; i < max; i++)
            bucket[oracle[i]]++;

        // Convert into prefix sum.
        index[0] = 0;
        for (int i = 1; i < maxsym; i++)
            index[i] = (index[i - 1] + bucket[i - 1]);

        // Recover into buffer.
        for (int i = min; i < max; i++)
            buffer[index[oracle[i]]++] = suffix[i];

        // Copy buffer back into suffix array.
        for (int i = min; i < max; i++)
            suffix[i] = buffer[i - min];

        int cursor = bucket[0] + min;
        assert(cursor <= max);

        // Recurse for first bucket.
        if (cursor > 1)
            radixSort(min, cursor, depth + 1);

        for (int i = 1; i < maxsym; i++) {
            final int count = bucket[i];

            if (count > 0) {
                // Recurse for this bucket.
                if (count > 1)
                    radixSort(cursor, cursor + count, depth + 1);

                // Move cursor along.
                cursor += count;
            }
        }

        // Recurse for last bucket.
        if ((cursor + 1) < max)
            radixSort(cursor, max, depth + 1);
    }

    private void insertionSort(int min, int max, int depth) {
        for (int i = (min + 1); i < max; i++) {
            // Key at given depth.
            final int sym1 = tape.symbol(suffix[i] + depth);

            // Swap down until in order.
            for (int j = i; j > min; j--) {
                final int sym2 = tape.symbol(suffix[j - 1] + depth);
                if (sym1 >= sym2)
                    break;
                swapSuffix(j, j - 1);
            }
        }
    }

    private void swapSuffix(int i1, int i2) {
        final int suf = suffix[i1];
        suffix[i1] = suffix[i2];
        suffix[i2] = suf;
    }

    public static void search(MatchVisitor visitor, TextModelTape tape, int maxDepth) {
        new RadixSearch(visitor, tape, maxDepth).search();
    }

    public static void search(MatchVisitor visitor, TextModel[] models, int maxDepth) {
        new RadixSearch(visitor, new TextModelTape(models), maxDepth).search();
    }
}
