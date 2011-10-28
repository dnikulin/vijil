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

class TestMinCostEditChar {
  import MinCostEdit._

  implicit val model = new NumericCostModel[Char]

  @Test
  def testEmpty() {
    val xs1 = ""

    val actions = MinCostEdit[Char](xs1, xs1).steps
    assertTrue(actions.isEmpty)
  }

  @Test
  def testModify1() {
    val xs1 = "e"

    val actions = MinCostEdit[Char](xs1, xs1).steps
    assertEquals(List(Modify(0, 0)), actions)
  }

  @Test
  def testModify2() {
    val xs1 = "ek"

    val actions = MinCostEdit[Char](xs1, xs1).steps
    assertEquals(List(Modify(0, 0), Modify(1, 1)), actions)
  }

  @Test
  def testModify3() {
    val xs1 = "ie"
    val xs2 = "bie"

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(List(Insert(0, 0), Modify(0, 1), Modify(1, 2)), actions)
  }

  @Test
  def testInsert1() {
    val xs1 = ""
    val xs2 = "g"

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(List(Insert(0, 0)), actions)
  }

  @Test
  def testInsert2() {
    val xs1 = "g"
    val xs2 = "gp"

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(List(Modify(0, 0), Insert(1, 1)), actions)
  }

  @Test
  def testInsert3() {
    val xs1 = "g"
    val xs2 = "pg"

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(List(Insert(0, 0), Modify(0, 1)), actions)
  }

  @Test
  def testDelete1() {
    val xs1 = "f"
    val xs2 = ""

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(List(Remove(0, 0)), actions)
  }

  @Test
  def testDelete2() {
    val xs1 = "ie"
    val xs2 = "i"

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(List(Modify(0, 0), Remove(1, 1)), actions)
  }

  @Test
  def testDelete3() {
    val xs1 = "ie"
    val xs2 = "e"

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(List(Remove(0, 0), Modify(1, 0)), actions)
  }

  @Test
  def testAppend() {
    val xs1 = "seven"
    val xs2 = "seventh"

    val expect = List(
      Modify(0, 0),
      Modify(1, 1),
      Modify(2, 2),
      Modify(3, 3),
      Modify(4, 4),
      Insert(5, 5),
      Insert(5, 6)
    )

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(expect, actions)
  }

  @Test
  def testPrepend() {
    val xs1 = "unseven"
    val xs2 = "seven"

    val expect = List(
      Remove(0, 0),
      Remove(1, 0),
      Modify(2, 0),
      Modify(3, 1),
      Modify(4, 2),
      Modify(5, 3),
      Modify(6, 4)
    )

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(expect, actions)
  }

  @Test
  def testMix() {
    val xs1 = "1seven"
    val xs2 = "seven2"

    val expect = List(
      Remove(0, 0),
      Modify(1, 0),
      Modify(2, 1),
      Modify(3, 2),
      Modify(4, 3),
      Modify(5, 4),
      Insert(6, 5)
    )

    // Requires alternative model.
    implicit val model = new NumericZeroCostModel[Char]

    val actions = MinCostEdit[Char](xs1, xs2).steps
    assertEquals(expect, actions)
  }
}

class TestMinCostEditInt {
  import MinCostEdit._

  implicit object model extends NumericCostModel[Int]

  @Test
  def testEmpty() {
    val xs1 = IndexedSeq.empty

    val actions = MinCostEdit[Int](xs1, xs1).steps
    assertTrue(actions.isEmpty)
  }

  @Test
  def testModify1() {
    val xs1 = IndexedSeq(5)

    val actions = MinCostEdit[Int](xs1, xs1).steps
    assertEquals(List(Modify(0, 0)), actions)
  }

  @Test
  def testModify2() {
    val xs1 = IndexedSeq(6, 7)

    val actions = MinCostEdit[Int](xs1, xs1).steps
    assertEquals(List(Modify(0, 0), Modify(1, 1)), actions)
  }

  @Test
  def testModify3() {
    val xs1 = IndexedSeq(3, 2)
    val xs2 = IndexedSeq(1, 3, 2)

    val actions = MinCostEdit[Int](xs1, xs2).steps
    assertEquals(List(Insert(0, 0), Modify(0, 1), Modify(1, 2)), actions)
  }

  @Test
  def testInsert1() {
    val xs1 = IndexedSeq.empty
    val xs2 = IndexedSeq(1)

    val actions = MinCostEdit[Int](xs1, xs2).steps
    assertEquals(List(Insert(0, 0)), actions)
  }

  @Test
  def testInsert2() {
    val xs1 = IndexedSeq(1)
    val xs2 = IndexedSeq(1, 2)

    val actions = MinCostEdit[Int](xs1, xs2).steps
    assertEquals(List(Modify(0, 0), Insert(1, 1)), actions)
  }

  @Test
  def testInsert3() {
    val xs1 = IndexedSeq(2)
    val xs2 = IndexedSeq(1, 2)

    val actions = MinCostEdit[Int](xs1, xs2).steps
    assertEquals(List(Insert(0, 0), Modify(0, 1)), actions)
  }

  @Test
  def testDelete1() {
    val xs1 = IndexedSeq(1)
    val xs2 = IndexedSeq.empty

    val actions = MinCostEdit[Int](xs1, xs2).steps
    assertEquals(List(Remove(0, 0)), actions)
  }

  @Test
  def testDelete2() {
    val xs1 = IndexedSeq(1, 2)
    val xs2 = IndexedSeq(1)

    val actions = MinCostEdit[Int](xs1, xs2).steps
    assertEquals(List(Modify(0, 0), Remove(1, 1)), actions)
  }

  @Test
  def testDelete3() {
    val xs1 = IndexedSeq(1, 2)
    val xs2 = IndexedSeq(2)

    val actions = MinCostEdit[Int](xs1, xs2).steps
    assertEquals(List(Remove(0, 0), Modify(1, 0)), actions)
  }
}
