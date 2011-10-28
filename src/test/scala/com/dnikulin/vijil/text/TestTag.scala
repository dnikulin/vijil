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

package com.dnikulin.vijil.text

import net.liftweb.json._

import org.junit.Test
import org.junit.Assert._

class TestTag {
  @Test
  def testJSON() {
    val tag1 = Tag("foo", "bar")
    assertEquals("foo", tag1.name)
    assertEquals("bar", tag1.value)

    val json = tag1.toJson

    assertEquals("""["foo","bar"]""", json)

    val tag2 = Tag.fromJson(json).get

    assertNotSame (tag1,       tag2      )
    assertEquals  (tag1.name,  tag2.name )
    assertEquals  (tag1.value, tag2.value)
    assertEquals  (tag1,       tag2      )
  }
}
