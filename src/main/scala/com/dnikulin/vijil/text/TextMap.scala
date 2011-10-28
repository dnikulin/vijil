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

import net.liftweb.util.SimpleInjector

// Trait to early-bind the erased function type for text retrieval.
trait TextMap extends (String => Option[TextFile])

object TextMap {
  def apply(get: (String => Option[TextFile])) = new TextMap {
    override def apply(hash: String): Option[TextFile] =
      get(hash)
  }
}

// Injector to late-bind an implementation of the trait.
object TextMapInjector extends SimpleInjector {
  def register(get: (String => Option[TextFile])): Unit = {
    // Wrap the retrieval function once.
    val map = TextMap(get)

    // Return the retrieval function for any injection.
    registerInjection[TextMap](() => map)
  }

  def apply(hash: String): Option[TextFile] =
    inject[TextMap].toOption.flatMap(_.apply(hash))
}
