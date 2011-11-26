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

package com.dnikulin.vijil.report

import scala.collection.mutable.HashMap

import net.liftweb.json._

import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.result.SpanText
import com.dnikulin.vijil.text._
import com.dnikulin.vijil.tools._
import com.dnikulin.vijil.traits._

case class SourceText(text: TextFile, spans: Seq[StubSpan])
  extends KeyedByHash[SourceText] with ToJson {

  override val hash = text.hash

  val clen = spans.foldLeft(0)(_ + _.clen)
  val llen = spans.foldLeft(0)(_ + _.llen)

  override def toJValue: JValue = {
    val jspans = spans.sortWith(_.cmin < _.cmin).map(_.toJValue).toList
    JArray(List(text.toLiteJValue, JArray(jspans)))
  }
}

object SourceText extends FromJson[SourceText] {
  override def fromJValue(jv: JValue): Option[SourceText] = jv match {
    case JArray(List(jtext, JArray(jspans))) =>
      for (text <- TextFile.fromJValue(jtext)) yield {
        val spans = jspans.flatMap(StubSpan.fromJValue)
        SourceText(text, spans)
      }

    case _ =>
      None
  }

  def makeSourceTexts(text: TextModel, spans: Seq[SpanText]): Seq[SourceText] = {
    val utexts = new HashMap[String, Option[TextFile]]

    for (span <- spans)
      utexts.getOrElseUpdate(span.text2, TextFile.fromJson(span.meta2))

    for (utext <- utexts.values.toSeq; meta <- utext.toSeq) yield {
      val stubs = {
        for (span <- spans; if (span.text2 == meta.hash))
          yield StubSpan(text, span)
      }

      SourceText(meta, stubs)
    }
  }
}
