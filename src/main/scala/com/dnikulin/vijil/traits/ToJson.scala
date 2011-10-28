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

package com.dnikulin.vijil.traits

import net.liftweb.json._
import net.liftweb.json.Serialization._

import java.io.StringWriter

import com.dnikulin.vijil.text.SpanPair
import com.dnikulin.vijil.file.Hash.utf8
import com.dnikulin.vijil.tools.TryTraced

object VijilFormats {
  val formats : Formats =
    DefaultFormats
}

trait ToJson {
  private implicit val formats = VijilFormats.formats

  def toJValue: JValue =
    Extraction.decompose(this)(formats)

  def toJson: String =
    ToJson.string(toJValue)

  def toJsonBytes: Array[Byte] =
    toJson.getBytes(utf8)
}

trait FromJson[T <: ToJson] {
  private implicit val formats = VijilFormats.formats

  def fromJValue(jv: JValue): Option[T] =
    TryTraced(Some(postJson(jv.extract)))

  def fromJson(string: String): Option[T] =
    TryTraced(FromJson.string(string).flatMap(fromJValue))

  def fromJson(bytes: Array[Byte]): Option[T] =
    TryTraced(FromJson.bytes(bytes).flatMap(fromJValue))

  protected def postJson(obj: T): T =
    obj
}

object ToJson {
  def string(jv: JValue): String =
    Printer.compact(render(jv), new StringWriter).toString

  def bytes(jv: JValue): Array[Byte] =
    string(jv).getBytes(utf8)
}

object FromJson {
  def string(string: String): Option[JValue] =
    parseOpt(string)

  def bytes(bytes: Array[Byte]): Option[JValue] =
    string(new String(bytes, utf8))
}
