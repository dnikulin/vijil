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

import org.junit.Test
import org.junit.Assert._

import com.dnikulin.vijil.text.TextSpan

object TestRenderString {
  import NodeSpan._

  def wrap(data: String, cmin: Int, cmax: Int, wrapper: Wrap): NodeSpan =
    NodeSpan(TextSpan(data, data, cmin, cmax), wrapper)

  def wrap(data: String, cmin: Int, cmax: Int, wrapper: Wrap, depth: Int): NodeSpan =
    NodeSpan(TextSpan(data, data, cmin, cmax), wrapper, depth)

  def run(spans: List[NodeSpan]): String = {
    val data = spans.head.span.data
    RenderString(TextSpan(data, data), spans).toString
  }
}

class TestRenderString {
  import NodeSpan._
  import TestRenderString._

  val line = "plain bold italic remove !"

  @Test
  def testPlain(): Unit = {
    val have = RenderString(TextSpan(line, line), Nil).toString
    assertEquals(line, have)
  }

  @Test
  def testFlat(): Unit = {
    val spans = List(
      wrap(line,  0,  5, identity),
      wrap(line,  6, 10, bold),
      wrap(line, 11, 17, italic),
      wrap(line, 17, 24, empty)
    )

    val want = "plain <b>bold</b> <i>italic</i> !"
    val have = run(spans)
    assertEquals(want, have)
  }

  @Test
  def testNest(): Unit = {
    val spans = List(
      wrap(line,  0,  5, identity),
      wrap(line,  6, 17, bold),
      wrap(line, 11, 17, italic),
      wrap(line, 17, 24, empty)
    )

    val want = "plain <b>bold <i>italic</i></b> !"
    val have = run(spans)
    assertEquals(want, have)
  }

  @Test
  def testOverlap(): Unit = {
    val spans = List(
      wrap(line,  0,  5, identity),
      wrap(line,  6, 14, bold),
      wrap(line, 11, 17, italic),
      wrap(line, 16, 18, bold),
      wrap(line, 18, 24, empty)
    )

    val want = "plain <b>bold <i>ita</i></b><i>li<b>c</b></i><b> </b> !"
    val have = run(spans)
    assertEquals(want, have)
  }

  @Test
  def testDepth(): Unit = {
    val spans = List(
      wrap(line,  6, 10, bold,   1),
      wrap(line,  6, 10, italic, 2),
      wrap(line, 11, 17, bold,   2),
      wrap(line, 11, 17, italic, 1),
      wrap(line, 17, 24, empty)
    )

    val want = "plain <b><i>bold</i></b> <i><b>italic</b></i> !"
    val have = run(spans)
    assertEquals(want, have)
  }

  @Test
  def testOverlapDepth1(): Unit = {
    val spans = List(
      wrap(line,  6, 17, bold,   2),
      wrap(line,  6, 10, italic, 1),
      wrap(line, 11, 17, italic, 1),
      wrap(line, 17, 24, empty)
    )

    val want = "plain <i><b>bold</b></i><b> </b><i><b>italic</b></i> !"
    val have = run(spans)
    assertEquals(want, have)
  }

  @Test
  def testOverlapDepth2(): Unit = {
    val spans = List(
      wrap(line,  6, 17, bold,   1),
      wrap(line,  6, 10, italic, 2),
      wrap(line, 11, 17, italic, 2),
      wrap(line, 17, 24, empty)
    )

    val want = "plain <b><i>bold</i> <i>italic</i></b> !"
    val have = run(spans)
    assertEquals(want, have)
  }
}
