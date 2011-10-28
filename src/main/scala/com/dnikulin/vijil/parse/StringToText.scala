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

import com.dnikulin.vijil.file.Hash
import com.dnikulin.vijil.parse._
import com.dnikulin.vijil.text._
import com.dnikulin.vijil.tools.CleanString._

object StringToText {
  val paragraphLevel = Tag("BlockLevel", "paragraph")

  def apply(name: String, string: String): TextFile = {
    // Normalise newlines to make paragraphs safe.
    val lined  = fixNewlines(string)

    // Split into paragraphs before cleaning whitespace.
    val split  = splitParagraphs(lined).map(_.trim).filter(_.length > 0)

    // Clean references, quotes, whitespace, etc.
    val clean  = split.map(cleanString)

    // Re-split large paragraphs into 4 sentences each.
    val rclean = clean.flatMap(resplit(_, 4)).toList

    // Combine paragraphs into new data string.
    val data   = rclean.mkString("\n")

    // Hash the normalised string data.
    val hash   = Hash.hash(data)

    // Create per-paragraph spans.
    var count  = 0
    var cursor = 0
    val paras  = for (para <- rclean) yield {
      // Calculate position in data string.
      val cmin = cursor
      cursor  += para.length
      val cmax = cursor
      cursor  += 1
      count   += 1

      // Verify position in data string.
      assert(data.substring(cmin, cmax) == para)

      // Create tags for span.
      val tags = List(paragraphLevel, Tag("BlockName", count.toString))

      // Create paragraph span.
      TextSpan(data, hash, cmin, cmax, tags, Nil)
    }

    // Create root span.
    val rootTags = List(Tag("BlockName", name))
    val rootSpan = TextSpan(data, hash, 0, data.length, rootTags, paras)

    // Create text file.
    val textTags = List(Tag("Title", name))
    TextFile(data, hash, textTags, List(rootSpan))
  }

  def resplit(para: String, each: Int): IndexedSeq[String] = {
    var sents = FindSpans.sentences(new PlainStringSpan(para)).map(_.substring)
    val out   = Array.newBuilder[String]
    while (sents.isEmpty == false) {
      out  += sents.take(each).mkString(" ")
      sents = sents.drop(each)
    }
    out.result
  }
}
