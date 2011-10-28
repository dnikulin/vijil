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

package com.dnikulin.vijil.parse;

import com.dnikulin.vijil.tools.Empty;

public final class Lemma implements Comparable<Lemma> {
    // Immutable identity.
    public final int symbol;

    // Mutable extensions.
    public boolean   stopword;
    public int []    synsets;

    public Lemma(int code) {
        this.symbol   = code;
        this.stopword = false;
        this.synsets  = Empty.ints;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if ((obj instanceof Lemma) == false) return false;
        final Lemma that = (Lemma) obj;
        return (this.symbol == that.symbol);
    }

    @Override
    public int hashCode() {
        return symbol;
    }

    @Override
    public int compareTo(Lemma that) {
        return (this.symbol - that.symbol);
    }
}