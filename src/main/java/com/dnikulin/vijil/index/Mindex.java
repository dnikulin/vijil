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

import com.dnikulin.vijil.model.TextModel;

/** Online index for exact-length lemma sublist matching.
 *
 * @author Dmitri Nikulin
 */
public final class Mindex extends MindexCore {
    /** Exact match length. */
    public final int matchLength;

    /** Construct an index for the given match length.
     *
     * @param matchLength  Exact number of lemmas to regard as a match.
     */
    public Mindex(int matchLength) {
        assert (matchLength > 0);

        this.matchLength = matchLength;
    }

    /** Add a text to the index.
     *
     * @param text  Parsed text to index for exact matches.
     */
    @Override
    public void add(TextModel text) {
        // Verify parameters.
        assert (text        != null);
        assert (text.hash   != null);

        final int itext = ntexts;
        addText(text);

        final int last = text.size + 1 - matchLength;
        for (int ilem = 0; ilem < last; ilem++) {
            // Hash lemma sub-list, deciding the bin and mixing with the record.
            final int hash = text.hash(ilem, matchLength);

            // Calculate bin index.
            final int ibin = (hash & nbinmask);

            // Append to the bin's chained array, mixing with the hash.
            addEntry(ibin, itext ^ hash, ilem ^ hash);
        }
    }

    /** Match a text against other texts in the index.
     *
     * @param text1  Query text that will be 'text1' in visitor calls.
     * @param each   Visitor that will be invoked for each exact match.
     */
    @Override
    public void search(TextModel text1, MatchVisitor each) {
        // Verify parameters.
        assert (text1        != null);
        assert (text1.hash   != null);
        assert (each         != null);

        final int last = text1.size + 1 - matchLength;
        for (int ilem1 = 0; ilem1 < last; ilem1++) {
            // Hash lemma sub-list, deciding the bin and mixing with the record.
            final int hash = text1.hash(ilem1, matchLength);

            // Calculate bin index.
            final int    ibin   = (hash & nbinmask);
            final int    nposts = nbposts [ibin];
            final int [] itexts = ibtexts [ibin];
            final int [] ilems  = iblems  [ibin];

            // Maintain previous text index to confirm forward progress.
            int prevText = 0;

            // Search each entry in the bin's chained array.
            perbin: for (int ipost = 0; ipost < nposts; ipost++) {
                // Eliminate entry by text index.
                // If the hash was 'wrong', this is almost certain to be
                // an invalid text index, allowing it to be ignored at
                // very low runtime cost.
                final int itext2 = (itexts[ipost] ^ hash);
                if ((itext2 >= ntexts) || (itext2 < prevText))
                    continue;

                // Eliminate entry by text identity.
                final TextModel text2 = texts[itext2];
                if (text1 == text2)
                    continue;

                // Eliminate entry by lemma index within text.
                // As with the text index, this is almost certain to be
                // an invalid lemma index.
                final int ilem2 = (ilems[ipost] ^ hash);
                if (((ilem2 + matchLength) > text2.size) || (ilem2 < 0))
                    continue;

                // Cheap eliminations have failed, and it is very likely
                // that this is a real match. Confirm the full length of
                // the match before invoking the visitor.
                for (int i = 0; i < matchLength; i++) {
                    final int lem1 = text1.symbol(ilem1 + i);
                    final int lem2 = text2.symbol(ilem2 + i);

                    // Skip to next bin posting.
                    if (lem1 != lem2)
                        continue perbin;
                }

                // The match is confirmed, so invoke the visitor.
                each.matched(text1, text2, ilem1, ilem2, matchLength, matchLength);

                // Update previous text index to strengthen future eliminations.
                prevText = itext2;
            }
        }
    }
}
