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

import scala.collection.mutable.HashMap

import com.dnikulin.vijil.model._
import com.dnikulin.vijil.text._
import com.dnikulin.vijil.tools._

protected final case class Expander(text: TextModel, span: LinkSpan, size: Int) {
  require(span.set.domain == SpanDomain.SYMBOLS)

  final val lmin0 = (span.min - size) max 0
  final val lmax0 = (span.max + size) min text.size
  final val set   = new Integers

  /* Add all lemma codes to the integer set. */ {
    var i = lmin0
    while (i < lmax0) {
      set.add(text.symbol(i))
      i += 1
    }
    set.makeSet()
  }

  final var lmin1 = span.min
  final var lmax1 = span.max

  var node : LinkSpan = span

  def update(set2: LinkSpanSet) {
    node = new LinkSpan(set2, span.code, span.hash, lmin1, lmax1, span.links)
  }
}

object ExpandSymbols {
  def apply(texts: Array[TextModel], sets: Array[LinkSpanSet], size: Int): Array[LinkSpanSet] = {
    if (size < 1)
      return sets

    val links = new HashMap[Int, Expander]
    val osets = new Array[LinkSpanSet](sets.length)

    var iset = 0
    while (iset < sets.length) {
      val set    = sets(iset)
      val spans  = set.spans
      val expans = new Array[Expander](spans.length)

      var ispan1 = 0
      while (ispan1 < spans.length) {
        val span = spans(ispan1)

        var itext = 0
        while (itext < texts.length) {
          val text = texts(itext)
          if (text.hash == span.hash) {
            links(span.code) = Expander(text, span, size)
            itext = texts.length
          }
          itext += 1
        }
        ispan1 += 1
      }

      ispan1 = 0
      while (ispan1 < spans.length) {
        val span1  = spans(ispan1)
        val expan1 = links(span1.code)

        var ispan2 = 0
        while (ispan2 < span1.links.length) {
          val span2  = set.spans(span1.links(ispan2))
          val expan2 = links(span2.code)

          if (expan1.span.hash != expan2.span.hash)
            expandMatch(expan1, expan2)

          ispan2 += 1
        }
        ispan1 += 1
      }

      val oset = new LinkSpanSet(set.code, spans.length, SpanDomain.SYMBOLS)
      osets(iset) = oset

      ispan1 = 0
      while (ispan1 < spans.length) {
        val span1  = spans(ispan1)
        val expan1 = links(span1.code)
        expan1.update(oset)

        oset.spans(ispan1) = expan1.node
        ispan1 += 1
      }

      links.clear()
      iset += 1
    }

    return osets
  }

  private def expandMatch(task: Expander, base: Expander) {
    if (task.lmin1 > task.lmin0)
      expandMin(task, base)

    if (task.lmax1 < task.lmax0)
      expandMax(task, base)
  }

  private def expandMin(task: Expander, base: Expander) {
    // Start from minimum possible extent of the span.
    var lmin = task.lmin0

    // Shrink towards minimum known extent of the span.
    while (lmin < task.lmin1) {
      // If the lemma matches, update the known extent and finish.
      val lemma = task.text.symbol(lmin)
      if (base.set.contains(lemma)) {
        task.lmin1 = lmin
        return
      }

      lmin += 1
    }
  }

  private def expandMax(task: Expander, base: Expander) {
    // Start from maximum possible extent of the span.
    var lmax = task.lmax0

    // Shrink towards maximum known extent of the span.
    while (lmax > task.lmax1) {
      // If the lemma matches, update the known extent and finish.
      val lemma = task.text.symbol(lmax - 1)
      if (base.set.contains(lemma)) {
        task.lmax1 = lmax
        return
      }

      lmax -= 1
    }
  }
}
