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
  val domain = SpanDomain.CHARACTERS

  def apply(texts: Seq[TextModel], sets: Array[LinkSpanSet]): Array[LinkSpanSet] = {
    // Normalise from symbol domain to character domain.
    val mapper = new LinkSpanGraph(domain)
    mapper.normalise(sets, texts.toArray)
    return mapper.result
  }

  def apply(texts: Seq[TextModel], pairs: Array[ModelSpanPair]): Array[ModelSpanPair] =
    pairs.flatMap(apply(texts, _))

  def apply(texts: Seq[TextModel], pair: ModelSpanPair): Option[ModelSpanPair] = {
    for (span1 <- apply(texts, pair.span1);
         span2 <- apply(texts, pair.span2))
      yield new ModelSpanPair(span1, span2, pair.code)
  }

  def apply(texts: Seq[TextModel], span: ModelSpan): Option[ModelSpan] = {
    assert(span.domain == SpanDomain.SYMBOLS)
    for (text <- texts.find(_.hash == span.hash)) yield {
      val cmin = text.minChar(span.min)
      val cmax = text.maxChar(span.max - 1)
      val clen = (cmax - cmin)
      new ModelSpan(span.hash, domain, span.code, cmin, clen)
    }
  }
}
