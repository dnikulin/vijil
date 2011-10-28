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

package com.dnikulin.vijil.scripts

import scala.collection.mutable.HashMap
import scala.xml._

import java.io._
import java.util.regex.Pattern

import org.apache.commons.lang.StringUtils.getLevenshteinDistance

import com.dnikulin.vijil.parse._
import com.dnikulin.vijil.text._
import com.dnikulin.vijil.tools._

object BlackstoneCharCostModel extends CostModel[Char] {
  // Use 1 as unit cost.
  override def unit(a: Char): Long =
    1

  override def pair(a: Char, b: Char): Long = (a, b) match {
    case // Zero cost for OCR-damaged letters.
      ('h', 'n') | ('n', 'h') |
      ('f', 'r') | ('r', 'f') |
      ('f', 's') | ('s', 'f') |
      ('t', 's') | ('s', 't') |
      ('f', 't') | ('t', 'f') |
      ('l', 't') | ('t', 'l') => 0

    case // Zero cost for identical letters.
      (ac, bc) if (ac  == bc) => 0

    case // Unit cost any change.
      _ => 1
  }
}

object BlackstoneStringCostModel extends CostModel[String] {
  // Use length as string unit cost.
  override def unit(a: String): Long =
    a.length

  // Use edit distance with exceptions for edit cost.
  override def pair(a: String, b: String): Long = {
    // Shortcut for majority case.
    if (a equals b)
      return 0

    MinCostEdit[Char](a, b)(BlackstoneCharCostModel).cost
  }
}

object Difftex {
  val opath = new File("diff-v1.tex")

  // Reuse string cost model for simple edit distance.
  import BlackstoneStringCostModel.{pair => editCost}

  def getLetters(s: String): String = {
    CleanString.getLettersDigits(s).
    replace('h', 'n').
    replace('f', 't').
    replace('s', 't').
    replace('l', 't').
    replace('r', 't')
  }

  def main(args: Array[String]): Unit = {
    val paths = {
      for (edition <- (1 to 7))
        yield new File("data/V1-E%d.xml".format(edition))
    }

    val texts = readTexts(paths)

    // Create file writer.
    val writer = new PrintWriter(new BufferedWriter(new FileWriter(opath)))
    import writer.{print, println}

    def sprint(s: String) {
      print(escapeAll(s))
    }

    println("\\documentclass[10pt,a4paper]{article}")
    println("\\usepackage[normalem]{ulem}")
    println("\\usepackage{color}")
    println("\\usepackage{fullpage}")
    println("\\usepackage[bookmarks]{hyperref}")
    println("\\setlength{\\parindent}{0pt}")
    println("\\setlength{\\parskip}{0.25cm}")
    println("\\author{Factotum}")
    println("\\title{Differences in Commentaries on the Laws of England, Volume 1}")
    println("\\begin{document}")
    println()
    println("\\maketitle")
    println()
    println("\\setcounter{tocdepth}{2}")
    println("\\tableofcontents")
    println("\\newpage")
    println()

    // Take root spans of each text.
    val roots    = texts.flatMap(_.spans)

    // Take chapter list per root span.
    val chapters = roots.map(_.spans)

    // Transpose chapter lists.
    // (text, chapter) -> (chapter, text)
    for (chapter <- chapters.transpose.drop(3).take(1)) {
      println("\\section{%s}".format(chapter.head.name.split("\\), ", 2).last.trim))
      println()

      chapter.reduce{(e1, e2) =>
        // Announce new edition.
        println("\\subsection{%s $\\Rightarrow$ %s}".format(getEdition(e1.name), getEdition(e2.name)))
        println()

        // Refer to data.
        import e1.{data => d1}
        import e2.{data => d2}

        import FindSpans.sentences
        import FindSpans.wordsNumbers

        import Differ._

        // Split paragraph spans into sentences.
        val sents1 = DifferTokens(e1, sentences(e1).toArray, getLetters)
        val sents2 = DifferTokens(e2, sentences(e2).toArray, getLetters)

        // Cost model for string edits, used by each minCostAlign().
        implicit val model = BlackstoneStringCostModel

        Differ(sents1, sents2) {
          case Changed(sent1, sent2, change) =>
            assert(sent1.data eq d1)
            assert(sent2.data eq d2)

            val sub1 = sent1.substring.trim
            val sub2 = sent2.substring.trim

            val let1 = getLetters(sub1)
            val let2 = getLetters(sub2)

            change match {
              case Del =>
                if (let1.length > 0) {
                  println("\\subsubsection*{Erased text}")
                  println()
                  println("{\\color{red}\\sout{%s}}".format(escapeAll(sub1)))
                  println()
                }

              case Add =>
                if (let2.length > 0) {
                  println("\\subsubsection*{Added text}")
                  println()
                  println("{\\color{blue}\\uline{%s}}".format(escapeAll(sub2)))
                  println()
                }

              case Mod =>
                if (getLevenshteinDistance(let1, let2) > 0) {
                  // Re-split edited section into sentences.
                  val sents1B = DifferTokens(sent1, sentences(sent1).toArray, getLetters)
                  val sents2B = DifferTokens(sent2, sentences(sent2).toArray, getLetters)

                  // Perform min-cost alignment.
                  minCostAlign(sents1B, sents2B) {
                    case Changed(sent1, sent2, change) =>
                      assert(sent1.data eq d1)
                      assert(sent2.data eq d2)

                      val sub1 = sent1.substring.trim
                      val sub2 = sent2.substring.trim

                      val let1 = getLetters(sub1)
                      val let2 = getLetters(sub2)

                      change match {
                        case Del =>
                          if (let1.length > 0) {
                            println("\\subsubsection*{Erased text}")
                            println()
                            println("{\\color{red}\\sout{%s}}".format(escapeAll(sub1)))
                            println()
                          }

                        case Add =>
                          if (let2.length > 0) {
                            println("\\subsubsection*{Added text}")
                            println()
                            println("{\\color{blue}\\uline{%s}}".format(escapeAll(sub2)))
                            println()
                          }

                        case Mod if (getLevenshteinDistance(let1, let2) > 0) =>
                          // Wastefully re-calculate edit distance.
                          val score = getLevenshteinDistance(let1, let2)

                          // Split sentence spans into words.
                          val words1 = DifferTokens(sent1, wordsNumbers(sent1).toArray, getLetters)
                          val words2 = DifferTokens(sent2, wordsNumbers(sent2).toArray, getLetters)

                          println("\\subsubsection*{Edited text $(\\Delta \\  %d)$}".format(score))
                          println()

                          var cursor1 = sent1.min
                          def catchup1(to: Int) {
                            if (cursor1 < to)
                              print(escapeAll(d1.substring(cursor1, to)))
                            cursor1 = to
                          }

                          Differ(words1, words2) {
                            case Changed(word1, word2, change) =>
                              assert(word1.data eq d1)
                              assert(word2.data eq d2)

                              change match {
                                case Del | Mod =>
                                  catchup1(word1.min)
                                  print(" {\\color{red}\\sout{%s}} ".format(escapeAll(word1.substring)))
                                  cursor1 = word1.max

                                case _ =>
                              }
                          }

                          catchup1(sent1.max)
                          println()
                          println()

                          var cursor2 = sent2.min
                          def catchup2(to: Int) {
                            if (cursor2 < to)
                              print(escapeAll(d2.substring(cursor2, to)))
                            cursor2 = to
                          }

                          Differ(words1, words2) {
                            case Changed(word1, word2, change) =>
                              assert(word1.data eq d1)
                              assert(word2.data eq d2)

                              change match {
                                case Add | Mod =>
                                  catchup2(word2.min)
                                  print(" {\\color{blue}\\uline{%s}} ".format(escapeAll(word2.substring)))
                                  cursor2 = word2.max

                                case _ =>
                              }
                          }

                          catchup2(sent2.max)
                          println()
                          println()

                        case _ =>
                          // Ignore small edits.
                      }
                  }
                }
            }
        }

        e2
      }
    }

    println()
    println("\\end{document}")
    println()

    writer.flush()
    writer.close()
  }

  def plural(s: String, c: Int): String =
    if (c > 1) (s + "s") else s

  def escapeAll(s: String): String =
    CleanString.cleanWhite(List("&", "$", "{", "}", "%", "#", "_").foldLeft(s)(escape))

  def escape(s: String, p: String): String =
    s.replace(p, "\\" + p)

  def readTexts(paths: Seq[File]): Seq[TextFile] = {
    for (path <- paths) yield {
      println("Reading [%s]".format(path))
      for (node <- XML.loadFile(path); text <- ReadFactotumXML(node))
        yield text
    }
  }.flatten

  def getEdition(name: String): String =
    name.replaceAll("^.*(Edition [0-9]+).*$", "$1")
}
