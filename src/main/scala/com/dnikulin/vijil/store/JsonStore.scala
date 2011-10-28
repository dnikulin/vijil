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

import com.dnikulin.vijil.traits._

class JsonStore[T <: ToJson](
  override val lower:    ObjectStore[Array[Byte]],
           val fromJson: (Array[Byte] => Option[T])
) extends ObjectStoreLayer[T, Array[Byte]] {

  override def put(key: String, item: T): Unit =
    lower.put(key, item.toJsonBytes)

  override def put(item: T): Option[String] =
    lower.put(item.toJsonBytes)

  override def get(key: String): Option[T] =
    lower.get(key).flatMap(fromJson)
}
