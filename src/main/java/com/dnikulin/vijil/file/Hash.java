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

package com.dnikulin.vijil.file;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hash {
    /** Base 62 alphabet, shuffled. For convenience, 0 is still 0. */
    public static final String tableString =
        "0JXD9UsYGREMitBfQAcgdxmCeV1ZTz4krq6vWOpbFlIKP7Sayhj28L5wN3uonH";

    /** Character table for the base 60 alphabet. */
    public static final char [] table     = tableString.toCharArray();

    /** Standard character set for encoding strings. */
    public static final Charset utf8      = Charset.forName("UTF-8");

    /** Hash function of choice. */
    public static final String hashName   = "SHA-256";

    /** Size of the hash function output in bytes. */
    public static final int    hashSize   = 32;

    /** Length of output string, considerably less than the full hash. */
    public static final int    codeSize   = 24;

    /** BigInteger for 256. */
    public static final BigInteger big256 = BigInteger.valueOf(256);

    /** BigInteger for 62. */
    public static final BigInteger big62  = BigInteger.valueOf(62);

    public static byte[] hashBytes(byte[] bytes) {
        try {
            return MessageDigest.getInstance(hashName).digest(bytes);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return new byte[hashSize];
        }
    }

    public static byte[] hashBytes(String string) {
        return hashBytes(string.getBytes(utf8));
    }

    public static String hash(byte[] data) {
        return bytesToCode(hashBytes(data));
    }

    public static String hash(String string) {
        return bytesToCode(hashBytes(string));
    }

    public static String bytesToCode(byte[] bytes) {
        return resolve(compose(bytes));
    }

    private static BigInteger compose(byte[] bytes) {
        // Start with 0 integer.
        BigInteger total = BigInteger.ZERO;

        // Accumulate each byte.
        for (byte b : bytes) {
            // Raise magnitude of the integer.
            total = total.multiply(big256);

            // Add single byte.
            total = total.add(BigInteger.valueOf(0xFFL & b));
        }

        return total;
    }

    private static String resolve(BigInteger start) {
        // Buffer for full size hash string.
        char [] out = new char[codeSize];

        // Start from entire integer.
        BigInteger total = start;

        for (int i = 0; i < codeSize; i++) {
            // Take modulo for encoding index.
            int code = total.mod(big62).intValue();
            out[i] = table[code];

            // Remove modulo from integer.
            total = total.divide(big62);
        }

        // Compile string.
        return new String(out);
    }

    private Hash() {}
}
