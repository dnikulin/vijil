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

import scala.collection.mutable.HashMap
import scala.io.Source

import java.io._
import java.lang.StrictMath.log

import com.dnikulin.vijil.tools.CleanString
import com.dnikulin.vijil.tools.Count

final case class TermCorpus(nfiles: Long, nterms: Long) {
  require(nfiles >= 0)
  require(nterms >= 0)
}

final case class TermCount(term: Lemma, corpus: TermCorpus, nfiles: Long, nterms: Long) {
  require(nfiles >= 0)
  require(nterms >= 0)
  require(nfiles <= corpus.nfiles)
  require(nterms <= corpus.nterms)

  val idf   : Double = log(corpus.nfiles.toDouble   / (nfiles.toDouble max 1.0D))
  val tf    : Double = ((nterms.toDouble * 1000.0D) / corpus.nterms.toDouble)

  assert(idf >= 0)
  assert(tf  >= 0)
}

final case class TermFactor(inCorpus: TermCount, inQuery: TermCount) {
  require(inCorpus.term eq inQuery.term)

  val term  : Lemma  = inCorpus.term
  val tfidf : Double = (inQuery.tf * inCorpus.idf)

  assert(tfidf >= 0)
}

class WordCount(val words: Words, private val counts: HashMap[Lemma, TermCount]) {
  def apply(lemma: Lemma): Option[TermCount] =
    counts.get(lemma)

  def apply(word: String): Option[TermCount] =
    Option(words.lemma(word)).flatMap(apply)

  def apply(inQuery: TermCount): Option[TermFactor] =
    apply(inQuery.term).map(TermFactor(_, inQuery))
}

object WordCount {
  def apply(words: Words, file: File): WordCount =
    apply(words, new BufferedInputStream(new FileInputStream(file)))

  def apply(words: Words, stream: InputStream): WordCount = {
    val counts = new HashMap[Lemma, TermCount]
    var corpus = Option.empty[TermCorpus]

    var ngood  = 0L
    var ncode  = 0L
    var nmiss  = 0L

    for (line <- Source.fromInputStream(stream, "UTF-8").getLines) {
      CleanString.whitePattern.split(line.trim) match {
        case Array(snterms, "terms", "in", snfiles, "files") =>
          if (corpus.isDefined)
            throw new IllegalArgumentException("Word count file repeats 'terms in files' line")

          corpus = Some(TermCorpus(snfiles.toLong, snterms.toLong))

        case Array(scount, sinFiles, scode, word) =>
          if (corpus.isEmpty)
            throw new IllegalArgumentException("Word count file has invalid 'files' line")

          Option(words.lemma(word)) match {
            case Some(lemma) =>
              counts(lemma) = TermCount(lemma, corpus.get, sinFiles.toLong, scount.toLong)

              if (lemma.symbol == scode.toLong)
                ngood += 1 // Count perfect match.
              else
                ncode += 1 // Count word found but with wrong symbol.

            case _ =>
              nmiss += 1   // Count word not found.
          }

        case _ =>
      }
    }

    stream.close()

    val nfull = (ngood + ncode + nmiss)

    println("WordCount read %d terms: %d terms correctly, %d terms with an incorrect code, %d terms with no known lemma".format
        (nfull, ngood, ncode, nmiss))

    new WordCount(words, counts)
  }
}
