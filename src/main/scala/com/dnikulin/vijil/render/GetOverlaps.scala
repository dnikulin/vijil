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

package com.dnikulin.vijil.render

import scala.collection.mutable.ArrayBuilder

import com.dnikulin.vijil.parse._

object GetOverlaps {
  // Assumes spans is already sorted by .min
  def apply[LT <: StringSpan, ST <: StringSpan](leafList: List[LT], spanList: List[ST]): List[(LT, List[ST])] = {
    val leaves = leafList.toIndexedSeq
    val spans  = spanList.toIndexedSeq

    val groups = List.newBuilder[(LT, List[ST])]
    val buffer = List.newBuilder[ST]

    var li = 0
    var si = 0
    while ((li < leaves.length) && (si < spans.length)) {
      val leaf = leaves(li)
      var found = false

      // Catch up in leaves array.
      if (leaf.max >= spans(si).min) {
        // Catch up in spans array.
        while ((si < spans.length) && (spans(si).max < leaf.min))
          si += 1

        // Cycle forward in spans array while overlapping.
        var si2   = si
        while ((si2 < spans.length) && (spans(si2).min <= leaf.max)) {
          if (spans(si2).overlaps(leaf)) {
            buffer += spans(si2)
            found = true
          }
          si2 += 1
        }
      }

      // Record in output.
      if (found == true) {
        groups += (leaf -> buffer.result)
        buffer.clear()
      } else {
        groups += (leaf -> Nil)
      }

      li += 1
    }

    while (li < leaves.length) {
      val leaf = leaves(li)
      groups  += (leaf -> Nil)
      li      += 1
    }

    groups.result
  }
}
