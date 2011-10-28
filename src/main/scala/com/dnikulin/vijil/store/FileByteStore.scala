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

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import com.dnikulin.vijil.tools.TryOrNone

class FileByteStore(val root: String) extends ByteStore {
  def path(key: String): File =
    new File(root + File.separator + key)

  override def put(key: String, bytes: Array[Byte]): Unit = {
    val file = new FileOutputStream(path(key))

    try {
      file.write(bytes)
      file.flush()
    } finally {
      file.close()
    }

    println("Wrote %d bytes to %s".format(bytes.length, key))
  }

  override def get(key: String): Option[Array[Byte]] = TryOrNone {
    val file = new FileInputStream(path(key))

    try {
      val size = file.available
      val bytes = new Array[Byte](size)
      file.read(bytes)
      file.close()
      println("Read %d bytes from %s".format(bytes.length, key))
      Some(bytes)
    } finally {
      file.close()
    }
  }
}
