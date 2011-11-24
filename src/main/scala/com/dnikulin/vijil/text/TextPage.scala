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
import com.dnikulin.vijil.traits.Rune

/**
 * A page break the starts a given page number at a given string data position.
 *
 * Use one per page directly in a TextFile, then TextPage.makePages()
 * can interpret the runes to create the correct original page numbers.
 */
case class PageBreak(number: Int, position: Int) extends Rune

case class TextPage(text: TextFile, number: Int, minLeaf: Int, maxLeaf: Int) extends HasHash {
  require(number  >= 1)
  require(minLeaf >= 0)
  require(maxLeaf >= minLeaf) // Allow zero leaves, but not "negative" number of leaves.
  require(maxLeaf <= text.leaves.length)

  val leaves = text.leaves.slice(minLeaf, maxLeaf)
  val cmin   = text.leaves(minLeaf).min
  val cmax   = text.leaves((maxLeaf - 1) max 0).max max cmin
  val span   = TextSpan(text, cmin, cmax)

  override val hash = ("t%s_p%d".format(text.hash, number))
}

object TextPage {
  val none = new Array[TextPage](0)

  def makePages(text: TextFile, clen: Int = 2000): IndexedSeq[TextPage] = {
    // Find page break runes within the text.
    val breaks = text.runesOfClass[PageBreak]

    // If page breaks were found, use them.
    if (breaks.isEmpty) makePagesByLength(text, clen)
    else                makePagesByBreaks(text, breaks)
  }

  def makePagesByLength(text: TextFile, clen: Int = 2000): IndexedSeq[TextPage] = {
    val pages   = new ArrayBuilder.ofRef[TextPage]

    var number  = 1
    var length  = 0
    var minLeaf = 0

    val leaves = text.leaves.toArray

    for ((leaf, index) <- leaves.zipWithIndex) {
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

  def makePagesByBreaks(text: TextFile, breaks: Seq[PageBreak]): IndexedSeq[TextPage] = {
    import System.err.{println => err}

    // Sort pages by position in string data.
    val sorted = breaks.toArray.sortWith(_.position < _.position)

    // Keep leaves in an array.
    val leaves = text.leaves.toArray

    // Verify all page pairs for consistency.
    sorted.reduce{(p1,p2) =>
      if ((p1.number + 1) != p2.number) {
        err("Text [%s] has page number [%d] followed by [%d]".
            format(text.name, p1.number, p2.number))
      }

      if (p1.position >= p2.position) {
        err("Text [%s] has position [%d] for page [%d] followed by [%d] for page [%d]".
            format(text.name, p1.position, p1.number, p2.position, p2.number))
      }

      // Pass page 2 for next step of 'reduce'.
      p2
    }

    // Zip breaks against leaves.
    val ileaves = leavesForBreaks(sorted, leaves)

    // The number of pages is the same as the number of breaks.
    for ((break, idx) <- sorted.zipWithIndex) yield {
      // Calculate minimum and maximum leaf for this page.
      // leavesForBreaks() makes the first and last pages extend to the bounds.
      val minLeaf = ileaves(idx)
      val maxLeaf = ileaves(idx + 1)

      // Create the page.
      // Throw away the number, and use the raw index instead.
      TextPage(text, idx + 1, minLeaf, maxLeaf)
    }
  }

  private def leavesForBreaks(breaks: Array[PageBreak], leaves: Array[TextSpan]): Array[Int] = {
    val ileaves = new ArrayBuilder.ofInt

    // Record lower leaf bound.
    // This makes the first page extend to the start.
    ileaves += 0

    // Skip first page break.
    // First leaves may be skipped automatically, see below.
    var ib = 1
    var il = 1

    while ((ib < breaks.length) && (il < leaves.length)) {
      // Advance in leaves while the start is still before a page break.
      while ((il < leaves.length) && (leaves(il).max < breaks(ib).position))
        il += 1

      if ((ib < breaks.length) && (il < leaves.length)) {
        // Record the leaf index.
        ileaves += il

        // Advance in pages exactly once.
        ib += 1
      }
    }

    // Record upper leaf bound.
    // This makes the last page extend to the end.
    ileaves += leaves.length

    return ileaves.result
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
