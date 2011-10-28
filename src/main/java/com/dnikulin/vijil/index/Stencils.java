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

import static com.dnikulin.vijil.tools.HashInts.hash;

/** Online index for lemma subsequence matching.
 *
 * @author Dmitri Nikulin
 */
public final class Stencils extends MindexCore {
    /** Stencil model. */
    public final StencilModel model;

    /** Stencil index within model for each entry in each bin. */
    protected final byte [][] ibstens;

    /** Buffer for stencil sampling (for add() only). */
    protected final int    [] buffer;

    /** Construct an index for the given stencil model.
     *
     * @param matchLength  Exact number of lemmas to regard as a match.
     */
    public Stencils(StencilModel model) {
        this.model = model;

        // Allocate empty initial bin array.
        this.ibstens = new byte[nbins][];

        // Allocate buffer for stencil sampling.
        this.buffer = new int[model.size];
    }

    /** Add a text to the index.
     *
     * @param text  Parsed text to index for exact matches.
     */
    @Override
    public void add(final TextModel text) {
        // Verify parameters.
        assert (text        != null);
        assert (text.hash   != null);

        final int    length  = text.size;
        final int    itext   = ntexts;
        addText(text);

        for (int ilem = 0; ilem < length; ilem++) {
            for (int isten = 0; isten < model.nstencils; isten++) {
                // Exclude stencils that pass the edge of the lemma data.
                if ((ilem + model.width(isten)) > length)
                    continue;

                // Sample stencil.
                model.sample(buffer, text, ilem, isten);

                // Hash buffer, deciding the bin and mixing with the record.
                final int hash = hash(buffer, 0, buffer.length);

                // Calculate bin index.
                final int ibin = (hash & nbinmask);

                // Append to the bin's chained array, mixing with the hash.
                addEntry(ibin, itext ^ hash, ilem ^ hash, (byte) isten);
            }
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

        // Allocate temporary buffers for stencil sampling.
        final int [] buffer1 = new int[model.size];
        final int [] buffer2 = new int[model.size];

        final int length1 = text1.size;
        for (int ilem1 = 0; ilem1 < length1; ilem1++) {
            for (int isten1 = 0; isten1 < model.nstencils; isten1++) {
                // Exclude stencils that pass the edge of the lemma data.
                final int nlems1 = model.width(isten1);
                if ((ilem1 + nlems1) > length1)
                    continue;

                // Sample stencil into buffer 1.
                model.sample(buffer1, text1, ilem1, isten1);

                // Hash buffer, deciding the bin and mixing with the record.
                final int     hash   = hash(buffer1, 0, buffer1.length);

                // Calculate bin index.
                final int     ibin   = (hash & nbinmask);
                final int     nposts = nbposts [ibin];
                final int  [] itexts = ibtexts [ibin];
                final int  [] ilems  = iblems  [ibin];
                final byte [] istens = ibstens [ibin];

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
                    final int  ilem2   = (ilems[ipost] ^ hash);
                    final byte isten2  = istens[ipost];
                    final int  nlems2  = model.width(isten2);
                    final int  length2 = text2.size;
                    if (((ilem2 + nlems2) > length2) || (ilem2 < 0))
                        continue;

                    // Sample stencil into buffer 2.
                    model.sample(buffer2, text2, ilem2, isten2);

                    // Cheap eliminations have failed, and it is very likely
                    // that this is a real match. Confirm the full length of
                    // the match before invoking the visitor.
                    for (int i = 0; i < buffer1.length; i++) {
                        // Skip to next bin posting.
                        if (buffer1[i] != buffer2[i])
                            continue perbin;
                    }

                    // The match is confirmed, so invoke the visitor.
                    each.matched(text1, text2, ilem1, ilem2, nlems1, nlems2);

                    // Update previous text index to strengthen future eliminations.
                    prevText = itext2;
                }
            }
        }
    }

    /** Add an entry to a bin.
     *
     * @param ibin   Bin index.
     * @param itext  Text index.
     * @param ilem   Lemma index.
     * @param isten  Stencil index.
     */
    protected void addEntry(int ibin, int itext, int ilem, byte isten) {
        assert (ibtexts.length == nbins);
        assert (iblems.length  == nbins);
        assert (ibstens.length  == nbins);
        assert ((ibtexts[ibin] == null) == (iblems[ibin] == null));
        assert ((ibtexts[ibin] == null) == (ibstens[ibin] == null));

        // Allocate per-bin arrays if necessary.
        if (ibtexts[ibin] == null) {
            // Allocate both before assigning either.
            // If an allocation fails, they remain equally unassigned.
            final int  [] itexts2 = new int  [8];
            final int  [] ilems2  = new int  [8];
            final byte [] istens2 = new byte [8];
            ibtexts [ibin] = itexts2;
            iblems  [ibin] = ilems2;
            ibstens [ibin] = istens2;
        }

        assert (ibtexts[ibin].length == iblems[ibin].length);
        assert (ibtexts[ibin].length == ibstens[ibin].length);

        // Expand per-bin arrays if necessary.
        if (nbposts[ibin] >= ibtexts[ibin].length) {
            final int nlength = (ibtexts[ibin].length * 2);

            // Allocate both before assigning either.
            // If an allocation fails, they remain equally unassigned.
            final int  [] itexts2  = Arrays.copyOf(ibtexts [ibin], nlength);
            final int  [] ilems2   = Arrays.copyOf(iblems  [ibin], nlength);
            final byte [] istens2  = Arrays.copyOf(ibstens [ibin], nlength);
            ibtexts  [ibin] = itexts2;
            iblems   [ibin] = ilems2;
            ibstens  [ibin] = istens2;
        }

        // Write into per-bin arrays.
        final int at       = nbposts[ibin];
        ibtexts [ibin][at] = itext;
        iblems  [ibin][at] = ilem;
        ibstens [ibin][at] = isten;
        nbposts [ibin]     = (at + 1);
    }
}
