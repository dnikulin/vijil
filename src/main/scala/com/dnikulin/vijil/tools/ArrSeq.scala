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

import scala.collection.mutable.ArraySeq

/** Collection of utility functions to complement those available
 * in ArraySeq. */
object ArrSeq {
  /** Empty ArraySeq typed as an IndexedSeq. */
  val emptySeq: IndexedSeq[Nothing] =
    new ArraySeq[Nothing](0)

  /** Empty ArraySeq typed as an IndexedSeq[T].
   *
   * Unlike ArraySeq.empty, this returns a singleton. */
  def empty[T]: IndexedSeq[T] =
    emptySeq

  /** Convert a collection to an ArraySeq typed as an IndexedSeq[T]. */
  def convert[T](items: TraversableOnce[T]): IndexedSeq[T] = {
    val buffer = ArraySeq.newBuilder[T]
    buffer ++= items
    buffer.result
  }

  /** Delegate directly to ArraySeq.newBuilder[T]. */
  def newBuilder[T] =
    ArraySeq.newBuilder[T]

  /** ArraySeq of one item typed as an IndexedSeq[T]. */
  def apply[T](i0: T): IndexedSeq[T] = {
    val array = new ArraySeq[T](1)
    array(0) = i0
    array
  }

  /** ArraySeq of two items typed as an IndexedSeq[T]. */
  def apply[T](i0: T, i1: T): IndexedSeq[T] = {
    val array = new ArraySeq[T](2)
    array(0) = i0
    array(1) = i1
    array
  }

  /** ArraySeq of three items typed as an IndexedSeq[T]. */
  def apply[T](i0: T, i1: T, i2: T): IndexedSeq[T] = {
    val array = new ArraySeq[T](3)
    array(0) = i0
    array(1) = i1
    array(2) = i2
    array
  }

  /** ArraySeq of four items typed as an IndexedSeq[T]. */
  def apply[T](i0: T, i1: T, i2: T, i3: T): IndexedSeq[T] = {
    val array = new ArraySeq[T](4)
    array(0) = i0
    array(1) = i1
    array(2) = i2
    array(3) = i3
    array
  }
}
