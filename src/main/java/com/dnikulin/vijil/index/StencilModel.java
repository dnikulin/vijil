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

public final class StencilModel {
    public final int     size;
    public final int     width;
    public final int     nstencils;
    public final int []  offsets;
    public final boolean sorted;

    public StencilModel(int size, int[] offsets, boolean sorted) {
        assert (size > 1);
        assert (size <= offsets.length);

        assert (offsets.length > 0);
        assert ((offsets.length % size) == 0);

        // Infer number of stencils from offsets.
        this.nstencils = offsets.length / size;

        // Infer stencil width from offsets.
        this.width = max(offsets) + 1;
        assert (this.width >= size);

        this.size    = size;
        this.offsets = offsets;
        this.sorted  = sorted;
    }

    public void sample(int[] buffer, TextModel text, int offset, int nstencil) {
        assert (buffer.length == size);
        assert (offset >= 0);
        assert ((offset + size) <= text.size);
        assert (nstencil >= 0);
        assert (nstencil < nstencils);

        // Populate output buffer based on stencil offsets.
        for (int i = nstencil * size, o = 0; o < size; i++, o++)
            buffer[o] = text.symbol(offset + offsets[i]);

        // Sort output buffer if configured so.
        if (sorted)
            Arrays.sort(buffer);
    }

    public int width(int nstencil) {
        assert (nstencil >= 0);
        assert (nstencil < nstencils);
        return offsets[(nstencil * size) + size - 1] + 1;
    }

    public static int max(int[] xs) {
        int out = 0;
        for (int x : xs)
            out = Math.max(out, x);
        return out;
    }
}
