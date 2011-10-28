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

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

// The hash values here are not special, but must now be consistent.

public class HashTest {
    @Test
    public void testEncodeEmpty() {
        byte[] bytes = new byte[0];
        String hash = Hash.bytesToCode(bytes);
        assertEquals("000000000000000000000000", hash);
    }

    @Test
    public void testEncodeZero7() {
        byte[] bytes = new byte[7];
        String hash = Hash.bytesToCode(bytes);
        assertEquals("000000000000000000000000", hash);
    }

    @Test
    public void testHashEmpty() {
        byte[] bytes = new byte[0];
        String hash = Hash.hash(bytes);
        assertEquals("UcoxA2FBr74Th2NII6ESQBPP", hash);
    }

    @Test
    public void testHashZero7() {
        byte[] bytes = new byte[7];
        String hash = Hash.hash(bytes);
        assertEquals("LsFpiu6iY0GcwWCGHEHHdIeq", hash);
    }

    @Test
    public void testHashMix7() {
        byte[] bytes = new byte[]{1, 4, -8, 0, 2, 127, -34};
        String hash = Hash.hash(bytes);
        assertEquals("zpF9cDeT8bNcoYBudgMeVq4p", hash);
    }

    @Test
    public void testHashMax7() {
        byte[] bytes = new byte[7];
        Arrays.fill(bytes, (byte) 0xFF);
        String hash = Hash.hash(bytes);
        assertEquals("eqABmJxW2RZqwwMk3HJHWjyl", hash);
    }
}