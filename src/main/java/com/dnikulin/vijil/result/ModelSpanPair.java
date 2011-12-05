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

public final class ModelSpanPair {
    public static final ModelSpanPair [] none = new ModelSpanPair[0];

    public final ModelSpan span1;
    public final ModelSpan span2;
    public final int       code;

    public ModelSpanPair(ModelSpan span1, ModelSpan span2, int code) {
        assert (span1 !=  null);
        assert (span2 !=  null);
        assert (span1 != span2);
        assert (code  >=     0);
        assert (span1.hash.equals(span2.hash) == false);

        this.span1 = span1;
        this.span2 = span2;
        this.code  = code;
    }

    @Override
    public String toString() {
        return "ModelSpanPair(span1=" + span1.toString() + ", span2=" + span1.toString() + ", code=" + code + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if ((obj instanceof ModelSpanPair) == false) return false;
        final ModelSpanPair that = (ModelSpanPair) obj;
        return (this.code == that.code);
    }

    @Override
    public int hashCode() {
        return code;
    }

    public ModelSpan[] spans() {
        return new ModelSpan[]{span1, span2};
    }
}
