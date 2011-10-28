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

package com.dnikulin.vijil.render

import scala.xml._

import com.dnikulin.vijil.parse.PlainStringSpan

case class AntiRender(data: String, nodes: List[NodeSpan])

object AntiRender {
  def apply(tree: Node): AntiRender = {
    // Extract all string data without changes.
    val data = tree.text

    // List of extracted nodes.
    val nodes = List.newBuilder[NodeSpan]

    // Current position in 'data'.
    var cursor = 0

    def extract(node: Node): Unit = node match {
      // Elements are re-created with appropriate NodeSpans.
      case Elem(prefix, label, attributes, scope, child @ _*) =>
        // Extract all children, noting position before and after.
        val cmin = cursor
        child.foreach(extract)
        val cmax = cursor
        assert(cmax >= cmin)

        // Create string span.
        val span = new PlainStringSpan(data, cmin, cmax)

        // Create wrapper function to restore the element with new children.
        def wrap(child2: NodeSeq): NodeSeq =
          new Elem(prefix, label, attributes, scope, child2:_*)

        // Create and record node span.
        nodes += new NodeSpan(span, wrap, -100000)

      // Text is already accumulated in 'data', but spans must be known.
      case Text(body) =>
        // Advance cursor.
        val cmin = cursor
        cursor  += body.length
        val cmax = cursor

        // Verify consistency.
        assert(cmax >= cmin)
        assert(body == data.substring(cmin, cmax))

      case _ =>
        // Do nothing.
    }

    // Start extraction from children of root node.
    tree.child.foreach(extract)

    // Return results.
    new AntiRender(data, nodes.result)
  }
}