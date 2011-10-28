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

import java.util.Arrays;

public final class TextModelBuilder {
    public static final int startSize = 8192;

    private int  [] symbols;
    private int  [] offsets;
    private byte [] lengths;
    private int     cursor;
    private int     lastoff;

    public TextModelBuilder() {
        this.symbols = new int  [startSize];
        this.offsets = new int  [startSize];
        this.lengths = new byte [startSize];
        this.cursor  = 0;
        this.lastoff = 0;
    }

    public final int length() {
        return cursor;
    }

    public final void extend(int length) {
        if (symbols.length >= length)
            return;

        int nlength = symbols.length;
        while (nlength < length)
            nlength <<= 1;

        symbols = extend(symbols, cursor, nlength);
        offsets = extend(offsets, cursor, nlength);
        lengths = extend(lengths, cursor, nlength);
    }

    public final void add(int symbol, int offset, byte length) {
        // Check basic consistency.
        assert (symbol >= 0);
        assert (offset >= 0);
        assert (length >= 1);

        // Check against previous position.
        assert (offset >= lastoff);

        // Ensure space for record.
        extend(cursor + 1);

        // Commit record.
        symbols [cursor] = symbol;
        offsets [cursor] = offset;
        lengths [cursor] = length;

        // Update cursor.
        cursor++;

        // Update stored position.
        lastoff = (offset + length);
    }

    public final void reset() {
        cursor  = 0;
        lastoff = 0;
    }

    public final TextModel finish(String hash) {
        return finish(hash, Empty.bytes);
    }

    public final TextModel finish(String hash, byte[] meta) {
        return new TextModel(
            hash,
            Arrays.copyOf(symbols, cursor),
            Arrays.copyOf(offsets, cursor),
            Arrays.copyOf(lengths, cursor),
            meta
        );
    }

    public static int[] extend(int[] array, int used, int size) {
        int[] copy = new int[size];
        System.arraycopy(array, 0, copy, 0, used);
        return copy;
    }

    public static byte[] extend(byte[] array, int used, int size) {
        byte[] copy = new byte[size];
        System.arraycopy(array, 0, copy, 0, used);
        return copy;
    }
}
