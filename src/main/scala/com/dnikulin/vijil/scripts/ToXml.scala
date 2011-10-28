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
import scala.xml.Node
import scala.xml.XML

import java.io.File

import com.google.common.io.Files

object ToXml {
  def main(args: Array[String]): Unit = {
    // Use println for output.
    import scala.Predef.{println => out}

    // Read entire XML document with fixes.
    val file = fixXML(readFile(args.head))

    // Interpret as XML DOM.
    val document = XML.loadString(file)

    out("""<?xml version="1.0" encoding="UTF-8"?>""")
    out()

    out("""<text id="Speculum Morale">""")
    out()

    // Find all footnote divs.
    val notes = findFootnotes(document)

    def writeout(node: Node) {
      for (child <- node.child.toList) {
        if (child.label == "#PCDATA") {
          if (child.text.trim.matches("\\[[0-9]*\\]")) {
            val num = child.text.trim.drop(1).dropRight(1).toInt
            val note = notes(num)
            print("""<note number="%d">%s</note>""".format(num, note))
          } else {
            print(cleanup(child.text))
          }
        } else {
          if (attr(child, "class") == "Page") {
            val num = child.text.trim.drop(1).dropRight(1).toInt
            print("""<page number="%d" />""".format(num))
          } else {
            writeout(child)
          }
        }
      }
    }

    var insec1 = false
    var insec2 = false

    for (p <- (document \ "body" \ "div" \ "p")) {
      attr(p, "class") match {
        case "LiberTitle" =>
          val inner = cleanup(p.text).trim

          if (insec2) {
            out("    </section>")
            out()
            insec2 = false
          }

          if (insec1) {
            out("  </section>")
            out()
            insec1 = false
          }

          out("  <section id=\"%s\">".format(inner))
          out()
          insec1 = true

        case "DistinctioTitle" =>
          val titleText  = cleanup((p \ "i" \ "span").text).trim
          val numberText = cleanup((p \ "span").text).trim

          if (insec2) {
            out("    </section>")
            out()
            insec2 = false
          }

          val inner = List(numberText, titleText).mkString(" ").trim
          out("    <section id=\"%s\">".format(inner))
          out()
          insec2 = true

        case "Text" =>
          writeout(p)
          out()
          out()

        case _ =>
      }
    }

    out("    </section>")
    out()
    out("  </section>")
    out()
    out("</text>")
    out()
  }

  def findFootnotes(document: Node): Map[Int, String] = {
    val map = new HashMap[Int, String]

    val notes = (document \ "body" \ "div" \ "div").toList

    for (div <- notes) {
      val id = attr(div, "id").drop(3).toInt
      map(id) = cleanup(div.text).trim
    }

    return map.toMap
  }

  def readFile(path: String): String =
    new String(Files.toByteArray(new File(path)), "utf8")

  def fixXML(file: String): String = {
    file.
      replaceAll("<[bh]r[^>]*>", "").
      replaceAll("='[^']+'", "=\"\"").
      replaceAll("([^\\s=]+)=([^\"][^\\s>]*?)([\\s>])", "$1=\"$2\"$3").
      replace("&nbsp;", " ")
  }

  def cleanup(file: String): String =
    reLigs(reSpace(killNotes(file)))

  def reSpace(file: String): String =
    file.replaceAll("\\s+", " ")

  def killNotes(file: String): String =
    file.replaceAll("\\[[0-9]*\\]", "")

  def reLigs(file: String): String = {
    file.
      replace("à", "a").
      replace("Æ", "AE").
      replace("æ", "ae").
      replace("œ", "oe").
      replaceAll("[èéêë]", "e").
      replaceAll("[ìî]", "i").
      replaceAll("[òóô]", "o").
      replaceAll("[ùûü]", "u")
  }

  def attr(node: Node, key: String): String =
    node.attribute(key).toList.flatMap(_.flatMap(_.text.trim)).mkString
}
