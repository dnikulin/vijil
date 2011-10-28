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

public final class LinkSpanSet {
    public static final LinkSpanSet [] none     = new LinkSpanSet[0];
    public static final LinkSpanSet    sentinel = new LinkSpanSet(0, 0, SpanDomain.SYMBOLS);

    public final int          code;
    public final LinkSpan []  spans;
    public final SpanDomain   domain;

    public LinkSpanSet(int code, int nspans, SpanDomain domain) {
        this.code    = code;
        this.spans   = new LinkSpan [nspans];
        this.domain  = domain;
    }

    @Override
    public String toString() {
        return "LinkSpanSet(" + code + ')';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if ((obj instanceof LinkSpanSet) == false) return false;
        final LinkSpanSet that = (LinkSpanSet) obj;
        return (this.code == that.code);
    }

    @Override
    public int hashCode() {
        return code;
    }
}
