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

import net.liftweb.json._

import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.result.SpanText
import com.dnikulin.vijil.traits._

case class StubSpan(cmin: Int, cmax: Int, lmin: Int, lmax: Int) extends ToJson {
  require(cmax >= cmin)
  require(lmax >= lmin)

  def clen = (cmax - cmin)
  def llen = (lmax - lmin)

  override def toJValue: JValue = {
    JArray(
      List(
        JArray(List(JInt(cmin), JInt(cmax))),
        JArray(List(JInt(lmin), JInt(lmax)))
      )
    )
  }
}

object StubSpan extends FromJson[StubSpan] {
  def apply(text: TextModel, span: SpanText): StubSpan = {
    assert(text.hash != span.text2)
    val lmin = span.min
    val lmax = span.max
    val cmin = text.minChar(lmin)
    val cmax = text.maxChar(lmax - 1)
    StubSpan(cmin, cmax, lmin, lmax)
  }
  
  override def fromJValue(jv: JValue): Option[StubSpan] = jv match {
    case
      JArray(
        List(
          JArray(List(JInt(cmin), JInt(cmax))),
          JArray(List(JInt(lmin), JInt(lmax)))
        )
      ) =>

      Some(StubSpan(cmin.toInt, cmax.toInt, lmin.toInt, lmax.toInt))

    case _ =>
      None
  }
}
