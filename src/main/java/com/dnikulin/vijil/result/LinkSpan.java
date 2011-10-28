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

public final class LinkSpan {
    public static final LinkSpan [] none = new LinkSpan[0];

    public final LinkSpanSet  set;

    public final String       hash;
    public final int          code;
    public final int          min;
    public final int          max;
    public final int          len;

    public final LinkSpan []  links;

    public LinkSpan(LinkSpanSet set, int code, String hash, int min, int max, LinkSpan[] links) {
        assert (set   != null);
        assert (hash  != null);
        assert (links != null);
        assert (code  >= 0);
        assert (min   >= 0);
        assert (max   >  min);

        this.set   = set;
        this.code  = code;
        this.hash  = hash;
        this.min   = min;
        this.max   = max;
        this.links = links;

        this.len   = (max - min);
    }

    public LinkSpan(int code, String hash, int min, int max) {
        this(LinkSpanSet.sentinel, code, hash, min, max, none);
    }

    @Override
    public String toString() {
        return "LinkSpan(" + hash + ", " + min + ", " + max + ')';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if ((obj instanceof LinkSpan) == false) return false;
        final LinkSpan that = (LinkSpan) obj;
        return (this.code == that.code);
    }

    @Override
    public int hashCode() {
        return code;
    }

    public boolean includes(int position) {
        return ((min <= position) && (position < max));
    }

    public boolean isFromText(String hash) {
        return hash.equals(hash);
    }

    public boolean isToText(String hash) {
        for (LinkSpan link : links) {
            if (link.isFromText(hash))
                return true;
        }
        return false;
    }
}
