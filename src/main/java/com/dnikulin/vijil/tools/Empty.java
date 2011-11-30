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

import org.apache.commons.lang.ArrayUtils;

/** Empty arrays of each primitive type, used as free sentinels. */
public final class Empty {
    // It happens that Apache Commons Lang already has these,
    // so simply refer back to them with shorter cleaner names.
    public static final byte   [] bytes   = ArrayUtils.EMPTY_BYTE_ARRAY;
    public static final short  [] shorts  = ArrayUtils.EMPTY_SHORT_ARRAY;
    public static final int    [] ints    = ArrayUtils.EMPTY_INT_ARRAY;
    public static final long   [] longs   = ArrayUtils.EMPTY_LONG_ARRAY;
    public static final float  [] floats  = ArrayUtils.EMPTY_FLOAT_ARRAY;
    public static final double [] doubles = ArrayUtils.EMPTY_DOUBLE_ARRAY;
    public static final Object [] objects = ArrayUtils.EMPTY_OBJECT_ARRAY;
    public static final String [] strings = ArrayUtils.EMPTY_STRING_ARRAY;

    // Apache Commons Lang does not have an empty string
    // (which is almost certainly interned by the compiler anyway)
    // so provide one here.
    public static final String    string  = "";

    public static byte[] copy(byte[] in) {
        return ArrayUtils.nullToEmpty(in);
    }

    public static short[] copy(short[] in) {
        return ArrayUtils.nullToEmpty(in);
    }

    public static int[] copy(int[] in) {
        return ArrayUtils.nullToEmpty(in);
    }

    public static long[] copy(long[] in) {
        return ArrayUtils.nullToEmpty(in);
    }

    public static float[] copy(float[] in) {
        return ArrayUtils.nullToEmpty(in);
    }

    public static double[] copy(double[] in) {
        return ArrayUtils.nullToEmpty(in);
    }

    private Empty() {}
}
