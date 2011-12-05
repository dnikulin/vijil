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

package com.dnikulin.vijil.index

import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.result._

object SearchLinkCloud {
  def apply(text1: TextModel, text2: TextModel, index: MindexCore): Array[ModelSpanPair] = {
    // Always invoke with text1 as larger.
    if (text1.size < text2.size)
      return apply(text2, text1, index)

    // Create span merger.
    val buffer = new SpanMerger(text1, text2)

    // Populate index.
    index.add(text2)

    // Search index.
    index.search(text1, buffer)

    // Return linked spans.
    return buffer.result
  }

  def apply(text1: TextModel, text2: TextModel, size: Int): Array[ModelSpanPair] =
    apply(text1, text2, new Mindex(size))

  def apply(text1: TextModel, text2: TextModel, model: StencilModel): Array[ModelSpanPair] =
    apply(text1, text2, new Stencils(model))
}
