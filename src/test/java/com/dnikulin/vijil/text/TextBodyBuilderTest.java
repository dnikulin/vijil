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

package com.dnikulin.vijil.text;

import com.dnikulin.vijil.model.TextModelBuilder;
import com.dnikulin.vijil.model.TextModel;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TextBodyBuilderTest {
    @Test
    public void testEmpty() {
        TextModelBuilder builder = new TextModelBuilder();
        assertEquals(builder.length(), 0);
    }

    @Test
    public void testAdd() {
        TextModelBuilder builder = new TextModelBuilder();
        assertEquals(builder.length(), 0);

        builder.add(1, 2, (byte) 3);
        assertEquals(builder.length(), 1);

        builder.add(4, 5, (byte) 6);
        assertEquals(builder.length(), 2);
    }

    @Test
    public void testReset() {
        TextModelBuilder builder = new TextModelBuilder();
        assertEquals(builder.length(), 0);

        builder.add(1, 2, (byte) 3);
        assertEquals(builder.length(), 1);

        builder.add(4, 5, (byte) 6);
        assertEquals(builder.length(), 2);

        builder.reset();
        assertEquals(builder.length(), 0);
    }

    @Test
    public void testFinish() {
        TextModelBuilder builder = new TextModelBuilder();
        assertEquals(builder.length(), 0);

        builder.add(1, 2, (byte) 3);
        assertEquals(builder.length(), 1);

        builder.add(4, 5, (byte) 6);
        assertEquals(builder.length(), 2);

        TextModel text = builder.finish("hash1");
        assertEquals(text.size,  2);

        assertEquals(text.symbol(0),  1);
        assertEquals(text.offset(0), 2);
        assertEquals(text.length(0), 3);

        assertEquals(text.symbol(1),  4);
        assertEquals(text.offset(1), 5);
        assertEquals(text.length(1), 6);

        builder.reset();
        assertEquals(builder.length(), 0);

        TextModel etext = builder.finish("hash2");
        assertEquals(etext.size,  0);
    }
}