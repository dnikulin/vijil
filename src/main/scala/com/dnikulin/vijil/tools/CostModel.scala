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

import org.apache.commons.lang.StringUtils.getLevenshteinDistance

trait CostModel[-T] {
  def unit(a: T): Long
  def pair(a: T, b: T): Long
}

class NumericZeroCostModel[T](implicit val numeric: Numeric[T]) extends CostModel[T] {
  override def unit(a: T): Long =
    numeric.toLong(a)

  override def pair(a: T, b: T): Long =
    (numeric.toLong(a) - numeric.toLong(b)).abs
}

class NumericCostModel[T](implicit val numeric: Numeric[T]) extends CostModel[T] {
  override def unit(a: T): Long =
    (1L << 48L) // Large number, should be larger than any pair cost.

  override def pair(a: T, b: T): Long =
    (numeric.toLong(a) - numeric.toLong(b)).abs
}

object StringCostModel extends CostModel[String] {
  override def unit(a: String): Long =
    a.length

  override def pair(a: String, b: String): Long =
    getLevenshteinDistance(a, b)
}

object EqualCostModel extends CostModel[Any] {
  override def unit(a: Any): Long =
    1

  override def pair(a: Any, b: Any): Long =
    (if (a equals b) 1 else 0)
}
