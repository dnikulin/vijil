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

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

final class WorkerTasks(val count: Int) {
  private val started  = new AtomicInteger(0)
  private val finished = new AtomicInteger(0)
  private val token    = new Object

  def foreach(each: Int => Unit) {
    while (true) {
      // Take start index.
      val index = started.getAndAdd(1)
      if (index >= count)
        return

      // Issue work to function.
      try {
        each(index)
      } catch {
        case ex: Throwable =>
          ex.printStackTrace()

        case _ =>
          println("WorkerTasks.foreach(): error in work item " + index)
      }

      // Count towards completion.
      if (finished.addAndGet(1) == count) {
        token.synchronized {
          // Wake any waiting threads.
          token.notifyAll()
        }
        return
      }
    }
  }

  def join() {
    // Wait until all work units are processed.
    token.synchronized {
      while (finished.get < count)
        token.wait()
    }
  }
}

trait WorkerPool {
  import WorkerPool._

  // Override if desired.
  val nworkers = 16

  protected lazy val workers = new ThreadPoolExecutor(
    nworkers, nworkers, 0L,
    TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue[Runnable]
  )

  def future(work: => Unit): Future[Unit] = {
    val task = new FutureTask(callable(work))
    workers.execute(task)
    return task
  }

  def background(work: => Unit) {
    workers.execute(runnable(work))
  }

  def parallel(count: Int)(each: WorkerTasks => Unit) {
    // Create coordination point for tasks.
    val tasks = new WorkerTasks(count)

    // Issue several jobs for the work queue.
    // Because of the pull design, this will balance naturally
    // even if over-allocated.
    (0 to nworkers).foreach(_ => background(each(tasks)))

    // Wait until all work units are processed.
    tasks.join()
  }

  def parallel[T](items: Array[T])(each: T => Unit) {
    parallel(items.length)(tasks => tasks.foreach(i => each(items(i))))
  }
}

object WorkerPool {
  def apply(nthreads: Int) =
    new WorkerPool {override val nworkers = nthreads}

  def callable(work: => Unit): Callable[Unit] = {
    new Callable[Unit]() {
      override def call(): Unit = {
        try {
          work
        } catch {
          case ex: Throwable =>
            ex.printStackTrace()

          case _ =>
        }
      }
    }
  }

  def runnable(work: => Unit): Runnable = {
    new Runnable() {
      override def run(): Unit = {
        try {
          work
        } catch {
          case ex: Throwable =>
            ex.printStackTrace()

          case _ =>
        }
      }
    }
  }
}
