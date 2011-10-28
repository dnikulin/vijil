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

/** Online index for lemma matching, base class.
 *
 * @author Dmitri Nikulin
 */
public abstract class MindexCore {
    /** Total number of bins in the hash table. */
    public static final int nbins    = (1 << 20);

    /** Bit mask for bin indices. */
    public static final int nbinmask = nbins - 1;

    /** Text index for each entry in each bin. */
    protected final int     [][] ibtexts;

    /** Lemma index (within text) for each entry in each bin. */
    protected final int     [][] iblems;

    /** Number of entries populated in each bin. */
    protected final int       [] nbposts;

    /** Texts recorded in this index. */
    protected       TextModel  [] texts;

    /** Number of texts recorded in this index. */
    protected       int          ntexts;

    /** Construct an index. */
    public MindexCore() {
        // Allocate large initial text array.
        this.texts   = new TextModel[1024];
        this.ntexts  = 0;

        // Allocate empty initial bin arrays.
        this.ibtexts = new int[nbins][];
        this.iblems  = new int[nbins][];
        this.nbposts = new int[nbins];
    }

    /** Clear the index to contain no entries. */
    public void clear() {
        // Reset text references to allow GC.
        for (int itext = 0; itext < ntexts; itext++)
            texts[itext] = null;
        ntexts = 0;

        // Reset entry count in each bin.
        for (int ibin = 0; ibin < nbins; ibin++)
            nbposts[ibin] = 0;
    }

    /** Add a text to the index.
     *
     * @param text  Parsed text to index for exact matches.
     */
    public abstract void add(TextModel text);

    /** Match a text against other texts in the index.
     *
     * @param text1  Query text that will be 'text1' in visitor calls.
     * @param each   Visitor that will be invoked for each exact match.
     */
    public abstract void search(TextModel text1, MatchVisitor each);

    /** Add a text to the text list.
     *
     * @param text  Parsed text to add.
     */
    protected void addText(TextModel text) {
        if (ntexts >= texts.length)
            texts = Arrays.copyOf(texts, texts.length * 2);
        texts[ntexts++] = text;
    }

    /** Add an entry to a bin.
     *
     * @param ibin   Bin index.
     * @param itext  Text index.
     * @param ilem   Lemma index.
     */
    protected void addEntry(int ibin, int itext, int ilem) {
        assert (ibtexts.length == nbins);
        assert (iblems.length  == nbins);
        assert ((ibtexts[ibin] == null) == (iblems[ibin] == null));

        // Allocate per-bin arrays if necessary.
        if (ibtexts[ibin] == null) {
            // Allocate both before assigning either.
            // If an allocation fails, they remain equally unassigned.
            final int[] itexts2 = new int[8];
            final int[] ilems2  = new int[8];
            ibtexts [ibin] = itexts2;
            iblems  [ibin] = ilems2;
        }

        assert (ibtexts[ibin].length == iblems[ibin].length);

        // Expand per-bin arrays if necessary.
        if (nbposts[ibin] >= ibtexts[ibin].length) {
            final int nlength = (ibtexts[ibin].length * 2);

            // Allocate both before assigning either.
            // If an allocation fails, they remain equally unassigned.
            final int[] itexts2 = Arrays.copyOf(ibtexts [ibin], nlength);
            final int[] ilems2  = Arrays.copyOf(iblems  [ibin], nlength);
            ibtexts [ibin] = itexts2;
            iblems  [ibin] = ilems2;
        }

        // Write into per-bin arrays.
        final int at       = nbposts[ibin];
        ibtexts [ibin][at] = itext;
        iblems  [ibin][at] = ilem;
        nbposts [ibin]     = (at + 1);
    }
}
