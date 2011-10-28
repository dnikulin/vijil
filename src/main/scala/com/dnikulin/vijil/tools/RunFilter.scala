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

package com.dnikulin.vijil.tools

import java.io.ByteArrayOutputStream

object RunFilter {
  private val rpool = WorkerPool(4)
  private val wpool = WorkerPool(4)
  private val epool = WorkerPool(4)

  def apply(command: String, input: Array[Byte]): Option[Array[Byte]] = TryTraced {
    println("Process [%s] with %d input bytes starting".format(command, input.length))
    val time1 = System.currentTimeMillis

    // Create and start external process.
    val process =
      new ProcessBuilder().
      command(command.split("\\s+"):_*).
      start()

    // Buffer for output bytes.
    val output = new ByteArrayOutputStream

    // Thread to write input to process.
    val writer = wpool.future {
      val pipe = process.getOutputStream
      pipe.write(input)
      pipe.close()
    }

    // Thread to read output from process.
    val reader = rpool.future {
      val pipe = process.getInputStream
      val buffer = new Array[Byte](8192)
      var count = 1
      while (count > 0) {
        count = pipe.read(buffer)
        if (count > 0)
          output.write(buffer, 0, count)
      }
      pipe.close()
      output.flush()
    }

    // Thread to read stderr from process.
    val drainer = epool.future {
      val pipe = process.getErrorStream
      val buffer = new Array[Byte](8192)
      var count = 1
      while (count > 0)
        count = pipe.read(buffer)
      pipe.close()
    }

    // Block on all futures.
    writer.get()
    reader.get()
    drainer.get()

    // Block on process.
    val returned = process.waitFor()

    val time2 = System.currentTimeMillis

    println("Process [%s] with %d input bytes finished in %d ms with code %d".format(command, input.length, (time2 - time1), returned))

    // Finish output buffer.
    Some(output.toByteArray)
  }
}
