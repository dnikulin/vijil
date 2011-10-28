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

object MinCostEdit {
  sealed trait Change
  case class Insert(i1: Int, i2: Int) extends Change
  case class Remove(i1: Int, i2: Int) extends Change
  case class Modify(i1: Int, i2: Int) extends Change

  case class Plan(cost: Long, steps: List[Change])

  def apply[T](xs1: IndexedSeq[T], xs2: IndexedSeq[T])(implicit model: CostModel[T]): Plan = {
    val n1 = xs1.length
    val n2 = xs2.length

    val grid = Array.ofDim[Long](n1 + 1, n2 + 1)
    val z1   = new Array[Long](n1 + 1)
    val z2   = new Array[Long](n2 + 1)

    var i1 = 0
    while (i1 < n1) {
      val cost = model.unit(xs1(i1))
      z1(i1 + 1) = cost
      grid(i1 + 1)(0) = (grid(i1)(0) + cost)
      i1 += 1
    }

    var i2 = 0
    while (i2 < n2) {
      val cost = model.unit(xs2(i2))
      z2(i2 + 1) = cost
      grid(0)(i2 + 1) = (grid(0)(i2) + cost)
      i2 += 1
    }

    i1 = 1
    while (i1 <= n1) {
      val x1 = xs1(i1 - 1)
      val c1 = z1(i1)

      i2 = 1
      while (i2 <= n2) {
        val x2 = xs2(i2 - 1)
        val c2 = z2(i2)

        // Cost of editing item 1 into item 2.
        val cost = model.pair(x1, x2)
        assert(cost >= 0)

        // Total cost of pairing these items directly.
        val costP = (cost + grid(i1 - 1)(i2 - 1))

        // Total cost of pairing item 2 but deleting item 1.
        val cost1 = (c1 + grid(i1 - 1)(i2))

        // Total cost of pairing item 1 but deleting item 2.
        val cost2 = (c2 + grid(i1)(i2 - 1))

        // Calculate minimum cost.
        val costM = (costP min cost1 min cost2)

        // Record minimum cost in grid.
        grid(i1)(i2) = costM

        i2 += 1
      }

      i1 += 1
    }

    // Note final cost.
    val tcost = grid.last.last

    // Create stack of actions.
    var stack = List.empty[Change]

    // Re-trace grid.
    i1 = n1
    i2 = n2
    while ((i1 > 0) && (i2 > 0)) {
      val x1 = xs1(i1 - 1)
      val x2 = xs2(i2 - 1)
      val c1 = z1(i1)
      val c2 = z2(i2)

      // Cost of editing item 1 into item 2.
      val cost = model.pair(x1, x2)
      assert(cost >= 0)

      // Total cost of pairing these items directly.
      val costP = (cost + grid(i1 - 1)(i2 - 1))

      // Total cost of pairing item 2 but deleting item 1.
      val cost1 = (c1 + grid(i1 - 1)(i2))

      // Total cost of pairing item 1 but deleting item 2.
      val cost2 = (c2 + grid(i1)(i2 - 1))

      // Recall chosen cost.
      val costM = grid(i1)(i2)

      if (cost1 == costM) {
        // Item 1 was deleted.
        i1 -= 1
        stack = (Remove(i1, i2) :: stack)
      } else if (cost2 == costM) {
        // Item 2 was inserted.
        i2 -= 1
        stack = (Insert(i1, i2) :: stack)
      } else {
        // Item 1 was edited into item 2.
        i1 -= 1
        i2 -= 1
        stack = (Modify(i1, i2) :: stack)
      }
    }

    while (i1 > 0) {
      i1 -= 1
      stack = Remove(i1, i2) :: stack
    }

    while (i2 > 0) {
      i2 -= 1
      stack = Insert(i1, i2) :: stack
    }

    // Return plan with cost and change stack.
    new Plan(tcost, stack)
  }
}