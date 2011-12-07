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

import com.dnikulin.vijil.tools.TryOrNone

class ByteFilterStore(override val lower: ObjectStore[Array[Byte]], val filter: ByteFilter)
  extends ObjectStoreLayer[Array[Byte], Array[Byte]] {

  override def put(key: String, bytes: Array[Byte]): Unit =
    lower.put(key, filter.apply(bytes))

  override def put(bytes: Array[Byte]): Option[String] =
    lower.put(filter.apply(bytes))

  override def get(key: String): Option[Array[Byte]] =
    TryOrNone(lower.get(key).map(filter.unapply))
}
