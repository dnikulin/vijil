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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object GZipByteFilter extends ByteFilter {
  val zipBufferSize = 8192

  override def apply(bytes: Array[Byte]): Array[Byte] = {
    // Compress into memory buffer.
    val byteBuffer = new ByteArrayOutputStream()
    val zipper = new GZIPOutputStream(byteBuffer, zipBufferSize)
    zipper.write(bytes)
    zipper.close()

    // Snapshot memory buffer.
    return byteBuffer.toByteArray
  }

  override def unapply(zip: Array[Byte]): Array[Byte] = {
    // Unzip into memory buffer.
    val ibuffer = new ByteArrayInputStream(zip)
    val unzipper = new GZIPInputStream(ibuffer)

    // Build output buffer from zipped data.
    val obuffer = new ByteArrayOutputStream()
    val pack = new Array[Byte](zipBufferSize)
    var read = 1
    while (read > 0) {
      read = unzipper.read(pack)
      if (read > 0)
        obuffer.write(pack, 0, read)
    }

    // Close unzipper.
    unzipper.close()
    ibuffer.close()

    // Finish output buffer.
    return obuffer.toByteArray
  }
}
