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

import net.liftweb.mapper.IndexedField
import net.liftweb.mapper.KeyedMapper
import net.liftweb.mapper.MappedBinary
import net.liftweb.mapper.MappedField
import net.liftweb.mapper.MappedStringIndex

trait MapperByteStore[OwnerType <: MapperByteStore[OwnerType]]
  extends ByteStore with KeyedMapper[String, OwnerType] {

  self: OwnerType =>

  object storedHash extends MappedStringIndex [OwnerType](this, 64) {
    override def dbNotNull_? = true
  }

  object storedData extends MappedBinary      [OwnerType](this) {
    override def dbNotNull_? = true
  }

  override def primaryKeyField = storedHash

  override def put(key: String, bytes: Array[Byte]): Unit =
    getSingleton.create.storedHash(key).storedData(bytes).save()

  override def get(key: String): Option[Array[Byte]] =
    TryOrNone(getSingleton.findByKey(key).map(_.storedData.get))
}
