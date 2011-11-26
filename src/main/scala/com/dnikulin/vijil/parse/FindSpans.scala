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

import scala.collection.mutable.ArraySeq

import java.text.BreakIterator
import java.util.regex.Pattern

object FindSpans {
  val paragraphPattern = Pattern.compile("(\\s*\r?\n\\s*\r?\n\\s*)")
  val wordPattern      = Pattern.compile("([a-zA-Z][a-zA-Z0-9'\\-]*)")
  val wordNumPattern   = Pattern.compile("([a-zA-Z0-9'\\-]+)")
  val wordBoundPattern = Pattern.compile("(\\b|\\s+)")
  val sentencePattern  = Pattern.compile("[\\.!?]['\"]?(\\s+)[A-Z\\-'\"]")

  def words(root: StringSpan): IndexedSeq[StringSpan] =
    each(root, wordPattern)

  def sentences(root: StringSpan): IndexedSeq[StringSpan] =
    rejoinSentences(sentencesSimple(root))

  def sentencesSimple(root: StringSpan): IndexedSeq[StringSpan] =
    split(root, sentencePattern)

  def wordsNumbers(root: StringSpan): IndexedSeq[StringSpan] =
    each(root, wordNumPattern)

  def paragraphs(root: StringSpan): IndexedSeq[StringSpan] =
    split(root, paragraphPattern)

  def wordBounds(root: StringSpan): IndexedSeq[StringSpan] =
    split(root, wordBoundPattern)

  def split(root: StringSpan, bounds: BreakIterator): IndexedSeq[StringSpan] = {
    val spans = ArraySeq.newBuilder[StringSpan]

    // Assign string data for iterator.
    bounds.setText(root.substring)

    // Initial span start marker, from start of root span.
    var cmin = root.min

    // Boundary between sentences.
    var bound = bounds.first

    while (bound != BreakIterator.DONE) {
      // Calculate boundary index in full string data.
      val cmax = (root.min + bound)

      // Create span for any non-white substring.
      if (cmax > cmin) {
        val span = root.cut(cmin, cmax).trim
        if (span.len > 0)
          spans += span
      }

      // Move span start marker.
      cmin = cmax

      // Find next boundary.
      bound = bounds.next()
    }

    return spans.result
  }

  def each(root: StringSpan, pattern: Pattern): IndexedSeq[StringSpan] = {
    val spans = ArraySeq.newBuilder[StringSpan]

    // Create matcher to iterate over splits.
    val matcher = pattern.matcher(root.substring)

    while (matcher.find) {
      // Calculate indices in full string data.
      val cmin = root.min + matcher.start(1)
      val cmax = root.min + matcher.end(1)

      // Record span with full string data.
      spans   += root.cut(cmin, cmax).trim
    }

    return spans.result
  }

  def split(root: StringSpan, pattern: Pattern): IndexedSeq[StringSpan] = {
    val spans = ArraySeq.newBuilder[StringSpan]

    // Create matcher to iterate over splits.
    val matcher = pattern.matcher(root.substring)

    // Initial span start marker, from start of root span.
    var cmin = root.min

    while (matcher.find) {
      // Find extent of the splitter.
      val smin = matcher.start(1)
      val smax = matcher.end(1)

      // Calculate boundary index in full string data.
      // This is for the *previous* span, if any.
      val cmax = (root.min + smin)

      // Create span for any non-white substring.
      if (cmax > cmin) {
        val span = root.cut(cmin, cmax).trim
        if (span.len > 0)
          spans += span
      }

      // Move span start marker.
      cmin = root.min + smax
    }

    // Create span for any non-white substring at the end.
    if (root.max > cmin) {
      val span = root.cut(cmin, root.max).trim
      if (span.len > 0)
        spans += span
    }

    return spans.result
  }

  def rejoinSentences(sents: IndexedSeq[StringSpan]): IndexedSeq[StringSpan] = {
    // Process the spans as a list, building a list in reverse.
    val out = sents.foldLeft(List.empty[StringSpan]){(spans, thisSpan) =>
      if (spans.isEmpty == false) {
        val lastSpan = spans.head
        assert(thisSpan.min >= lastSpan.max)

        // Extract the string hole between the two spans.
        val hole = lastSpan.data.substring(lastSpan.max, thisSpan.min)

        // Find if there were any newlines between the two spans.
        if (hole.contains('\n')) {
          // Keep this span as-is.
          thisSpan :: spans
        } else {
          val lastWord = lastSpan.substring.reverse.
                         dropWhile(_.isLetter == false).
                         takeWhile(_.isWhitespace == false)

          if ((lastWord.length > 0) && (lastWord.last.isUpper == true)) {
            // Join the span with the previous span.
            join(lastSpan, thisSpan) :: spans.drop(1)
          } else {
            // Keep this span as-is.
            thisSpan :: spans
          }
        }
      } else {
        // Keep this span as-is.
        thisSpan :: spans
      }
    }

    // Reverse the list and construct an ArraySeq.
    (StringSpan.emptySeq ++ out.reverse)
  }

  def join(s1: StringSpan, s2: StringSpan): StringSpan = {
    require(s1.data eq s2.data)
    new PlainStringSpan(s1.data, (s1.min min s2.min), (s1.max max s2.max))
  }
}
