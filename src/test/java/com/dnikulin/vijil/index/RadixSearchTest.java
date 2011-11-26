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

package com.dnikulin.vijil.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dnikulin.vijil.model.TextModel;

public class RadixSearchTest {
    public static final int depth = 6;

    @Test
    public void testMatchMindex() {
        final TextModel    text1 = MindexTest.makeBody();
        final TextModel    text2 = MindexTest.makeBody();
        final TextModel [] texts = new TextModel[]{text1, text2};

        final boolean offsets[] = new boolean[text1.size + 1 - depth];

        MatchVisitor visitor = new MatchVisitor() {
            @Override
            public void matched(TextModel ntext1, TextModel ntext2, int offset1, int offset2, int length1, int length2) {
                assertNotSame(ntext1, ntext2);
                assertSame(ntext1, text1);
                assertSame(ntext2, text2);
                assertEquals(offset1, offset2);
                assertFalse(offsets[offset1]);
                offsets[offset1] = true;
            }
        };

        RadixSearch.search(visitor, texts, depth);

        for (int i = 0; i < offsets.length; i++)
            assertTrue(offsets[i]);
    }
}