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

package com.dnikulin.vijil.tools;

import java.util.Arrays;

public final class Integers {
    private static final int initialSize = 32;

    public int [] data;
    public int    count;

    public Integers() {
        data  = new int[initialSize];
        count = 0;
    }

    public void add(int x) {
        checkup();
        if (count >= data.length)
            data = Arrays.copyOf(data, data.length * 2);
        data[count++] = x;
        checkup();
    }

    public void clear() {
        count = 0;
    }

    public void makeSet() {
        sort();
        uniq();
        shrink();
    }

    public void sort() {
        checkup();
        Arrays.sort(data, 0, count);
    }

    public void uniq() {
        checkup();
        int o = 0;

        for (int i = 1; i < count; i++) {
            assert (o < i);
            assert (data[o] <= data[i]);

            if (data[o] != data[i])
                data[++o] = data[i];

            assert (o <= i);
        }

        count = o + 1;
        checkup();
    }

    public void shrink() {
        checkup();
        final int minlength = Math.max(count, initialSize);
        int nlength = data.length;
        while ((nlength >> 1) > minlength)
            nlength >>= 1;

        if ((nlength >= minlength) && (nlength < data.length))
            data = Arrays.copyOf(data, nlength);
        checkup();
    }

    public int[] snapshot() {
        return Arrays.copyOf(data, count);
    }

    public boolean contains(final int value) {
        // Take final data to help JIT.
        final int[] fdata = data;

        int min = 0;
        int max = count - 1;

        // Shortcut: check count and extents.
        if (count < 1         ) return false;
        if (value < fdata[min]) return false;
        if (value > fdata[max]) return false;

        while ((max - min) > 8) {
            final int mid = (min + ((max - min) >>> 1));
            final int cut = fdata[mid];

            if (cut == value)
                return true;

            if (cut > value)
                max = mid - 1;
            else
                min = mid + 1;
        }

        while (min <= max) {
            if (fdata[min] == value)
                return true;
            min++;
        }

        return false;
    }

    private void checkup() {
        assert (count >= 0);
        assert (data != null);
        assert (data.length >= count);
        assert (data.length >= initialSize);
    }
}
