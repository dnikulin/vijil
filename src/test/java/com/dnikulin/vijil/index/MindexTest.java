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

import com.dnikulin.vijil.model.TextModelBuilder;
import com.dnikulin.vijil.model.TextModel;

import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MindexTest {
    public static final int nlemmas = 17;
    public static final int nentries = nlemmas - 5;

    @Test
    public void testMatchMindex() {
        final TextModel text1 = makeBody();
        final TextModel text2 = makeBody();
        final Mindex   index = new Mindex(6);
        index.add(text2);

        final boolean offsets[] = new boolean[nentries];

        index.search(text1, new MatchVisitor() {
            @Override
            public void matched(TextModel ntext1, TextModel ntext2, int offset1, int offset2, int length1, int length2) {
                assertNotSame(ntext1, ntext2);
                assertSame(ntext1, text1);
                assertSame(ntext2, text2);
                assertEquals(offset1, offset2);
                assertFalse(offsets[offset1]);
                offsets[offset1] = true;
            }
        });

        for (int i = 0; i < offsets.length - 2; i++)
            assertTrue(offsets[i]);
    }

    public static TextModel makeBody() {
        TextModelBuilder builder = new TextModelBuilder();
        assertEquals(builder.length(), 0);

        Random random = new Random(7);

        for (int i = 0; i < nlemmas; i++) {
            int code = i ^ (random.nextInt() & 1931);
            builder.add(code, i, (byte) 1);
        }

        return builder.finish("hash");
    }
}