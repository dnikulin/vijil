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

import java.util.Arrays;

import com.dnikulin.vijil.model.TextModel;

public final class TextModelTape {
    /**
     * Sentinel value, exactly 0, to be lexicographically less than every symbol.
     *
     * Some algorithms strictly require this to be the last symbol in the array.
     */
    public static final int SENTINEL =  0;

    /**
     * Barrier value, exactly 1, inserted after each text model in the tape.
     *
     * This helps algorithms to check if a span actually crosses between models,
     * and is thus invalid.
     *
     * The same could be done by comparing model(im1) and model(im2), but
     * doing so requires further memory indirections.
     */
    public static final int BARRIER  =  1;

    /**
     * Void index value, exactly -1, considered the model and symbol index
     * for each barrier and the sentinel.
     */
    public static final int VOID     = -1;

    private final TextModel [] models;
    private final int       [] symbols;
    private final int       [] imodels;
    private final int       [] isymbols;

    public  final int          nmodels;
    public  final int          nsymbols;
    public  final int          maximum;

    public TextModelTape(TextModel [] models) {
        // Defensively copy model array.
        this.models   = Arrays.copyOf(models, models.length);

        // Count number of models and symbols with barriers and sentinel.
        this.nmodels  = this.models.length;
        this.nsymbols = totalSymbols(this.models);

        // Find maximum symbol integer.
        this.maximum  = maxSymbol(this.models);

        // Allocate arrays spanning all symbols with padding.
        this.symbols  = new int [this.nsymbols];
        this.imodels  = new int [this.nsymbols];
        this.isymbols = new int [this.nsymbols];

        // Populate arrays.
        int cursor = 0;
        for (int imodel = 0; imodel < this.nmodels; imodel++) {
            final TextModel model = this.models[imodel];

            for (int isymbol = 0; isymbol < model.size; isymbol++, cursor++) {
                // Read symbol at this symbol index.
                this.symbols  [cursor] = model.symbol(isymbol);

                // Record the model index and symbol index.
                this.imodels  [cursor] = imodel;
                this.isymbols [cursor] = isymbol;
            }

            // Record a barrier symbol.
            this.symbols  [cursor] = BARRIER;
            this.imodels  [cursor] = VOID;
            this.isymbols [cursor] = VOID;

            // Count the barrier towards the output index.
            cursor += 1;
        }

        // Record a sentinel symbol.
        this.symbols  [cursor] = SENTINEL;
        this.imodels  [cursor] = VOID;
        this.isymbols [cursor] = VOID;

        // Count the sentinel towards the output index.
        cursor += 1;

        // Check the full size was recorded.
        assert(cursor == this.nsymbols);
    }

    public TextModel model(int im) {
        return models[im];
    }

    public int symbol(int is) {
        return symbols[is];
    }

    public int modelIndex(int is) {
        return imodels[is];
    }

    public int modelOffset(int is) {
        return isymbols[is];
    }

    public static int totalSymbols(TextModel [] models) {
        // Count sentinel.
        int nsymbols = 1;

        // Count symbols from each model.
        for (TextModel model : models) {
            // Count symbols.
            nsymbols += model.size;

            // Count barrier.
            nsymbols += 1;
        }

        return nsymbols;
    }

    public static int maxSymbol(TextModel [] models) {
        int max = 0;

        for (TextModel model : models) {
            for (int isymbol = 0; isymbol < model.size; isymbol++)
                max = Math.max(max, model.symbol(isymbol));
        }

        return max;
    }
}
