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

package com.dnikulin.vijil.store

import java.util.concurrent.ConcurrentHashMap

import com.dnikulin.vijil.traits.HasHash

class DirectStore[T <: AnyRef] extends ObjectStore[T] {
  val store = new ConcurrentHashMap[String, T]

  override def put(key: String, item: T): Unit =
    store.put(key, item)

  override def put(item: T): Option[String] = item match {
    case x: HasHash =>
      put(x.hash, item)
      Some(x.hash)

    case _ =>
      None
  }

  override def get(key: String): Option[T] = {
    val value = store.get(key)
    if (value eq null) None
    else Some(value)
  }
}
