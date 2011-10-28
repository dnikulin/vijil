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
import com.dnikulin.vijil.result.LinkSpanGraph
import com.dnikulin.vijil.result.LinkSpanSet
import com.dnikulin.vijil.result.SpanDomain

object SearchSet {
  def apply(texts: Array[TextModel], index: MindexCore): Array[LinkSpanSet] = {
    // Sort so that larger texts come last.
    // The largest text won't be indexed at all.
    val stexts = texts.sortWith(_.size < _.size)

    val buffer = new LinkSpanGraph(SpanDomain.SYMBOLS)

    var itext = 0
    while (itext < stexts.length) {
      val text = stexts(itext)

      // Search against previous texts.
      if (itext > 0)
        index.search(text, buffer)

      // Advance array counter.
      itext += 1

      // Add to index.
      if (itext < stexts.length)
        index.add(text)
    }

    return buffer.result
  }

  def apply(texts: Array[TextModel], size: Int): Array[LinkSpanSet] =
    apply(texts, new Mindex(size))

  def apply(texts: Array[TextModel], model: StencilModel): Array[LinkSpanSet] =
    apply(texts, new Stencils(model))
}
