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

import com.dnikulin.vijil.model.TextModel;

public final class SlowSearch {
    public static void search(MatchVisitor visitor, TextModel[] models, int depth) {
        final int nmodels = models.length;

        for (int imodel1 = 0; imodel1 < nmodels; imodel1++) {
            final TextModel model1 = models[imodel1];

            for (int imodel2 = (imodel1 + 1); imodel2 < nmodels; imodel2++) {
                final TextModel model2 = models[imodel2];

                for (int i1 = 0; i1 <= (model1.size - depth); i1++) {
                    for (int i2 = 0; i2 <= (model1.size - depth); i2++)
                        search(visitor, model1, model2, i1, i2, depth);
                }
            }
        }
    }

    public static void search(MatchVisitor visitor, TextModel model1, TextModel model2, int i1, int i2, int depth) {
        for (int j = 0; j < depth; j++) {
            // Check symbols at these positions.
            if (model1.symbol(i1) != model2.symbol(i2))
                return;
        }

        // Match verified.
        visitor.matched(model1, model2, i1, i2, depth, depth);
    }

    private SlowSearch() {}
}
