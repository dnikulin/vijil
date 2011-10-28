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

// This code is based on only the 32-bit hash of MurmurHash3.
// It is not meant to be compatible, only to have the same hashing qualities.
//
// http://code.google.com/p/smhasher/wiki/MurmurHash3

package com.dnikulin.vijil.tools;

public final class HashInts {
    public static int hash(int[] data) {
        return hash(data, 0, data.length);
    }

    public static int hash(int[] data, int at, int len) {
        return hash(data, at, len, 0xbcaa747);
    }

    public static int hash(int[] data, int at, int len, int seed) {
        assert (at  >= 0);
        assert (len >  0);
        assert ((at + len) <= data.length);

        int h = seed;

        for (int i = 0; i < len; i++) {
            int k = data[at + i];

            k *= 0xcc9e2d51;
            k  = Integer.rotateLeft(k, 15);
            k *= 0x1b873593;

            h ^= k;
            h  = Integer.rotateLeft(h, 13);
            h  = ((h * 5) + 0xe6546b64);
        }

        return fmix(h);
    }

    public static int fmix(int h) {
        h ^= (h >>> 16);
        h *= (0x85ebca6b);
        h ^= (h >>> 13);
        h *= (0xc2b2ae35);
        h ^= (h >>> 16);
        return h;
    }

    private HashInts() {}
}
