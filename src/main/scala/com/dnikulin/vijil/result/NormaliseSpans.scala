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

package com.dnikulin.vijil.result

import com.dnikulin.vijil.model.TextModel

object NormaliseSpans {
  def apply(texts: Seq[TextModel], sets: Array[LinkSpanSet]): Array[LinkSpanSet] = {
    // Normalise from symbol domain to character domain.
    val mapper = new LinkSpanGraph(SpanDomain.CHARACTERS)
    mapper.normalise(sets, texts.toArray)
    return mapper.result
  }
}
