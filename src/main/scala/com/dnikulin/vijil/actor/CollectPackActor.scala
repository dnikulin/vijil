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

import scala.collection.mutable.ArrayBuilder

import akka.actor._

import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.tools.SoloActor

class CollectPackActor(val minlemmas: Int, val next: ActorRef) extends SoloActor {
  private val queue = new ArrayBuilder.ofRef[TextModel]
  private var size  = 0

  private def flush(): Unit = {
    val texts = queue.result.toList
    queue.clear()
    println("Flushing %d lemmas in %d texts".format(size, texts.length))
    size = 0

    if (texts.isEmpty == false)
      next ! ParsedTexts(texts)
  }

  private def add(text: TextModel): Unit = {
    queue += text
    size += text.size
    if (size >= minlemmas)
      flush()
  }

  override def receive(): Receive = {
    case ParsedTexts(texts, plan) =>
      texts.foreach(add)

    case DeepFlush =>
      flush()
      next ! DeepFlush

    case Shutdown =>
      flush()
      next ! Shutdown
      self.stop()

    case _ =>
  }
}
