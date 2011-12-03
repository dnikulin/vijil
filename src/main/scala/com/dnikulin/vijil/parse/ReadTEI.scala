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

import scala.xml._

import java.util.regex.Pattern

import net.liftweb.util.Helpers.asInt

import com.dnikulin.vijil.render.NodeSpan
import com.dnikulin.vijil.text._
import com.dnikulin.vijil.traits.Rune
import com.dnikulin.vijil.tools.ArrSeq
import com.dnikulin.vijil.tools.CleanString

object ReadTEI {
  import CleanString._

  def readTeiNode(tei: Node, hash: String): Option[TextFile] = tei match {
    case Elem(null, teiName, _, _, _*) if teiName.startsWith("TEI") =>
      for (teiHeader <- (tei \ "teiHeader").headOption;
           // Skip through fileDesc layer, if any.
           titleStmt <- (teiHeader \\ "titleStmt" \ "title").headOption;
           title     <- (titleStmt \\ "title").headOption;
           text      <- (tei \ "text").headOption) yield {

        // Discover authors, if any.
        val authors = ArrSeq.convert((titleStmt \\ "author").map(cleanText))

        // Create tag list from title and authors.
        val tags = ArrSeq(Tag("Title", cleanText(title))) ++ authors.map(Tag("Author", _))

        // Read text content.
        readTextContent(text, tags, hash)
      }

    case _ =>
      None
  }

  def readTextContent(text: Node, tags: IndexedSeq[Tag], hash: String): TextFile = {
    // Refer to front, body and back elements.
    val frontBodyBack = ((text \ "front") ++ (text \ "body") ++ (text \ "back"))

    // Pre-extract all text data.
    val data = mergeText(frontBodyBack)

    // Text span cursor.
    var cursor = 0

    // Buffer for marks.
    val marks = ArrSeq.newBuilder[NodeSpan]

    // Buffer for runes.
    val runes = ArrSeq.newBuilder[Rune]

    // Prepare extraction inner function.
    def extract(node: Node): IndexedSeq[TextSpan] = node match {
      case Text(string) =>
        // Extract and clean string data.
        val clean = cleanPadString(string)

        // Calculate new text extent.
        val cmin  = cursor
        cursor   += clean.length
        val cmax  = cursor

        // Verify against pre-extracted data.
        assert(data.substring(cmin, cmax) == clean)

        // Return no span for plain text.
        ArrSeq.emptySeq

      case Elem(_, _, _, _, child@_*) =>
        // Explore child elements.
        val cmin  = cursor
        val parts = ArrSeq.convert(ArrSeq.convert(child).flatMap(extract))
        val cmax  = cursor

        // Create tags, keep only non-empty tags.
        val tags = nodeTags(node).filter(_.value.length > 0)

        // Create text span with these tags, trim it.
        val span = new TextSpan(data, hash, cmin, cmax, tags, parts)

        // Create trimmed version of this span.
        val trim = span.trim

        // Create any node spans.
        marks ++= nodeSpans(node).map(NodeSpan(trim, _, 1000))

        // Create any text runes.
        runes ++= nodeRunes(node, cursor)

        if (nodeMakeSpan(node)) {
          // Return new wrapping span.
          ArrSeq(span)
        } else {
          // Return child spans.
          parts
        }

      case _ =>
        ArrSeq.emptySeq
    }

    // Invoke extraction function on all text contents.
    val parts = ArrSeq.convert(ArrSeq.convert(frontBodyBack).flatMap(extract))

    // Check that all text content has been consumed.
    assert(cursor == data.length)

    // Create extra tag to represent title for root span.
    val tags2 = ArrSeq.convert(tags.filter(_.name == "Title").map(_.copy(name = "BlockName")))

    // Create root span to contain all text spans.
    val span = new TextSpan(data, hash, 0, cursor, tags2 ++ tags, parts)

    // Create TextFile.
    new TextFile(data, hash, tags, ArrSeq(span), ArrSeq.emptySeq, marks.result, runes.result)
  }

  def cleanText(nodes: NodeSeq): String =
    cleanString(nodes.text)

  def cleanString(string: String): String =
    weirdWhitePattern.matcher(string).replaceAll(" ")

  def cleanPadString(string: String): String =
    (" " + string + " ")

  def mergeText(nodes: NodeSeq): String =
    findText(nodes).mkString

  def findText(nodes: NodeSeq): Seq[String] =
    nodes.flatMap(findText)

  def findText(node: Node): Seq[String] = node match {
    case Text(string) => IndexedSeq(cleanPadString(string))
    case _            => findText(node.child)
  }

  def findName(node: Node, key: String): String =
    scoreToSpace(cleanString((node \ ("@" + key)).text)).trim

  private val underscorePattern = Pattern.compile("[\\s_]+")
  def scoreToSpace(string: String): String =
    underscorePattern.matcher(string).replaceAll(" ")

  def nodeTags(node: Node): IndexedSeq[Tag] = node match {
    case Elem(null, "div", _, _, _*) =>
      val divType = findName(node, "type")
      val divID   = findName(node, "id")
      ArrSeq(Tag("BlockLevel", divType), Tag("BlockName", divID))

    case _ =>
      ArrSeq.emptySeq
  }

  def nodeSpans(node: Node): IndexedSeq[NodeSpan.Wrap] = node match {
    case Elem(null, "head", _, _, _*) =>
      ArrSeq(TeiSpan.head)

    case Elem(null, "q", _, _, _*) =>
      ArrSeq(TeiSpan.quote)

    case Elem(null, "hi", _, _, _*) =>
      ArrSeq(TeiSpan.quote)

    case Elem(null, "list", _, _, _*) =>
      ArrSeq(TeiSpan.list)

    case Elem(null, "item", _, _, _*) =>
      ArrSeq(TeiSpan.listItem)

    case Elem(null, "note", _, _, _*) =>
      ArrSeq(TeiSpan.note)

    case Elem(null, "pb", _, _, _*) =>
      for (number <- numbers(node))
        yield TeiSpan.page(number)(_)

    case _ =>
      ArrSeq.emptySeq
  }

  def nodeRunes(node: Node, cursor: Int): IndexedSeq[Rune] = node match {
    // Interpret <pb n="1" /> element.
    case Elem(null, "pb", _, _, _*) =>
      // Convert to PageBreak rune.
      for (number <- numbers(node))
        yield PageBreak(number, cursor)

    case _ =>
     ArrSeq.emptySeq
  }

  def nodeMakeSpan(node: Node): Boolean = node match {
    case Elem(null, "front", _, _, _*) => true
    case Elem(null, "body",  _, _, _*) => true
    case Elem(null, "back",  _, _, _*) => true
    case Elem(null, "div",   _, _, _*) => true
    case Elem(null, "p",     _, _, _*) => true
    case Elem(null, "list",  _, _, _*) => true
    case Elem(null, "item",  _, _, _*) => true
    case _                             => false
  }

  def numbers(node: Node): IndexedSeq[Int] =
    ArrSeq.convert(asInt((node \ "@n").text.trim).toSeq)
}

object TeiSpan {
  def quote(nodes: NodeSeq): NodeSeq =
    <span class="tei_quote">{nodes}</span>

  def head(nodes: NodeSeq): NodeSeq =
    <div class="tei_head">{nodes}</div>

  def list(nodes: NodeSeq): NodeSeq =
    <ul class="tei_list">{nodes}</ul>

  def listItem(nodes: NodeSeq): NodeSeq =
    <li class="tei_list_item">{nodes}</li>

  def note(nodes: NodeSeq): NodeSeq = {
    <span class="tei_note">{nodes}</span>
  }

  def page(number: Int)(nodes: NodeSeq): NodeSeq = {
    <div class="tei_page">Page {number}</div>
  }
}
