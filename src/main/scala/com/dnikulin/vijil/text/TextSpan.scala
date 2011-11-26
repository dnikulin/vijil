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

import scala.collection.mutable.ArraySeq

import java.lang.Math

import net.liftweb.json._

import com.dnikulin.vijil.parse.StringSpan
import com.dnikulin.vijil.result.LinkSpan
import com.dnikulin.vijil.result.SpanDomain
import com.dnikulin.vijil.tools._
import com.dnikulin.vijil.traits._

case class TextSpan(
  override val data:  String,
  override val hash:  String,
  override val min:   Int,
  override val max:   Int,
  override val tags:  IndexedSeq[Tag]      = ArrSeq.emptySeq,
  override val spans: IndexedSeq[TextSpan] = ArrSeq.emptySeq
) extends StringSpan with HasHash with HasSpans[TextSpan] with HasTags[TextSpan] with HasIdentity with ToJson {

  require(min >= 0)
  require(max >= min)
  require(max <= data.length)

  // Require very strong text equality.
  require(spans.forall(_.hash eq hash))
  require(spans.forall(_.data eq data))

  val name = tag("BlockPath").headOption.getOrElse("?")

  override val identity = "tfs_%s_%9d_%9d".format(hash, min, max)

  override def addTag(tag: Tag): TextSpan =
    new TextSpan(data, hash, min, max, ArrSeq(tag) ++ tags, spans)

  override def cut(min1: Int, max1: Int): TextSpan = {
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
    new TextSpan(data, hash, min1b, max1b, tags, spans)
  }

  def leafSpans(): IndexedSeq[TextSpan] = {
    if  (spans.isEmpty) ArrSeq(this)
    else spans.flatMap(_.leafSpans)
  }

  def findSpan(min1: Int, max1: Int): Option[TextSpan] = {
    if ((min == min1) && (max == max1))
      return Some(this)

    for (span <- spans) {
      if ((span.min <= min1) && (span.max >= max1))
        return span.findSpan(min1, max1)
    }

    return None
  }

  def findLeaves(textSpan: StringSpan): IndexedSeq[TextSpan] = {
    // No possible solution if this span does not overlap.
    if (overlaps(textSpan) == false)
      return ArrSeq.emptySeq

    // Buffer for confirmed overlapping leaves.
    val buffer = ArrSeq.newBuilder[TextSpan]

    // Depth-first search function.
    def search(root: TextSpan) {
      if (root.spans.isEmpty) {
        // Record root as a confirmed leaf.
        buffer += root
      } else {
        // Iterate through all children of the root.
        var ispan = 0
        while (ispan < root.spans.length) {
          val span = root.spans(ispan)
          ispan += 1

          // Recurse if the span overlaps.
          if (span.overlaps(textSpan))
            search(span)
        }
      }
    }

    // Start search from this span.
    search(this)

    // Return any leaves found.
    buffer.result
  }

  override def toJValue: JValue = {
    val jtags  =  tags.map(_.toJValue).toList
    val jspans = spans.map(_.toJValue).toList
    JArray(List(JString(hash), JInt(min), JInt(max), JArray(jtags), JArray(jspans)))
  }

  def toLiteJValue: JValue = {
    val jtags  =  tags.map(_.toJValue).toList
    val jspans = spans.map(_.toLiteJValue).toList
    JArray(List(JInt(min), JInt(max), JArray(jtags), JArray(jspans)))
  }

  def toLitestJValue: JValue = {
    JArray(List(JInt(min), JInt(max)))
  }

  def findText: Option[TextFile] =
    TextMapInjector(hash)
}

object TextSpan extends FromJson[TextSpan] {
  val emptySeq   = ArrSeq.empty[TextSpan]
  val emptyArray = new Array   [TextSpan](0)

  def single(span: TextSpan): IndexedSeq[TextSpan] = {
    val out = new ArraySeq[TextSpan](1)
    out(0) = span
    out
  }

  def apply(data: String, hash: String): TextSpan =
    new TextSpan(data, hash, 0, data.length)

  def apply(text: TextFile): TextSpan =
    apply(text.data, text.hash)

  def apply(text: TextFile, min: Int, max: Int): TextSpan =
    apply(text.data, text.hash, min, max)

  def apply(data: String, hash: String, dspan: LinkSpan): TextSpan = {
    require(dspan.set.domain == SpanDomain.CHARACTERS)
    apply(data, hash, dspan.min, dspan.max)
  }

  def apply(text: TextFile, dspan: LinkSpan): TextSpan = {
    require(dspan.set.domain == SpanDomain.CHARACTERS)
    apply(text, dspan.min, dspan.max)
  }

  def apply(texts: Seq[TextFile], dspan: LinkSpan): Option[TextSpan] = {
    require(dspan.set.domain == SpanDomain.CHARACTERS)
    texts.find(_.hash == dspan.hash).map(apply(_, dspan))
  }

  override def fromJValue(jv: JValue): Option[TextSpan] = jv match {
    case JArray(List(JString(hash), JInt(min), JInt(max), JArray(jtags), JArray(jspans))) =>
      for (text <- TextMapInjector(hash)) yield {
        val tags  = ArrSeq.convert( jtags.flatMap(Tag.fromJValue))
        val spans = ArrSeq.convert(jspans.flatMap(TextSpan.fromJValue))
        new TextSpan(text.data, text.hash, min.toInt, max.toInt, tags, spans)
      }

    case _ =>
      None
  }

  def fromLiteJValue(data: String, hash: String)(jv: JValue): Option[TextSpan] = jv match {
    case JArray(List(JInt(min), JInt(max), JArray(jtags), JArray(jspans))) =>
      val tags  = ArrSeq.convert( jtags.flatMap(Tag.fromJValue))
      val spans = ArrSeq.convert(jspans.flatMap(fromLiteJValue(data, hash)))
      Some(new TextSpan(data, hash, min.toInt, max.toInt, tags, spans))

    case JArray(List(JInt(min), JInt(max))) =>
      Some(new TextSpan(data, hash, min.toInt, max.toInt, ArrSeq.emptySeq, ArrSeq.emptySeq))

    case _ =>
      None
  }
}
