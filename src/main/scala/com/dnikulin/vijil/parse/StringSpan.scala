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

import java.lang.Math

import com.dnikulin.vijil.tools.ArrSeq
import com.dnikulin.vijil.traits.HasData
import com.dnikulin.vijil.traits.Span

object StringSpan {
  val emptySeq   = ArrSeq.empty[StringSpan]
  val emptyArray = new Array   [StringSpan](0)
}

trait StringSpan extends Span[StringSpan] with HasData[String] {
  require(max <= data.length)

  def substring: String = data.substring(min, max)

  assert(substring.length == len)

  override def cut(min1: Int, max1: Int): StringSpan

  def overlaps(that: StringSpan): Boolean = {
    (this.data eq that.data) && (
      this.includes(that.min) || this.includes(that.max - 1) ||
      that.includes(this.min) || that.includes(this.max - 1)
    )
  }

  def trim(): StringSpan = {
    // Move min forward while in whitespace.
    var min1 = min
    while ((min1 < max) && Character.isWhitespace(data.charAt(min1)))
      min1 += 1

    // Move max backward while in whitespace.
    var max1 = max
    while ((max1 > min1) && Character.isWhitespace(data.charAt(max1 - 1)))
      max1 -= 1

    // Return (potentially empty) trimmed span.
    return cut(min1, max1)
  }
}

case class PlainStringSpan(
  override val data: String,
  override val min:  Int,
  override val max:  Int
) extends StringSpan {

  def this(data: String) =
    this(data, 0, data.length)

  override def cut(min1: Int, max1: Int): PlainStringSpan = {
    // Check sanity of new span.
    require(min1 >= 0)
    require(max1 >= min1)

    // Clamp new span within this span.
    val min1b = Math.max(min1, min)
    val max1b = Math.min(max1, max)

    // Check if the new span is equivalent.
    if ((min1b == min) && (max1b == max))
      return this

    // Create new span.
    new PlainStringSpan(data, min1b, max1b)
  }
}
