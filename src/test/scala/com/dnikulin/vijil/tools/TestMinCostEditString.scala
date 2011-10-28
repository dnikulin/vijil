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

import org.junit.Test
import org.junit.Assert._

class TestMinCostEditString {
  import MinCostEdit._

  implicit val model = StringCostModel

  @Test
  def testRemove1() {
    val xs1 = IndexedSeq(
      "foo",
      "bar",
      "baz"
    )

    val xs2 = IndexedSeq(
      "foo",
      "baz"
    )

    val expect = List(
      Modify(0, 0),
      Remove(1, 1),
      Modify(2, 1)
    )

    val actions = MinCostEdit[String](xs1, xs2).steps
    assertEquals(expect, actions)
  }

  @Test
  def testMix1() {
    val xs1 = IndexedSeq(
      "foo",
      "bar",
      "baz"
    )

    val xs2 = IndexedSeq(
      "foo",
      "derp",
      "baz"
    )

    val expect = List(
      Modify(0, 0),
      Modify(1, 1),
      Modify(2, 2)
    )

    val actions = MinCostEdit[String](xs1, xs2).steps
    assertEquals(expect, actions)
  }

  @Test
  def testMix2() {
    val xs1 = IndexedSeq(
      "foo",
      "flanger",
      "baz"
    )

    val xs2 = IndexedSeq(
      "foo",
      "seven",
      "flanger"
    )

    val expect = List(
      Modify(0, 0),
      Insert(1, 1),
      Modify(1, 2),
      Remove(2, 3)
    )

    val actions = MinCostEdit[String](xs1, xs2).steps
    assertEquals(expect, actions)
  }

  @Test
  def testMix3() {
    val xs1 = IndexedSeq(
      "Vigil is free software.",
      "Vigil is distributed in the hope that it will be useful.",
      "You should have received a copy of the GNU."
    )

    val xs2 = IndexedSeq(
      "Vigil is distributed in the hope that it will be awesome.",
      "This file is part of Vigil.",
      "You should have consumed a copy of the GNU."
    )

    val expect = List(
      Remove(0, 0),
      Modify(1, 0),
      Insert(2, 1),
      Modify(2, 2)
    )

    val actions = MinCostEdit[String](xs1, xs2).steps
    assertEquals(expect, actions)
  }
}
