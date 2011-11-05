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

import com.dnikulin.vijil.model.Symbols
import com.dnikulin.vijil.tools.Empty

class TestWords {
  import Words._
  import WordFiles._

  @Test
  def testWords(): Unit = {
    val words = new Words
    import words.lemma

    // Must default to non-filling.
    assertFalse(words.isFilling)

    // Must default to skipping.
    assertTrue(words.skipStopwords)

    def read(reader: Reader, leaf: String): Unit =
      fromFile(words, reader, leaf)

    // Must have lemmas below the lemma offset.
    assertNotNull(lemma(0))
    assertNotNull(lemma(1))
    assertNotNull(lemma(Symbols.offset))

    // Must have no words to start with.
    assertNull(lemma("every"))
    assertNull(lemma("coffee"))
    assertNull(lemma(Symbols.offset + 1))
    assertNull(lemma(70))

    // Must not give exceptions reading a lemma file.
    read(readLemmas, "EnglishWord.txt")

    // Must now contain words.
    assertNotNull(lemma("every"))
    assertNotNull(lemma("coffee"))
    assertNotNull(lemma(70))

    // Must correctly map symbols.
    for (symbol <- Array(18, 41, 90, 1832))
      assertEquals(symbol, lemma(symbol).symbol)

    // Must correctly equate words.
    assertSame    (lemma("coffee"),    lemma("coffees"))
    assertSame    (lemma("encoder"),   lemma("encodings"))
    assertSame    (lemma("excellent"), lemma("excellence"))
    assertNotSame (lemma("coffee"),    lemma("excellence"))
    assertNotSame (lemma("encoder"),   lemma("excellence"))

    // Must not yet have numbers.
    assertNull(lemma("7"))
    assertNull(lemma("20"))
    assertNull(lemma("7th"))
    assertNull(lemma("vii"))
    assertNull(lemma("septimus"))
    assertNull(lemma("vicensiumus"))

    // Must not give exceptions reading a lemma file of numbers.
    // Some of these will *override* existing lemmas.
    read(readLemmas, "MultiNumbers.txt")

    // Must correctly equate numbers.
    assertSame    (lemma("7"),         lemma("7th"))
    assertSame    (lemma("7"),         lemma("vii"))
    assertSame    (lemma("7"),         lemma("septimus"))
    assertSame    (lemma("20"),        lemma("vicensiumus"))
    assertNotSame (lemma("7"),         lemma("20"))
    assertNotSame (lemma("7"),         lemma("vicensiumus"))

    // Must not yet have Wikipedia titles.
    assertNull(lemma("corba"))
    assertNull(lemma("yukimori"))
    assertNull(lemma("xhtml"))

    // Must not yet mark any stopwords.
    assertFalse(lemma("whenever").stopword)
    assertFalse(lemma("every").stopword)
    assertFalse(lemma("often").stopword)

    // Must not give exceptions reading an extras file.
    read(readExtraWords, "WikiTitles.txt")

    // Must now yet have Wikipedia titles.
    assertNotNull(lemma("corba"))
    assertNotNull(lemma("yukimori"))
    assertNotNull(lemma("xhtml"))

    // Must not give exceptions reading a stopword file.
    read(readStopwords, "EnglishStop.txt")

    // Must now mark stopwords.
    assertTrue(lemma("whenever").stopword)
    assertTrue(lemma("every").stopword)
    assertTrue(lemma("often").stopword)

    // Must not mark non-stopwords as stopwords.
    assertFalse(lemma("coffee").stopword)
    assertFalse(lemma("encoder").stopword)
    assertFalse(lemma("excellent").stopword)

    // Must parse simple sentences, ignoring case, skipping stopwords.
    val data = "coffee is always wiNnIng confabulationnaly and 7"
    val parsed = words.parse(data, "hash", Empty.bytes)
    assertEquals(3, parsed.size)
    assertEquals(parsed.symbol(0), lemma("coffees").symbol)
    assertEquals(parsed.symbol(1), lemma("winningnesses").symbol)
    assertEquals(parsed.symbol(2), lemma("septimus").symbol)
    assertEquals(0,  parsed.offset(0))
    assertEquals(17, parsed.offset(1))
    assertEquals(47, parsed.offset(2))
    assertEquals(6,  parsed.length(0))
    assertEquals(7,  parsed.length(1))
    assertEquals(1,  parsed.length(2))
  }

  @Test
  def testFilling(): Unit = {
    val words = new Words(true)
    import words.lemma

    // Must remember filling mode.
    assertTrue(words.isFilling)

    val l1 = lemma("test")
    val l2 = lemma("test")
    val l3 = lemma("test")
    val l4 = lemma("tests")
    val l5 = lemma("tests")
    val l6 = lemma("tests")

    // Must create lemmas automatically, but without stemming.
    assertSame(l1, l2)
    assertSame(l1, l3)
    assertSame(l4, l5)
    assertSame(l4, l6)
    assertNotSame(l1, l4)
  }

  @Test
  def testNotFilling(): Unit = {
    val words = new Words(false)
    import words.lemma

    // Must remember filling mode.
    assertFalse(words.isFilling)

    // Must NOT create lemmas automatically.
    assertNull(lemma("test"))
    assertNull(lemma("tests"))
  }
}
