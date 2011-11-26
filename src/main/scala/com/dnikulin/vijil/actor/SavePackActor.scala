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

package com.dnikulin.vijil.actor

import java.io.File
import java.io.FileOutputStream

import akka.actor._

import com.dnikulin.vijil.file.Hash
import com.dnikulin.vijil.file.TextPack
import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.tools.SoloActor

class SavePackActor(val root: File, val full: Boolean) extends SoloActor {
  private def save(texts: Seq[TextModel]): Unit = {
    val bytes = TextPack.write(texts.toArray, full)
    val hash = Hash.hash(bytes)
    val path = root.getAbsolutePath + File.separator + hash
    val file = new FileOutputStream(path)
    file.write(bytes)
    file.close()

    println("Wrote %d KiB to %s".format(bytes.length / 1024, hash))
  }

  override def receive(): Receive = {
    case ParsedTexts(texts, plan) =>
      save(texts)

    case Shutdown =>
      self.stop()

    case _ =>
  }
}
