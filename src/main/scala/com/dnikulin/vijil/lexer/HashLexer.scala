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

package com.dnikulin.vijil.lexer

import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.model.TextModelBuilder
import com.dnikulin.vijil.parse.FindSpans
import com.dnikulin.vijil.text.TextFile

class HashLexer(val offset: Int, val length: Int) extends SimpleTextLexer {
  require(offset >= 0)
  require(length >= 1)

  val extent = (offset + length)
  require(extent >= 0)
  require(extent >  offset)
  require(extent >= length)

  override def apply(text: TextFile, builder: TextModelBuilder): Unit = {
    for (span <- FindSpans.words(text.spans.head)) {
      if (extent <= span.len) {
        val cmin = span.min
        val cmax = span.max
        val clen = cmax - cmin

        assert(cmin >= 0)
        assert(cmax >  cmin)
        assert(clen >  0)

        val data = span.data.substring(cmin + offset, cmin + extent)
        val hash = data.toLowerCase.hashCode

        builder.add(hash, cmin, clen.toByte)
      }
    }
  }
}
