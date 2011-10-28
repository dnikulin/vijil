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
import net.liftweb.json.Serialization._

import org.junit.Test
import org.junit.Assert._

import org.xerial.snappy.Snappy

class TestTextSpan {
  val hash = "hash"
  val data = "Vigil is distributed in the hope that it will be useful"

  @Test
  def testJSON() {
    val span1 = new TextSpan(data, hash, 3, 7)
    assertEquals(3, span1.min)
    assertEquals(7, span1.max)
    assertEquals(4, span1.len)

    val json1 = span1.toJson

    assertEquals("""["hash",3,7,[],[]]""", json1)

    // Inject text mapping.
    val texts = Map(hash -> new TextFile(data, hash))
    TextMapInjector.register(texts.get(_))

    val span2 = TextSpan.fromJson(json1).get

    // Must encode and decode without changes.
    val json2 = span2.toJson
    assertEquals(json1, json2)
  }

  @Test
  def testNestedJSON() {
    val tag1  = Tag("foo1", "bar1")
    val tag2  = Tag("foo2", "bar2")

    val span1 = new TextSpan(data, hash, 3, 4, List(tag2, tag1))
    val span2 = new TextSpan(data, hash, 1, 6, List(tag1), List(span1))

    val json1 = span2.toJson

    assertEquals(
        """["hash",1,6,[["foo1","bar1"]],[["hash",3,4,[["foo2","bar2"],["foo1","bar1"]],[]]]]""",
        json1
    )

    // Inject text mapping.
    val texts = Map(hash -> new TextFile(data, hash))
    TextMapInjector.register(texts.get(_))

    // Test restoration from injection.
    val span3 = (TextSpan.fromJson(json1).get)

    // Must form distinct objects for items outside of text injection.
    assertNotSame(span1,       span2      )
    assertNotSame(span1,       span3      )
    assertNotSame(span2,       span3      )
    assertNotSame(span2.spans, span3.spans)
    assertNotSame(span2.tags,  span3.tags )

    // Must have correctly referenced hash and data from injection.
    assertSame(span1.data, span3.data)
    assertSame(span2.data, span3.data)
    assertSame(span1.hash, span3.hash)
    assertSame(span2.hash, span3.hash)

    // Must encode and decode without changes.
    val json2 = span3.toJson
    assertEquals(json1, json2)

    // Must compress well, and in a repeatable form, with Snappy.
    val snap1 = Snappy.compress(json1)
    val snap2 = Snappy.compress(json2)
    assertTrue(snap1.length < json1.length)
    assertNotSame(snap1, snap2)
    assertArrayEquals(snap1, snap2)

    // Must uncompress to the original string.
    val json3 = Snappy.uncompressString(snap1)
    val json4 = Snappy.uncompressString(snap1)
    assertNotSame(json1, json3)
    assertNotSame(json2, json3)
    assertNotSame(json3, json4)
    assertEquals (json2, json3)
    assertEquals (json2, json4)
  }
}
