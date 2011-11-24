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

import static com.dnikulin.vijil.index.TextModelTape.BARRIER;
import static com.dnikulin.vijil.index.TextModelTape.SENTINEL;
import static com.dnikulin.vijil.index.TextModelTape.VOID;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dnikulin.vijil.model.TextModel;

public class TextModelTapeTest {
    @Test
    public void testEmptyTape() {
        TextModelTape tape = new TextModelTape(TextModel.none);
        assertEquals(0, tape.nmodels);
        assertEquals(1, tape.nsymbols);
        assertEquals(SENTINEL, tape.symbol(0));
        assertEquals(VOID, tape.modelIndex(0));
        assertEquals(VOID, tape.modelOffset(0));
    }

    @Test
    public void testSmallTape() {
        final TextModel    model1 = MindexTest.makeBody();
        final TextModel    model2 = MindexTest.makeBody();
        final TextModel [] models = new TextModel[]{model1, model2};

        TextModelTape tape = new TextModelTape(models);
        assertEquals(2, tape.nmodels);
        assertEquals(model1.size + model2.size + 3, tape.nsymbols);

        assertEquals(SENTINEL, tape.symbol(tape.nsymbols - 1));
        assertEquals(VOID, tape.modelIndex(tape.nsymbols - 1));
        assertEquals(VOID, tape.modelOffset(tape.nsymbols - 1));

        assertEquals(BARRIER, tape.symbol(model1.size));
        assertEquals(VOID, tape.modelIndex(model1.size));
        assertEquals(VOID, tape.modelOffset(model1.size));

        assertEquals(0, tape.modelIndex(0));
        assertEquals(1, tape.modelIndex(tape.nsymbols - 3));
    }
}