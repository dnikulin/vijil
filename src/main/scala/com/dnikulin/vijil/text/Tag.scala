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

import net.liftweb.json._

import com.dnikulin.vijil.tools.ArrSeq
import com.dnikulin.vijil.traits.FromJson
import com.dnikulin.vijil.traits.ToJson

case class Tag(name: String, value: String) extends ToJson {
  override def toJValue: JValue =
    JArray(List(JString(name), JString(value)))
}

object Tag extends FromJson[Tag] {
  val emptySeq   = ArrSeq.empty[Tag]
  val emptyArray = new Array   [Tag](0)

  override def fromJValue(jv: JValue): Option[Tag] = jv match {
    case JArray(List(JString(name), JString(value))) =>
      Some(Tag(name, value))

    case _ =>
      None
  }
}

trait HasTags[T <: HasTags[T]] {
  self: T =>

  val tags: IndexedSeq[Tag]

  def tag(name: String): Seq[String] =
    tags.filter(_.name.equalsIgnoreCase(name)).map(_.value)

  def tagOr(name: String, default: String = "?"): String =
    tag(name).headOption.getOrElse(default)

  def addTag(tag: Tag): T
}
