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

package com.dnikulin.vijil.text

import scala.collection.mutable.ArrayBuilder
import scala.collection.mutable.BitSet

import com.dnikulin.vijil.parse.StringSpan
import com.dnikulin.vijil.traits.HasHash

case class TextPage(text: TextFile, number: Int, minLeaf: Int, maxLeaf: Int) extends HasHash {
  require(number  >= 1)
  require(minLeaf >= 0)
  require(maxLeaf >  minLeaf)
  require(maxLeaf <= text.leaves.length)

  val leaves = text.leaves.slice(minLeaf, maxLeaf)
  val cmin   = leaves.head.min
  val cmax   = leaves.last.max
  val span   = TextSpan(text, cmin, cmax)

  override val hash = ("t%s_p%d".format(text.hash, number))
}

object TextPage {
  val none = new Array[TextPage](0)

  def makePages(text: TextFile, clen: Int = 2000): IndexedSeq[TextPage] = {
    val pages   = new ArrayBuilder.ofRef[TextPage]

    var number  = 1
    var length  = 0
    var minLeaf = 0

    for ((leaf, index) <- text.leaves.zipWithIndex) {
      // Count leaf itself as 100 characters.
      length += 100

      // Count span characters.
      length += leaf.len

      if (length >= clen) {
        val maxLeaf = index + 1
        pages      += TextPage(text, number, minLeaf, maxLeaf)
        number     += 1
        minLeaf     = maxLeaf
        length      = 0
      }
    }

    if (length > 0) {
      val maxLeaf   = text.leaves.length
      pages        += TextPage(text, number, minLeaf, maxLeaf)
    }

    return pages.result
  }

  def find(span: TextSpan): Option[TextPage] = {
    for (text <- span.findText;
         page <- find(text.pages, span.min))
      yield page
  }

  def find(pages: IndexedSeq[TextPage], positions: Array[Int]): BitSet = {
    val marks = new BitSet
    find(marks, pages, positions)
    return marks
  }

  def find(pages: IndexedSeq[TextPage], spans: IndexedSeq[StringSpan]): BitSet = {
    val marks = new BitSet
    find(marks, pages, spans)
    return marks
  }

  def find(marks: BitSet, pages: IndexedSeq[TextPage], positions: Array[Int]) {
    var pi = 0
    var ci = 0

    while ((pi < pages.length) && (ci < positions.length)) {
      // Advance in positions until matching.
      while ((ci < positions.length) && (positions(ci) < pages(pi).cmin))
        ci += 1

      if (ci < positions.length) {
        // If matching now, record the match.
        if (pages(pi).span.includes(positions(ci))) {
          marks.add(pages(pi).number)
          pi += 1
        }

        // Advance in pages until matching.
        while ((pi < pages.length) && (positions(ci) >= pages(pi).cmax))
          pi += 1
      }
    }
  }

  def find(marks: BitSet, pages: IndexedSeq[TextPage], spans: IndexedSeq[StringSpan]) {
    var pi = 0
    var ci = 0

    while ((pi < pages.length) && (ci < spans.length)) {
      // Advance in spans until matching.
      while ((ci < spans.length) && (spans(ci).max < pages(pi).cmin))
        ci += 1

      if (ci < spans.length) {
        // Advance in pages while matching.
        while ((pi < pages.length) && pages(pi).span.includes(spans(ci))) {
          marks.add(pages(pi).number)
          pi += 1
        }

        // Advance in pages until matching.
        while ((pi < pages.length) && (spans(ci).min >= pages(pi).cmax))
          pi += 1
      }
    }
  }

  def find(pages: IndexedSeq[TextPage], position: Int): Option[TextPage] = {
    var imin = 0
    var imax = pages.length - 1

    while ((imax - imin) > 8) {
      val imid = (imin + ((imax - imin) >> 1))
      val page = pages(imid)

      if (page.cmin > position) {
        imax = imid - 1
      } else if (page.cmax <= position) {
        imin = imid + 1
      } else {
        return Some(page)
      }
    }

    while (imin <= imax) {
      val page = pages(imin)
      if ((page.cmin <= position) && (position < page.cmax))
        return Some(page)
      imin += 1
    }

    return None
  }
}

trait HasTextPages {
  val pages: IndexedSeq[TextPage]
}
