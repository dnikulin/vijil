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

import scala.collection.mutable.HashMap

case class Count[T <: AnyRef](item: T, count: Int)

object Histogram {
  def apply[T <: AnyRef](items: Array[T]): Array[Count[T]] = {
    val table      = new HashMap[T, Count[T]]

    var index      = 0
    while (index   < items.length) {
      val item     = items(index)
      index       += 1

      val entry    = table.get(item).getOrElse(Count(item, 0))
      table(item)  = entry.copy(count=entry.count+1)
    }

    table.values.toArray.sortBy(-_.count)
  }
}
