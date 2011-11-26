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

import net.liftweb.json._

import com.dnikulin.vijil.file.Hash
import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.parse.StringSpan
import com.dnikulin.vijil.parse.Words
import com.dnikulin.vijil.render.NodeSpan
import com.dnikulin.vijil.result.LinkSpan
import com.dnikulin.vijil.traits._
import com.dnikulin.vijil.tools.ArrSeq
import com.dnikulin.vijil.tools.Empty

case class TextFile(
  override val data:    String,
  override val hash:    String,
  override val tags:    IndexedSeq[Tag]      = ArrSeq.emptySeq,
  override val spans:   IndexedSeq[TextSpan] = ArrSeq.emptySeq,
  override val notes:   IndexedSeq[TextNote] = ArrSeq.emptySeq,
  override val marks:   IndexedSeq[NodeSpan] = ArrSeq.emptySeq,
  override val runes:   IndexedSeq[Rune]     = ArrSeq.emptySeq
) extends KeyedByHash[TextFile] with HasData[String] with HasTags[TextFile] with HasSpans[TextSpan] with HasTextNotes with HasMarks with HasRunes with ToJson {

  val name: String =
    Array("Title", "TextName", "URL").
    flatMap(tag).
    headOption.
    getOrElse(hash)

  val leaves: IndexedSeq[TextSpan] =
    spans.flatMap(_.leafSpans)

  def this(hash: String, tags: IndexedSeq[Tag]) =
    this(Empty.string, hash, tags, ArrSeq.emptySeq, ArrSeq.emptySeq, ArrSeq.emptySeq)

  override def addTag(tag: Tag): TextFile =
    new TextFile(data, hash, ArrSeq(tag) ++ tags, spans, notes, marks)

  def findLeaf(min: Int): Option[TextSpan] =
    leaves.find(_.min == min)

  def findLeaves(modelSpan: LinkSpan): IndexedSeq[TextSpan] = {
    if (modelSpan.hash != hash)
      return ArrSeq.emptySeq

    findLeaves(TextSpan(this, modelSpan))
  }

  def findLeaves(textSpan: StringSpan): IndexedSeq[TextSpan] = {
    if (textSpan.data ne data)
      return ArrSeq.emptySeq

    return leaves.filter(_.overlaps(textSpan))
  }

  /** Reduce to only hash and tags. */
  def toMeta: TextFile =
    new TextFile(hash, tags)

  def toLiteBytes: Array[Byte] =
    ToJson.bytes(toLiteJValue)

  def findSpan(min: Int, max: Int): Option[TextSpan] =
    spans.flatMap(_.findSpan(min, max)).headOption

  lazy val pages = TextPage.makePages(this)

  def page(number: Int): Option[TextPage] = {
    if ((number > 0) && (number <= pages.length)) Some(pages(number - 1))
    else None
  }

  override def toJValue: JValue = {
    val jtags  =  tags.map(_.toJValue).toList
    val jspans = spans.map(_.toLiteJValue).toList
    val jnotes = notes.map(_.toJValue).toList

    JObject(
      List(
        JField("data",  JString(data)),
        JField("hash",  JString(hash)),
        JField("tags",  JArray(jtags)),
        JField("spans", JArray(jspans)),
        JField("notes", JArray(jnotes))
      )
    )
  }

  def toLiteJValue: JValue = {
    val jtags = tags.map(_.toJValue).toList
    JArray(List(JString(hash), JArray(jtags)))
  }
}

object TextFile extends FromJson[TextFile] {
  val emptySeq   = ArrSeq.empty[TextSpan]
  val emptyArray = new Array   [TextSpan](0)

  override def fromJValue(jv: JValue): Option[TextFile] = jv match {
    case JArray(List(JString(hash), JArray(jtags))) =>
      val tags = ArrSeq.convert(jtags.flatMap(Tag.fromJValue))
      Some(new TextFile(hash, tags))

    case
      JObject(
        List(
          JField("data",  JString(data)),
          JField("hash",  JString(hash)),
          JField("tags",  JArray(jtags)),
          JField("spans", JArray(jspans)),
          JField("notes", JArray(jnotes))
        )
      ) =>

      val tags  = ArrSeq.convert( jtags.flatMap(     Tag.fromJValue))
      val spans = ArrSeq.convert(jspans.flatMap(TextSpan.fromLiteJValue(data, hash)))
      val notes = ArrSeq.convert(jnotes.flatMap(TextNote.fromJValue))

      Some(new TextFile(data, hash, tags, spans, notes))

    case _ =>
      None
  }
}
