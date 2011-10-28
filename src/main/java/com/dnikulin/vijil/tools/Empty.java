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

/** Empty arrays of each primitive type, used as free sentinels. */
public final class Empty {
    public static final byte   [] bytes   = new byte   [0];
    public static final short  [] shorts  = new short  [0];
    public static final int    [] ints    = new int    [0];
    public static final long   [] longs   = new long   [0];
    public static final float  [] floats  = new float  [0];
    public static final double [] doubles = new double [0];
    public static final Object [] objects = new Object [0];
    public static final String [] strings = new String [0];

    public static final String    string  = "";

    public static byte[] copy(byte[] in) {
        if (in.length < 1) return bytes;
        return Arrays.copyOf(in, in.length);
    }

    public static short[] copy(short[] in) {
        if (in.length < 1) return shorts;
        return Arrays.copyOf(in, in.length);
    }

    public static int[] copy(int[] in) {
        if (in.length < 1) return ints;
        return Arrays.copyOf(in, in.length);
    }

    public static long[] copy(long[] in) {
        if (in.length < 1) return longs;
        return Arrays.copyOf(in, in.length);
    }

    public static float[] copy(float[] in) {
        if (in.length < 1) return floats;
        return Arrays.copyOf(in, in.length);
    }

    public static double[] copy(double[] in) {
        if (in.length < 1) return doubles;
        return Arrays.copyOf(in, in.length);
    }

    private Empty() {}
}
