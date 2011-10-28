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

class TestTextFile {
  implicit val formats = DefaultFormats

  val name = """Text "name"."""
  val hash = """Text "hash"."""
  val data = """Text "data"."""

  @Test
  def testNestedJSON() {
    val tag1  = Tag("foo1", "bar1")
    val tag2  = Tag("foo2", "bar2")
    val tag3  = Tag("title", name)

    val span1 = new TextSpan(data, hash, 3, 4, List(tag2, tag1))
    val span2 = new TextSpan(data, hash, 1, 6, List(tag1), List(span1))

    val note1 = new TextNote(7, "lab3l", "b0dy")

    val text1 = new TextFile(data, hash, List(tag2, tag3, tag1), List(span1, span2), List(note1))

    // Must correctly extract name from tags.
    assertEquals(name, text1.name)

    val json1 = text1.toJson

    assertEquals(
        """{"data":"Text \"data\".","hash":"Text \"hash\".",""" +
        """"tags":[["foo2","bar2"],["title","Text \"name\"."],["foo1","bar1"]]""" +
        ""","spans":[[3,4,[["foo2","bar2"],["foo1","bar1"]],[]],""" +
        """[1,6,[["foo1","bar1"]],[[3,4,[["foo2","bar2"],["foo1","bar1"]],[]]]]]""" +
        ""","notes":[[7,"lab3l","b0dy"]]}""",
        json1
    )

    val text2 = TextFile.fromJson(json1).get

    // Must form distinct objects.
    assertNotSame(text1,       text2      )
    assertNotSame(text1.hash,  text2.hash )
    assertNotSame(text1.data,  text2.data )
    assertNotSame(text1.spans, text2.spans)
    assertNotSame(text1.tags,  text2.tags )

    // Must preserve strings exactly.
    assertEquals(text1.hash, text2.hash)
    assertEquals(text1.data, text2.data)

    // Must encode and decode without changes.
    val json2 = text2.toJson
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
