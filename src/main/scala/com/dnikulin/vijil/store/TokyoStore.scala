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

import java.net.InetSocketAddress

import tokyotyrant.RDB
import tokyotyrant.transcoder.ByteArrayTranscoder
import tokyotyrant.transcoder.StringTranscoder

class TokyoStore(val host: String, val port: Int) extends ByteStore {
  // Create remote database connection.
  protected val rdb = new RDB

  // Use strings for keys, byte arrays for values.
  rdb.setKeyTranscoder(new StringTranscoder)
  rdb.setValueTranscoder(new ByteArrayTranscoder)

  // Attempt to open connection.
  log("Connecting")
  val address = new InetSocketAddress(host, port)
  rdb.open(address)
  log("Connected")

  def close(): Unit = synchronized {
    log("Disconnecting")
    rdb.close()
  }

  override def put(key: String, bytes: Array[Byte]): Unit = synchronized {
    rdb.put(key, bytes)
    log("Wrote %d bytes to [%s]".format(bytes.length, key))
  }

  override def get(key: String): Option[Array[Byte]] = synchronized {
    val obj = rdb.get(key)
    if (obj eq null) {
      None
    } else {
      val bytes = obj.asInstanceOf[Array[Byte]]
      log("Read %d bytes from [%s]".format(bytes.length, key))
      Some(bytes)
    }
  }

  protected def log(line: String): Unit =
    println("Tokyo Tyrant (%s:%d): %s".format(host, port, line))
}
