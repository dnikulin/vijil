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

import com.google.common.io.Files

import com.dnikulin.vijil.tools.CleanString

object ReadXHTML {
  val chapterRex = Pattern.compile("s?c\\s*hapter\\s*the ")
  val sectionRex = Pattern.compile("s\\s*ection\\s*the ")

  val textClasses = List("NormalWeb", "PlainText", "Standard", "Chapnum", "Chaptitle", "Textflush", "Text", "cen", "Numlistflush")
  val noteClasses = List("Footnoteanchor", "FootnoteSymbol")

  def main(args: Array[String]): Unit = {
    for (book <- (1 to 4); edition <- (1 to 7)) {
      val ipath = new File("data/V%d-E%d.xhtml".format(book, edition))
      val opath = new File("data/V%d-E%d.xml"  .format(book, edition))
      val name  = "Commentaries (Volume %d, Edition %d)" .format(book, edition)

      if (ipath.canRead == true) {
        System.err.println("Converting [%s]...".format(ipath.getName))
        convert(ipath, opath, name)
      } else {
        System.err.println("Could not find [%s]...".format(ipath.getName))
      }
    }
  }

  def convert(ipath: File, opath: File, name: String) {
    // Create file writer.
    val writer = new PrintWriter(new BufferedWriter(new FileWriter(opath)))
    import writer.{print, println}

    // Read entire XML document directly.
    val xhtml = XML.loadFile(ipath)

    println("""<?xml version="1.0" encoding="UTF-8"?>""")
    println()

    println("""<text id="%s">""".format(name))
    println()

    // Find all footnotes.
    val notes   = findFootnotes(xhtml)

    // Keep state of current chapter title,
    // as chapters are split between lines.
    var chapNum = Option.empty[String]
    var insec   = false
    var nchaps  = 0

    def writeout(node: Node): Unit = node.child.foreach{
      case Text(body) =>
        print(Text(despacePunct(killdollars(cleanup(body)))).toString)

      case child
      if (noteClasses.contains((child \ "@class").text.trim)) =>
        val href = (child \\ "a" \ "@href").text.trim
        if (href.startsWith("#ftn")) {
          val num = href.drop(4).toInt
          notes.get(num) match {
            case Some(note) =>
              print((<note number={num.toString}>{note.trim}</note>).toString)
              // Consume entry.
              notes.remove(num)

            case _ =>
              System.err.println("Error: Missing footnote %d".format(num))
          }
        } else {
          writeout(child)
        }

      case child =>
        writeout(child)
    }

    for (p <- (xhtml \ "body" \\ "p")) {
      if (textClasses.contains((p \ "@class").text.trim)) {
        val trimLine = Utility.trimProper(p).text.toLowerCase.trim

        if (chapterRex.matcher(trimLine).find) {
          val line = cleanup(p.text).toLowerCase.trim
          val head = undot(chapterRex.matcher(line).replaceAll("Chapter the "))
          chapNum = Some(head)
        } else if (sectionRex.matcher(trimLine).find) {
          val line = cleanup(p.text).toLowerCase.trim
          val head = undot(sectionRex.matcher(line).replaceAll("Section the "))
          chapNum = Some(head)
        } else if (chapNum.isDefined && (cleanup(p.text).trim.length > 0)) {
          val num = chapNum.get
          chapNum = None

          if (insec == true) {
            println("""</section>""")
            println()
          }

          val capt = undot(cleanup(p.text).replace("O f ", "Of").replace("KING' s", "KING's").replace("Ofthe", "Of the"))

          println("""<section id="%s (%s)">""".format(num, capt))
          println()
          insec = true
          nchaps += 1

        } else if ((insec == true) && (p.text.trim.length > 0)) {
          writeout(p)
          println()
          println()
        }
      }
    }

    if (insec == true) {
      println("</section>")
      println()
    } else {
      System.err.println("Error: No chapter headings found")
    }

    println("</text>")
    println()

    writer.flush()
    writer.close()

    for ((num, note) <- notes.toList.sorted)
      System.err.println("Error: Unused footnote (%d) [%s]".format(num, note))

    System.err.println("Found %d chapters".format(nchaps))
  }

  def findFootnotes(xhtml: Node): HashMap[Int, String] = {
    val map = new HashMap[Int, String]

    val notes = (xhtml \ "body" \ "p").toList

    for (p <- (xhtml \ "body" \ "p")) {
      val num = (p \ "span" \ "a" \ "@id").text
      if (num.startsWith("ftn")) {
        val pre_a    = (p \ "span" \ "a").text
        val pre_sup  = (p \ "sup").text
        val pre_span = refs(p).text

        val snum = num.drop(3)
        var note = killdollars(killpre(killpre(killpre(cleanup(p.text), pre_a), pre_sup), pre_span))
        map(snum.toInt) = note
      }
    }

    return map
  }

  def undot(s: String): String =
    killdollars(s).trim.replaceAll("[\\s\\.\\*]+$", "").trim

  def killpre(s: String, p: String): String =
    killpre1(s.trim, p.trim).trim

  def killpre1(s: String, p: String): String =
    if (s.startsWith(p)) s.drop(p.length) else s

  def refs(p: Node): NodeSeq =
    (p \ "span").filter(s => (s \\ "@class").text == "footnotereference")

  def killdollars(s: String): String =
    killpage(s.replace("<$$$>", "").replace("&$$$;", ""))

  def killpage(s: String): String =
    CleanString.cleanNoTrimWhite(s.replaceAll("\\[[pP]age [nN]o[^\\]]+\\]", ""))

  def despacePunct(s: String): String =
    List(".", ",", ";", ":").foldLeft(s)(despace)

  def despace(s: String, p: String): String =
    s.replace(" " + p, p)

  def cleanup(s: String): String = {
    import CleanString._
    unaccent(cleanQuotes(cleanDashes(cleanNoTrimWhite(s))))
  }
}
