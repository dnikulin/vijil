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

import java.util.HashSet;

public final class SpanToTexts {
    public static final SpanToTexts[] none = new SpanToTexts[0];

    public final String   hash;
    public final int      nlemmas;
    public final int      code;
    public       int      min;
    public       int      max;

    public final HashSet<String> hashes;

    public SpanToTexts(int code, String hash, int nlemmas, int min, int max) {
        this.hash    = hash;
        this.code    = code;
        this.nlemmas = nlemmas;

        this.min     = min;
        this.max     = max;

        this.hashes  = new HashSet<String>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if ((obj instanceof SpanToTexts) == false) return false;
        final SpanToTexts that = (SpanToTexts) obj;
        return (this.code == that.code);
    }

    @Override
    public int hashCode() {
        return code;
    }
}