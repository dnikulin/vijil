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

class TestRunFilter {
  @Test
  def testCatEmpty(): Unit = {
    // Must succeed, but return nothing.
    val out = RunFilter("cat", Empty.bytes)
    assertTrue(out.isDefined)
    assertEquals(0, out.get.length)
  }

  @Test
  def testCatString(): Unit = {
    val in  = "foo bar".getBytes

    // Must succeed, and return exact input.
    val out = RunFilter("cat", in)
    assertTrue(out.isDefined)
    assertNotSame(in, out.get)
    assertArrayEquals(in, out.get)
  }

  @Test
  def testGzip(): Unit = {
    // Create a highly compressable string.
    val in  = (0 until 10000).map(_ => "test").mkString.getBytes

    // Must succeed, and return compressed input.
    val zip = RunFilter("gzip -9", in)
    assertTrue(zip.isDefined)
    assertNotSame(in, zip.get)
    assertTrue(zip.get.length < in.length)

    // Must succeed, and return decompressed input.
    val out = RunFilter("gzip -d", zip.get)
    assertTrue(out.isDefined)
    assertNotSame(in, out.get)
    assertNotSame(zip.get, out.get)
    assertEquals(in.length, out.get.length)
    assertArrayEquals(in, out.get)
  }
}
