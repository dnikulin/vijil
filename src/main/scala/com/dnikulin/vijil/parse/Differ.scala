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

package com.dnikulin.vijil.parse

import scala.collection.JavaConversions._

import org.incava.util.diff.Diff
import org.incava.util.diff.Difference

import com.dnikulin.vijil.text.TextFile
import com.dnikulin.vijil.text.TextSpan
import com.dnikulin.vijil.tools.CleanString
import com.dnikulin.vijil.tools.CostModel
import com.dnikulin.vijil.tools.MinCostEdit

object DifferTokens {
  def apply(root: StringSpan, spans: Array[StringSpan]): DifferTokens =
    apply(root, spans, s => s)

  def apply(root: StringSpan, spans: Array[StringSpan], clean: (String => String)): DifferTokens = {
    val words = spans.map(s => clean(s.substring).trim).filter(_.length > 0)
    val cmins = spans.map(_.min)
    val cmaxs = spans.map(_.max)
    return new DifferTokens(root, words, cmins, cmaxs)
  }
}

class DifferTokens(val root: StringSpan, val words: Array[String],
                   val cmins: Array[Int], val cmaxs: Array[Int]) {

  val nwords = words.length

  private def cap(w: Int): Int =
    ((w min (words.length - 1)) max 0)

  def span(wmin: Int, wmax: Int): StringSpan =
    root.cut(cmins(cap(wmin)), cmaxs(cap(wmax)))

  def span1(wmin: Int, wmax: Int): StringSpan =
    root.cut(cmins(cap(wmin)), cmins(cap(wmin)) + 1)

  def slice(wmin: Int, wmax: Int): DifferTokens =
    new DifferTokens(span(wmin, wmax), words.slice(wmin, wmax), cmins.slice(wmin, wmax), cmaxs.slice(wmin, wmax))
}

object Differ {
  sealed trait Change
  case object  Add extends Change
  case object  Del extends Change
  case object  Mod extends Change
  case class   Changed(span1: StringSpan, span2: StringSpan, action: Change)

  type PerDiff = PartialFunction[Changed, Unit]

  def apply(text1: TextFile, text2: TextFile)(fdiff: PerDiff) {
    val span1 = TextSpan(text1)
    val span2 = TextSpan(text2)
    apply(span1, span2)(fdiff)
  }

  def apply(root1: StringSpan, root2: StringSpan)(fdiff: PerDiff) {
    import FindSpans.sentences
    import FindSpans.wordBounds

    // Split root spans into sentences.
    val sents1 = DifferTokens(root1, sentences(root1).toArray)
    val sents2 = DifferTokens(root2, sentences(root2).toArray)

    // Run diff at sentence level.
    apply(sents1, sents2) {
      // Resolve sentence edits at word level.
      case Changed(span1, span2, Mod) =>
        // Split sentence spans into words.
        val words1 = DifferTokens(root1, wordBounds(span1).toArray)
        val words2 = DifferTokens(root2, wordBounds(span2).toArray)

        // Run diff at word level.
        apply(words1, words2)(fdiff)

      // Pass through add or delete.
      case diff => fdiff(diff)
    }
  }

  def apply(span1: DifferTokens, span2: DifferTokens)(fdiff: PerDiff) {
    if ((span1.words.length < 1) && (span2.words.length > 0)) {
      fdiff(Changed(span1.root, span2.root, Add))
      return
    } else if (span2.words.length < 1) {
      fdiff(Changed(span1.root, span2.root, Del))
      return
    }

    // Run diff between the two arrays.
    val diffs = new Diff(span1.words, span2.words).diff()

    for (diff <- diffs) {
      import Difference._
      import diff._

      Array(getAddedStart, getAddedEnd, getDeletedStart, getDeletedEnd) match {
        case Array(add0, NONE, del0, del1) =>
          // End must be after start.
          assert(del1 >= del0)
          assert(span1.words.length >= del1)

          // Issue callback with deletion.
          val dspan1 = span1.span (del0, del1)
          val dspan2 = span2.span1(add0, add0)
          fdiff(Changed(dspan1, dspan2, Del))

        case Array(add0, add1, del0, NONE) =>
          // End must be after start.
          assert(add1 >= add0)
          assert(span2.words.length >= add1)

          // Issue callback with insertion.
          val dspan1 = span1.span1(del0, del0)
          val dspan2 = span2.span (add0, add1)
          fdiff(Changed(dspan1, dspan2, Add))

        case Array(add0, add1, del0, del1) =>
          // End must be after start.
          assert(del1 >= del0)
          assert(add1 >= add0)
          assert(span1.words.length >= del1)
          assert(span2.words.length >= add1)

          // Issue callback with replacement.
          val dspan1 = span1.span(del0, del1)
          val dspan2 = span2.span(add0, add1)
          fdiff(Changed(dspan1, dspan2, Mod))

        case _ =>
      }
    }
  }

  def atoms(span1: DifferTokens, span2: DifferTokens)(fdiff: PerDiff) {
    if ((span1.words.length < 1) && (span2.words.length > 0)) {
      fdiff(Changed(span1.root, span2.root, Add))
      return
    } else if (span2.words.length < 1) {
      fdiff(Changed(span1.root, span2.root, Del))
      return
    }

    // Run diff between the two arrays.
    val diffs = new Diff(span1.words, span2.words).diff()

    for (diff <- diffs) {
      import Difference._
      import diff._

      Array(getAddedStart, getAddedEnd, getDeletedStart, getDeletedEnd) match {
        case Array(add0, NONE, del0, del1) =>
          // End must be after start.
          assert(del1 >= del0)
          assert(span1.words.length >= del1)

          // Issue callback with deletion.
          val dspan2 = span2.span1(add0, add0)
          for (i <- (del0 to del1)) {
            val dspan1 = span1.span(i, i)
            fdiff(Changed(dspan1, dspan2, Del))
          }

        case Array(add0, add1, del0, NONE) =>
          // End must be after start.
          assert(add1 >= add0)
          assert(span2.words.length >= add1)

          // Issue callback with insertion.
          val dspan1 = span1.span1(del0, del0)
          for (i <- (add0 to add1)) {
            val dspan2 = span2.span(i, i)
            fdiff(Changed(dspan1, dspan2, Add))
          }

        case Array(add0, add1, del0, del1) =>
          // End must be after start.
          assert(del1 >= del0)
          assert(add1 >= add0)
          assert(span1.words.length >= del1)
          assert(span2.words.length >= add1)

          // Issue callback with replacement.
          val dspan1 = span1.span(del0, del1)
          val dspan2 = span2.span(add0, add1)
          fdiff(Changed(dspan1, dspan2, Mod))

        case _ =>
      }
    }
  }

  def minCostAlign(span1: DifferTokens, span2: DifferTokens)(fdiff: PerDiff)(implicit model: CostModel[String]) {
    import MinCostEdit._

    val plan = MinCostEdit[String](span1.words, span2.words)(model)

    plan.steps.foreach {
      case Modify(i1, i2) =>
        // Recall words at these positions.
        val s1 = span1.words(i1)
        val s2 = span2.words(i2)

        // Check that the words are actually different.
        if (model.pair(s1, s2) > 0)
          fdiff(Changed(span1.span(i1, i1), span2.span(i2, i2), Mod))

      case Remove(i1, i2) =>
        fdiff(Changed(span1.span(i1, i1), span2.span(i2, i2), Del))

      case Insert(i1, i2) =>
        fdiff(Changed(span1.span(i1, i1), span2.span(i2, i2), Add))
    }
  }

  def minCostExtents(span1: DifferTokens, span2: DifferTokens)(fdiff: PerDiff)(implicit model: CostModel[String]) {
    import MinCostEdit._

    def isEdit(i1: Int, i2: Int): Boolean = {
      // Recall words at these positions.
      val s1 = span1.words(i1)
      val s2 = span2.words(i2)

      // Check that the words are actually different.
      (model.pair(s1, s2) > 0)
    }

    // Compute min-cost edit plan.
    val plan = MinCostEdit[String](span1.words, span2.words)(model)

    // Treat steps in plan as step queue.
    var queue = plan.steps.sortBy{
      case Modify(i1, i2) => (i1, i2, 0)
      case Insert(i1, i2) => (i1, i2, 1)
      case Remove(i1, i2) => (i1, i2, 2)
    }

    while (queue.isEmpty == false) {
      queue.head match {
        case Modify(i1_0, i2_0) =>
          if (isEdit(i1_0, i2_0)) {
            // Start counting extent.
            var i1_1 = i1_0
            var i2_1 = i2_0

            queue = queue.dropWhile {
              // Take any non-benign Modify.
              case Modify(i1_n, i2_n) if (isEdit(i1_n, i2_n)) =>
                // Update extent.
                i1_1 = i1_n
                i2_1 = i2_n
                // Drop and continue.
                true

              case _ =>
                // Stop dropping.
                false
            }

            // Verify extent.
            assert(i1_1 >= i1_0)
            assert(i2_1 >= i2_0)

            // Release extent.
            fdiff(Changed(span1.span(i1_0, i1_1), span2.span(i2_0, i2_1), Mod))
          } else {
            // Drop benign Modify.
            queue = queue.tail
          }

        case Insert(i1_0, i2_0) =>
          // Start counting extent.
          var i2_1 = i2_0

          queue = queue.dropWhile {
            // Take any Insert.
            case Insert(i1_n, i2_n) =>
              // Update extent.
              assert(i1_n == i1_0)
              i2_1 = i2_n
              // Drop and continue.
              true

            case _ =>
              // Stop dropping.
              false
          }

          // Verify extent.
          assert(i2_1 >= i2_0)

          // Release extent.
          fdiff(Changed(span1.span(i1_0, i1_0), span2.span(i2_0, i2_1), Add))

        case Remove(i1_0, i2_0) =>
          // Start counting extent.
          var i1_1 = i1_0

          queue = queue.dropWhile {
            // Take any Remove.
            case Remove(i1_n, i2_n) =>
              // Update extent.
              assert(i2_n == i2_0)
              i1_1 = i1_n
              // Drop and continue.
              true

            case _ =>
              // Stop dropping.
              false
          }

          // Verify extent.
          assert(i1_1 >= i1_0)

          // Release extent.
          fdiff(Changed(span1.span(i1_0, i1_1), span2.span(i2_0, i2_0), Del))
      }
    }
  }
}
