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

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Ensures a callback is issued either by the timeout or explicit call(). */
final class Deadline(val seconds: Int, val then: (() => Unit)) {
  import Deadline.service

  /** Atomic boolean to ensure the callback is issued exactly once. */
  private val called = new AtomicBoolean(false)

  /** First runnable, scheduled by time. Will try to schedule second runnable. */
  private val runnable1 = new Runnable() {
    override def run(): Unit = {
      call()
    }
  }

  /** Second runnable, scheduled plainly. Will try to run the callback. */
  private val runnable2 = new Runnable() {
    override def run(): Unit = {
      TryTraced {
        then()
        None
      }
    }
  }

  // Arrange for first runnable to be run.
  service.schedule(runnable1, seconds, TimeUnit.SECONDS)

  /** Explicitly call the callback, iff it has not yet been called. */
  def call() {
    if (called.getAndSet(true) == false) {
      // Arrange for second runnable to be run.
      service.execute(runnable2)
    }
  }
}

object Deadline {
  private val service = Executors.newSingleThreadScheduledExecutor()

  def apply(seconds: Int)(then: => Unit): Deadline =
    new Deadline(seconds, () => then)
}
