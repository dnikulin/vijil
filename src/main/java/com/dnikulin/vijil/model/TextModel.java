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

package com.dnikulin.vijil.model;

import com.dnikulin.vijil.tools.Empty;
import com.dnikulin.vijil.tools.HashInts;

public final class TextModel implements Comparable<TextModel> {
    /** Sentinel text model singleton. */
    public static final TextModel    NULL = new TextModel("null", Empty.ints);

    /** Sentinel empty text model array. */
    public static final TextModel [] none = new TextModel[0];

    public  final String  hash;
    public  final int     size;

    private final int  [] symbols;
    private final int  [] offsets;
    private final byte [] lengths;
    private final byte [] meta;

    public TextModel(String hash, int[] symbols, int[] offsets, byte[] lengths, byte[] meta) {
        assert (hash    != null);
        assert (symbols != null);
        assert (offsets != null);
        assert (lengths != null);
        assert (meta    != null);

        assert (offsets.length == lengths.length);
        if (offsets.length > 0)
            assert (offsets.length == symbols.length);

        this.hash    = hash;
        this.size    = symbols.length;

        this.symbols = Empty.copy(symbols);
        this.offsets = Empty.copy(offsets);
        this.lengths = Empty.copy(lengths);
        this.meta    = Empty.copy(meta);

        for (int i = 0; i < offsets.length; i++) {
            assert (offsets[i] >= 0);
            assert (lengths[i] >= 1);
        }

        for (int i = 0; i < (offsets.length - 1); i++)
            assert ((offsets[i] + lengths[i]) <= offsets[i + 1]);
    }

    public TextModel(String hash, int[] lemmas) {
        this(hash, lemmas, Empty.ints, Empty.bytes, Empty.bytes);
    }

    public boolean hasSpans() {
        return (this.offsets.length > 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof TextModel))
            return false;

        TextModel that = (TextModel) obj;
        return this.hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return this.hash.hashCode();
    }

    @Override
    public String toString() {
        return this.hash;
    }

    @Override
    public int compareTo(TextModel that) {
        return this.hash.compareTo(that.hash);
    }

    public int symbol(int i) {
        return symbols[i];
    }

    public int offset(int i) {
        return offsets[i];
    }

    public int length(int i) {
        return lengths[i];
    }

    public byte[] meta() {
        return Empty.copy(meta);
    }

    public int minChar(int i) {
        return offset(i);
    }

    public int maxChar(int i) {
        return offset(i) + length(i);
    }

    public int hash(int ilemma, int nlemmas) {
        return HashInts.hash(symbols, ilemma, nlemmas);
    }
}
