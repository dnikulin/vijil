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

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

final object AutoLock {
  final def apply[T](lock: Lock, work: => T): T = try {
    lock.lock()
    work
  } finally {
    lock.unlock()
  }
}

class AutoLock {
  private final val rwlock = new ReentrantReadWriteLock

  final def read[T](work: => T): T =
    AutoLock(rwlock.readLock, work)

  final def write[T](work: => T): T =
    AutoLock(rwlock.writeLock, work)
}
