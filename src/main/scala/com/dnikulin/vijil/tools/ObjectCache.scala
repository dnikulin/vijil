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

import java.util.concurrent.ConcurrentMap

import com.google.common.base.{Function => GFunction}
import com.google.common.collect.MapMaker

final class ObjectCache[KT, VT](val maker: MapMaker, val calculate: (KT => VT)) {
  // Wrap 'calculate' Scala function as a Google Guava function.
  private val function = new GFunction[KT, VT] {
    override def apply(key: KT): VT =
      calculate(key)
  }

  // Create a concurrent map that invokes and caches 'calculate'.
  private val cache: ConcurrentMap[KT, VT] =
    maker.makeComputingMap(function)

  def apply(key: KT): VT =
    cache.get(key)

  def update(key: KT, value: VT): Unit =
    cache.put(key, value)

  def clear(): Unit =
    cache.clear()
}

object ObjectCache {
  def defaultMapMaker: MapMaker = {
    new MapMaker().
      softValues().
      concurrencyLevel(8)
  }

  def configure[KT, VT](config: (MapMaker => MapMaker))(calculate: (KT => VT)): ObjectCache[KT, VT] =
    new ObjectCache[KT, VT](config(defaultMapMaker), calculate)

  def apply[KT, VT](calculate: (KT => VT)): ObjectCache[KT, VT] =
    new ObjectCache[KT, VT](defaultMapMaker, calculate)
}
