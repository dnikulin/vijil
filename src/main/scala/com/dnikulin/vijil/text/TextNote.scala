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

import com.dnikulin.vijil.traits.FromJson
import com.dnikulin.vijil.traits.ToJson

case class TextNote(at: Int, label: String, body: String) extends ToJson {
  override def toJValue: JValue = {
    JArray(List(JInt(at), JString(label), JString(body)))
  }
}

object TextNote extends FromJson[TextNote] {
  override def fromJValue(jv: JValue): Option[TextNote] = jv match {
    case JArray(List(JInt(at), JString(label), JString(body))) =>
      Some(TextNote(at.toInt, label, body))

    case _ =>
      None
  }
}

trait HasTextNotes {
  val notes: IndexedSeq[TextNote]
}
