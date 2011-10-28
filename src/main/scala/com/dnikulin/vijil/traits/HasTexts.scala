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

import net.liftweb.util.Helpers._

import com.dnikulin.vijil.store.ObjectStore
import com.dnikulin.vijil.text.TextFile
import com.dnikulin.vijil.text.TextPage
import com.dnikulin.vijil.text.TextSpan
import com.dnikulin.vijil.tools.TryOrNone

trait HasTexts extends ObjectStore[TextFile] {
  // Abstract method, override in concrete classes
  def text(hash: String): Option[TextFile]

  // Adapt to ObjectStore but as read-only.

  def put(key: String, item: TextFile): Unit = {
    // Ignore
  }

  def put(item: TextFile): Option[String] =
    None

  def get(key: String): Option[TextFile] =
    text(key)

  // Convenience wrappers follow


  def section(hash: String, min: Int): Option[TextSpan] =
    text(hash).flatMap(_.findLeaf(min))

  def section(hash: String, index: String): Option[TextSpan] =
    asInt(index).toOption.flatMap(section(hash, _))

  def section(hashIndex: String): Option[TextSpan] = TryOrNone {
    hashIndex.split("_") match {
      case Array("tfs", hash, smin, smax) =>
        for (text <- this.text(hash);
             min  <- asInt(smin);
             max  <- asInt(smax);
             span <- text.findSpan(min, max))
          yield span

      case Array(hash) =>
        // Attempt to return "root" span.
        text(hash).flatMap(_.spans.headOption)

      case _ =>
        None
    }
  }

  def page(hashNumber: String): Option[TextPage] = TryOrNone {
    hashNumber.split("_") match {
      case Array(hash, number) =>
        text(hash).flatMap(_.page(number.toInt))

      case Array(hash) =>
        text(hash).flatMap(_.pages.headOption)

      case _ =>
        None
    }
  }
}
