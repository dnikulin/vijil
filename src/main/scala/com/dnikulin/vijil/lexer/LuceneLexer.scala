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

package com.dnikulin.vijil.lexer

import java.io.StringReader

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.util.Version

import com.dnikulin.vijil.model.Symbols
import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.model.TextModelBuilder
import com.dnikulin.vijil.text.TextFile

class LuceneLexer extends SimpleTextLexer {
  protected def newAnalyzer(): Analyzer =
    new StandardAnalyzer(Version.LUCENE_35)

  protected def termToSymbol(term: String): Int =
    ((term.hashCode & 0x3ffffff0) + Symbols.offset)

  override def apply(text: TextFile, builder: TextModelBuilder): Unit = {
    val analyzer = newAnalyzer()
    val reader   = new StringReader(text.data)
    val stream   = analyzer.tokenStream("body", reader)

    stream.reset()

    while (stream.incrementToken) {
      // Retrieve term and span attributes.
      val term     = stream.getAttribute(classOf[CharTermAttribute]).toString
      val offset   = stream.getAttribute(classOf[OffsetAttribute])

      // Only record letters in each word.
      val term1    = term.filter(_.isLetter)

      // Only record long words.
      if (term1.length >= 2) {
        // Convert term hash to integer symbol.
        val symbol = termToSymbol(term.toString)

        // Extract span in original data.
        val cmin   = offset.startOffset
        val cmax   = offset.endOffset
        val clen   = (cmax - cmin)

        // Verify span.
        assert(cmin >= 0)
        assert(cmax <= text.data.length)
        assert(cmax >  cmin)
        assert(clen >  0)

        if (clen <= 100) {
          // Record in builder.
          builder.add(symbol, cmin, clen.toByte)
        }
      }
    }

    stream.end()
    stream.close()
    reader.close()
  }
}
