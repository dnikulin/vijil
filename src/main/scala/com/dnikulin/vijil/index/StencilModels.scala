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

package com.dnikulin.vijil.index

object StencilModels {
  val noIndices  = Array.ofDim[Int](0, 0)
  val nilIndices = Array.ofDim[Int](1, 0)

  val indices4of4 = (0 until 4).toArray
  val indices4of5 = indices(4, 5)
  val indices4of6 = indices(4, 6)
  val indices5of5 = (0 until 5).toArray
  val indices5of6 = indices(5, 6)
  val indices5of7 = indices(5, 7)

  val exact4of4 = new StencilModel(4, indices4of4, false)
  val exact4of5 = new StencilModel(4, indices4of5, false)
  val exact4of6 = new StencilModel(4, indices4of6, false)
  val sort4of4  = new StencilModel(4, indices4of4, true)
  val sort4of5  = new StencilModel(4, indices4of5, true)
  val sort4of6  = new StencilModel(4, indices4of6, true)

  val exact5of5 = new StencilModel(5, indices5of5, false)
  val exact5of6 = new StencilModel(5, indices5of6, false)
  val exact5of7 = new StencilModel(5, indices5of7, false)
  val sort5of5  = new StencilModel(5, indices5of5, true)
  val sort5of6  = new StencilModel(5, indices5of6, true)
  val sort5of7  = new StencilModel(5, indices5of7, true)

  def allSets(lo: Int, hi: Int, take: Int): Array[Array[Int]] = {
    require(lo >= 0)
    require(hi >= lo)
    require(take >= 0)

    if (take < 1)
      return nilIndices

    if (lo >= hi)
      return noIndices

    val own = Array(lo)

    val wasTaken = allSets(lo + 1, hi, take - 1).map(own ++ _)
    val wasLeft  = allSets(lo + 1, hi, take)

    return wasTaken ++ wasLeft
  }

  def indices(setSize: Int, listSize: Int): Array[Int] = {
    val allIndices = (0 until listSize).toArray
    def indicesMinus(gap: Array[Int]): Array[Int] =
      allIndices.filter(i => !gap.contains(i))

    // Start gaps at 1, gaps at 0 are redundant.
    val allGaps = allSets(1, listSize, listSize - setSize)
    return allGaps.flatMap(indicesMinus)
  }
}
