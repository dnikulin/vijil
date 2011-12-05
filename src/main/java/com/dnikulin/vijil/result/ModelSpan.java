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

public final class ModelSpan {
    public static final ModelSpan [] none = new ModelSpan[0];

    public final String       hash;
    public final SpanDomain   domain;
    public final int          code;
    public final int          min;
    public final int          len;
    public final int          max;

    public ModelSpan(String hash, SpanDomain domain, int code, int min, int len) {
        assert (hash != null);
        assert (code >= 0);
        assert (min  >= 0);
        assert (len  >= 1);

        this.hash    = hash;
        this.domain  = domain;
        this.code    = code;
        this.min     = min;
        this.len     = len;
        this.max     = (min + len);
    }

    @Override
    public String toString() {
        return "ModelSpan(hash=" + hash + ", code=" + code + ", min=" + min + ", max=" + max + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if ((obj instanceof ModelSpan) == false) return false;
        final ModelSpan that = (ModelSpan) obj;
        return (this.code == that.code);
    }

    @Override
    public int hashCode() {
        return code;
    }

    public boolean includes(int position) {
        return ((min <= position) && (position < max));
    }
}
