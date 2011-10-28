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

import org.xerial.snappy.Snappy

object SnapByteStore {
  def ratio(zip: Array[Byte], bytes: Array[Byte]): Int =
    ((zip.length.toLong * 100L) / (bytes.length.toLong max 1L)).toInt

  def zipBytes(bytes: Array[Byte]): Array[Byte] = {
    val zip = Snappy.compress(bytes)
    val pcent = ratio(zip, bytes)
    println("Zipped %d bytes into %d (%d%%)".format(bytes.length, zip.length, pcent))
    return zip
  }

  def unzipBytes(zip: Array[Byte]): Array[Byte] = {
    val bytes = Snappy.uncompress(zip)
    val pcent = ratio(zip, bytes)
    println("Unzipped %d bytes into %d (%d%%)".format(zip.length, bytes.length, pcent))
    return bytes
  }
}

class SnapByteStore(override val lower: ObjectStore[Array[Byte]])
  extends ObjectStoreLayer[Array[Byte], Array[Byte]] with ByteStore {

  import SnapByteStore._

  override def put(key: String, bytes: Array[Byte]): Unit = {
    val zip = zipBytes(bytes)
    // Pass to next byte store.
    lower.put(key, zip)
  }

  override def put(bytes: Array[Byte]): Option[String] = {
    val zip = zipBytes(bytes)
    // Pass to next byte store.
    lower.put(zip)
  }

  override def get(key: String): Option[Array[Byte]] = TryOrNone {
    // Fetch from next byte store.
    lower.get(key).map(unzipBytes)
  }
}
