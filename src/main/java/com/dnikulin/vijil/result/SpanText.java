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

import com.dnikulin.vijil.tools.Empty;

public final class SpanText implements Comparable<SpanText> {
    public  final String text2;
    public  final int min;
    public  final int max;

    private final byte[] meta2;

    public SpanText(String text2, byte[] meta2, int min, int max) {
        assert (max > min);

        this.text2 = text2;
        this.min = min;
        this.max = max;

        this.meta2 = Empty.copy(meta2);
    }

    public byte[] meta2() {
        return Empty.copy(meta2);
    }

    @Override
    public int compareTo(SpanText that) {
        // Highest key is source text hash.
        final int cmpT = this.text2.compareTo(that.text2);
        if (cmpT != 0)
            return cmpT;

        // Middle key is start of span, increasing.
        final int cmpL = this.min - that.min;
        if (cmpL != 0)
            return cmpL;

        // Low key is end of span, increasing.
        return this.max - that.max;
    }
}
