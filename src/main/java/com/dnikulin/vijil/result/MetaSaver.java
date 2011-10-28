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

import java.util.HashMap;

import com.dnikulin.vijil.index.MatchVisitor;
import com.dnikulin.vijil.model.TextModel;

public final class MetaSaver implements MatchVisitor {
    public final MatchVisitor            next;
    public final HashMap<String, byte[]> meta;

    public MetaSaver(MatchVisitor next) {
        this.next = next;
        this.meta = new HashMap<String, byte[]>();
    }

    public MetaSaver() {
        this(null);
    }

    @Override
    public void matched(TextModel text1, TextModel text2, int offset1, int offset2, int length1, int length2) {
        meta.put(text1.hash, text1.meta());
        meta.put(text2.hash, text2.meta());

        if (next != null)
            next.matched(text2, text1, offset2, offset1, length2, length1);
    }
}
