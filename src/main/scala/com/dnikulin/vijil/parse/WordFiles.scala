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

import java.io.BufferedInputStream
import java.io.InputStream

import scala.io.Source

import com.dnikulin.vijil.data.VijilData
import com.dnikulin.vijil.tools.CleanString

object WordFiles {
  import CleanString.whitePattern

  type Reader = ((Words, InputStream) => Unit)

  def readLemmas(words: Words, stream: InputStream) {
    for (line <- Source.fromInputStream(stream).getLines) {
      if (line.startsWith("#") == false) {
        val forms = whitePattern.split(line.trim.toLowerCase)
        words.addWords(forms)
      }
    }
  }

  def readExtraWords(words: Words, stream: InputStream) {
    for (line <- Source.fromInputStream(stream).getLines) {
      if (line.startsWith("#") == false) {
        val tokens = whitePattern.split(line.trim.toLowerCase)
        for (token <- tokens)
          words.addExtraWord(token)
      }
    }
  }

  def readSynsets(words: Words, stream: InputStream) {
    for (line <- Source.fromInputStream(stream).getLines) {
      if (line.startsWith("#") == false) {
        val synset = Array(words.startSynset())

        for (word <- whitePattern.split(line.trim.toLowerCase)) {
          val lemma = words.lemma(word)
          if (lemma ne null)
            lemma.synsets ++= synset
        }
      }
    }
  }

  def readStopwords(words: Words, stream: InputStream) {
    for (line <- Source.fromInputStream(stream).getLines) {
      if (line.startsWith("#") == false) {
        for (word <- whitePattern.split(line.trim.toLowerCase)) {
          val lemma = words.lemma(word)
          if (lemma ne null)
            lemma.stopword = true
        }
      }
    }
  }

  def readNonStopwords(words: Words, stream: InputStream) {
    for (line <- Source.fromInputStream(stream).getLines) {
      if (line.startsWith("#") == false) {
        for (word <- whitePattern.split(line.trim.toLowerCase)) {
          val lemma = words.lemma(word)
          if (lemma ne null)
            lemma.stopword = false
        }
      }
    }
  }

  def fromFile(words: Words, reader: Reader, leaf: String) {
    val file = VijilData.open(leaf)
    val buffer = new BufferedInputStream(file)
    try {
      reader(words, buffer)
    } finally {
      buffer.close()
    }
  }

  def readDictionary(words: Words) {
    def read(reader: Reader, leaf: String): Unit =
      fromFile(words, reader, leaf)

    read(readLemmas,        "EnglishWord.txt")
    read(readExtraWords,    "WikiTitles.txt")
    read(readStopwords,     "EnglishStop.txt")

    // Read numbers, and mark them as non-stopwords.
    read(readLemmas,        "MultiNumbers.txt")
    read(readNonStopwords,  "MultiNumbers.txt")
  }

  def readMultiDictionary(words: Words) {
    def read(reader: Reader, leaf: String): Unit =
      fromFile(words, reader, leaf)

    read(readLemmas,        "MultiWord.txt")
    read(readExtraWords,    "WikiTitles.txt")
    read(readStopwords,     "EnglishStop.txt")
    read(readStopwords,     "LatinStop.txt")
    read(readSynsets,       "EnglishSyn.txt")

    // Read numbers, and mark them as non-stopwords.
    read(readLemmas,        "MultiNumbers.txt")
    read(readNonStopwords,  "MultiNumbers.txt")
  }
}
