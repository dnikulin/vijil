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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HashIntsTest {
    // Even 2**16 is likely to collide, as per the Birthday Paradox.
    public static final int count = 1 << 16;

    @Test
    public void testCollideSingle() {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < count; i++) {
            int[] data = new int[]{i};
            int hash = HashInts.hash(data);
            assertTrue(set.add(hash));
        }
    }

    @Test
    public void testCollideRepeat() {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < count; i++) {
            int[] data = new int[]{i, i, i};
            int hash = HashInts.hash(data);
            assertTrue(set.add(hash));
        }
    }

    @Test
    public void testCollideSeries() {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < count; i++) {
            // This pattern creates obvious overlap,
            // simulating realistic sub-list matching.
            int[] data = new int[]{i, i + 1, i + 2, i + 3};
            int hash = HashInts.hash(data);
            assertTrue(set.add(hash));
        }
    }
}