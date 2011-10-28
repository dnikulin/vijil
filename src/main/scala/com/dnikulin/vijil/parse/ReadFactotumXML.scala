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

import com.dnikulin.vijil.file.Hash
import com.dnikulin.vijil.text._

object ReadFactotumXML {
  def apply(root: Node): Option[TextFile] = {
    val data   = readString(root)
    var cursor = 0
    val notesb = List.newBuilder[TextNote]

    // Prepare text hash for spans.
    val hash   = Hash.hash(data)

    def consume(path: String, node: Node): Option[TextSpan] = {
      node match {
        case Text(body) =>
          val part = body.trim
          if (part.length > 0) {
            val cmin = cursor
            cursor += part.length + 1
            val cmax = cursor

            assert(data.substring(cmin, cmax - 1) == part)
          }
          None

        case Elem(_, e, _, _, _*)
        if ((e == "block") || (e == "section")) =>

          val level  = (node \ "@level").text.trim
          val name   = (node \ "@id").text.trim
          val id     = (level + " " + name).trim
          val path2  = (path + ", " + id)

          val tags = List(
            Tag("BlockLevel", level),
            Tag("BlockName",  name),
            Tag("BlockPath",  path2)
          ).filter(_.value.trim.length > 0)

          val tcmin = cursor
          val spans = node.child.toList.flatMap(consume(path2, _))
          val tcmax = cursor

          spans match {
            case List(span) =>
              Some(span)

            case Nil =>
              val span = new TextSpan(data, hash, tcmin, tcmax, tags, Nil)
              Some(paragraphs(span, path2))

            case _ =>
              val cmin = spans.map(_.min).min
              val cmax = spans.map(_.max).max
              val span = new TextSpan(data, hash, cmin, cmax, tags, spans)
              Some(span)
          }

        case Elem(_, "page", _, _, _*) =>
          for (number <- node.attribute("number")) {
            val label = "Page " + number
            notesb += TextNote(cursor, label, label)
          }
          None

        case Elem(_, "note", _, _, _*) =>
          for (number <- node.attribute("number")) {
            val label = "Note " + number
            notesb += TextNote(cursor, label, node.text.trim)
          }
          None

        case _ =>
          None
      }
    }

    val title = (root \ "@id").text.trim
    val tags2 = List(Tag("Title", title))
    val tags3 = List(Tag("BlockName", title), Tag("BlockPath", title))

    val spans = root.child.toList.flatMap(consume(title, _))
    val tspan = List(TextSpan(data, hash, 0, data.length, tags3, spans))

    val notes = notesb.result

    Some(new TextFile(data, hash, tags2, tspan))
  }

  private def readString(root: Node): String = {
    val out = new StringBuilder

    def consume(node: Node) {
      node match {
        case Text(body) =>
          val part = body.trim
          if (part.length > 0)
            out.append(part + " ")

        case Elem(_, e, _, _, _*)
        if ((e == "block") || (e == "section") || (e == "text")) =>
          node.child.foreach(consume)

        case _ =>
      }
    }

    consume(root)

    return out.toString
  }

  def paragraphs(root: TextSpan, path: String): TextSpan = {
    val level  = "paragraph"

    val paras = FindSpans.paragraphs(root)

    if (paras.length > 1) {
      val cmin    = paras.map(_.min).min
      val cmax    = paras.map(_.max).max
      var index   = 0

      val spans   = for (para <- paras) yield {
        index    += 1
        val name  = index.toString
        val id    = (level + " " + name).trim
        val path2 = (path + ", " + id)

        val tags = List(
          Tag("BlockLevel", level),
          Tag("BlockName",  name),
          Tag("BlockPath",  path2)
        ).filter(_.value.trim.length > 0)

        new TextSpan(root.data, root.hash, para.min, para.max, tags, Nil)
      }

      TextSpan(root.data, root.hash, cmin, cmax, root.tags, spans)
    } else {
      root
    }
  }
}
