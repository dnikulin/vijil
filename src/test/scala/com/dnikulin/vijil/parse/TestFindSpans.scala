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

package com.dnikulin.vijil.parse

import org.junit.Test
import org.junit.Assert._

import com.dnikulin.vijil.text.TextSpan

class TestFindSpans {
  import FindSpans.words
  import FindSpans.sentences
  import FindSpans.paragraphs
  import FindSpans.wordBounds

  private def test(method: (StringSpan => List[StringSpan]), body: String): Array[String] = {
    // Create pad string surrounding the real string.
    // This helps to test that the returned spans are from the original string.
    val pad1  = "test pre pad "
    val pad2  = " post test pad"
    val data  = (pad1 + body + pad2)

    // Create a root span that contains exactly the real body.
    val cmin  = pad1.length
    val cmax  = pad1.length + body.length
    val span  = new TextSpan(data, data, cmin, cmax)

    // Apply finding method to create new spans.
    val spans = method(span)

    for (span2 <- spans) {
      // Assert that the new spans are strictly within the root span.
      assertSame(span.data, span2.data)
      assertTrue(span2.min >= cmin)
      assertTrue(span2.max <= cmax)
    }

    // Return the new spans for task-specific assertions.
    // Map them to their substrings for easy equality.
    return spans.map(_.substring).toArray
  }

  @Test
  def testFindWords1(): Unit = {
    val body = ""
    val out  = test(words, body)

    assertEquals(0, out.length)
  }

  @Test
  def testFindWords2(): Unit = {
    val body = " + - "
    val out  = test(words, body)

    assertEquals(0, out.length)
  }

  @Test
  def testFindWords3(): Unit = {
    val body = "test 0fi'nd + 'wor1ds"
    val out  = test(words, body)

    assertEquals(3, out.length)
    assertEquals("test",   out(0))
    assertEquals("fi'nd",  out(1))
    assertEquals("wor1ds", out(2))
  }

  @Test
  def testFindWordBounds1(): Unit = {
    val body = ""
    val out  = test(wordBounds, body)

    assertEquals(0, out.length)
  }

  @Test
  def testFindWordBounds2(): Unit = {
    val body = " + - "
    val out  = test(wordBounds, body)

    assertEquals(2,   out.length)
    assertEquals("+", out(0))
    assertEquals("-", out(1))
  }

  @Test
  def testFindWordBounds3(): Unit = {
    val body = "test  1 0fi'nd + 'wor1ds"
    val out  = test(wordBounds, body)

    assertEquals(8,         out.length)
    assertEquals("test",    out(0))
    assertEquals("1",       out(1))
    assertEquals("0fi",     out(2))
    assertEquals("'",       out(3))
    assertEquals("nd",      out(4))
    assertEquals("+",       out(5))
    assertEquals("'",       out(6))
    assertEquals("wor1ds",  out(7))
  }

  @Test
  def testFindSentences1(): Unit = {
    val body = ""
    val out  = test(sentences, body)

    assertEquals(0, out.length)
  }

  @Test
  def testFindSentences2(): Unit = {
    val body = "+ 1 / ; ' 312"
    val out  = test(sentences, body)

    assertEquals(1, out.length)
    assertEquals(body, out(0))
  }

  @Test
  def testFindSentences3(): Unit = {
    val body = "test find + words"
    val out  = test(sentences, body)

    assertEquals(1, out.length)
    assertEquals(body, out(0))
  }

  @Test
  def testFindSentences4(): Unit = {
    val sen1 = "This is a sentence?"
    val sen2 = "This is. one too"
    val body = sen1 + " " + sen2
    val out  = test(sentences, body)

    assertEquals(2, out.length)
    assertEquals(sen1, out(0))
    assertEquals(sen2, out(1))
  }

  @Test
  def testFindSentences5(): Unit = {
    val sen1 = "This is a sentence !"
    val sen2 = "\" This is. one too \""
    val body = sen1 + " " + sen2
    val out  = test(sentences, body)

    assertEquals(2, out.length)
    assertEquals(sen1, out(0))
    assertEquals(sen2, out(1))
  }

  @Test
  def testFindSentences6(): Unit = {
    val sen1 = "\" This is a sentence .\""
    val sen2 = "This is. one too"
    val body = sen1 + " " + sen2
    val out  = test(sentences, body)

    assertEquals(2, out.length)
    assertEquals(sen1, out(0))
    assertEquals(sen2, out(1))
  }

  @Test
  def testFindSentences7(): Unit = {
    val sen1 = "- This is a sentence."
    val sen2 = "- This is. one too."
    val body = sen1 + " " + sen2
    val out  = test(sentences, body)

    assertEquals(2, out.length)
    assertEquals(sen1, out(0))
    assertEquals(sen2, out(1))
  }

  @Test
  def testFindSentences8(): Unit = {
    val body = "http://this.is/one/sentence"
    val out  = test(sentences, body)

    assertEquals(1, out.length)
    assertEquals(body, out(0))
  }

  @Test
  def testFindSentences9(): Unit = {
    // No clear separation, so expect to keep this as one sentence.
    val body = "A fine sentence by Mr. Robert. E. Lee. Another fine sentence by Dr. James."
    val out  = test(sentences, body)

    assertEquals(1, out.length)
    assertEquals(body, out(0))
  }

  @Test
  def testFindSentences10(): Unit = {
    // Separate by words with a non-capital leading letter.
    val sen1 = "A fine sentence by Mr. Robert. E. Lee. completed !"
    val sen2 = "Another fine sentence by Dr. James."
    val body = sen1 + " " + sen2
    val out  = test(sentences, body)

    assertEquals(2, out.length)
    assertEquals(sen1, out(0))
    assertEquals(sen2, out(1))
  }

  @Test
  def testFindParagraphs1(): Unit = {
    val body = ""
    val out  = test(paragraphs, body)

    assertEquals(0, out.length)
  }

  @Test
  def testFindParagraphs2(): Unit = {
    val body = "  \n  \n "
    val out  = test(paragraphs, body)

    assertEquals(0, out.length)
  }

  @Test
  def testFindParagraphs3(): Unit = {
    val body = "line1\nline2\r\nline3\rline4\nline5\rline6\n\rline7"
    val out  = test(paragraphs, body)

    assertEquals(1, out.length)
    assertEquals(body, out(0))
  }

  @Test
  def testFindParagraphs4(): Unit = {
    val par1 = "line1\nline2\r\nline3\rline4"
    val par2 = "line5\rline6\n\rline7"
    val body = "\t\r %s  \r\n  \t \n  %s \n  ".format(par1, par2)
    val out  = test(paragraphs, body)

    assertEquals(2, out.length)
    assertEquals(par1, out(0))
    assertEquals(par2, out(1))
  }
}
