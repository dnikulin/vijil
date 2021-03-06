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

import com.dnikulin.vijil.parse.StringSpan
import com.dnikulin.vijil.parse.PlainStringSpan
import com.dnikulin.vijil.tools.ArrSeq

object RenderString {
  def apply(data: String, spans: Seq[NodeSpan]): NodeSeq =
    apply(new PlainStringSpan(data), spans)

  def apply(root: StringSpan, spans: Seq[NodeSpan]): NodeSeq = {
    // Order spans by depth, start (ascending) and end (descending).
    val sorted = spans.
      toArray.
      filter(_.span.overlaps(root)).
      sortBy(s => (s.depth, s.span.min, -s.span.max))

    // Create list of all distinct depths, ordered so shallow is first.
    val depths = sorted.map(_.depth).toSet.toList.sorted

    // Separate spans by depth, keeping them in arrays.
    val tree = depths.map(depth => wrapZeroChars(sorted.filter(_.depth == depth)))

    // Start rendering at shallowest depth.
    return renderTree(root, tree)
  }

  private def renderTree(root: StringSpan, tree: List[Array[NodeSpan]]): NodeSeq = {
    import root.{data, min, max, cut}

    // Return clear text if no spans found.
    if (tree.isEmpty)
      return Text(data.substring(min, max))

    // Find applicable spans at this level.
    val queue  = tree.head.filter(_.span.overlaps(root))

    // Detach deeper levels, if any.
    val tail   = tree.tail

    // Defer to deeper levels if no spans found here.
    if (queue.length < 1)
      return renderTree(root, tail)

    val nodes  = ArrSeq.newBuilder[Node]

    var pos    = min
    var ispan  = 0

    // Prepare partial queue to exclude exactly one span.
    // This will first be the first span, so drop it first.
    val others = queue.drop(1)

    while ((pos < max) && (ispan < queue.length)) {
      // Take next wrapper span in the queue.
      val wrapper = queue(ispan)
      val span = wrapper.span

      // Restore leading part of queue into partial queue.
      if (ispan > 0)
        others(ispan - 1) = queue(ispan - 1)

      // Advance in queue.
      ispan += 1

      // Ensure the span belongs to the same string.
      assert(span.data eq data)

      // Pad intermediate gap with deeper levels.
      if (pos < span.min) {
        val span2 = cut(pos, span.min)
        nodes ++= renderTree(span2, tail)
        pos = span.min
      }

      // Render within this span.
      if ((pos < span.max) && (pos < max)) {
        // Cut sub-span into non-overlapping portion.
        val span2 = cut(pos, span.max)
        pos = span2.max

        // Recurse to render within the span,
        // which may then involve deeper levels.
        val inner = renderTree(span2, others :: tree.tail)

        // Apply style function to inner nodes.
        nodes ++= wrapper(inner)
      }

      // Render 0-character span.
      if ((span.min == span.max) && (pos == span.min))
        nodes ++= wrapper(NodeSeq.Empty)
    }

    // Pad final gap with deeper levels.
    if (pos < max) {
      val span2 = cut(pos, max)
      nodes ++= renderTree(span2, tail)
    }

    return nodes.result
  }

  private def wrapZeroChars(spans: Array[NodeSpan]): Array[NodeSpan] =
    spans.map(wrapZeroChar)

  private def wrapZeroChar(span: NodeSpan): NodeSpan = {
    if (span.len < 1) span.copy(wrapper=wrapOnce(span.wrapper))
    else span
  }

  // Hack wrapper to make a NodeSpan only effective once.
  private def wrapOnce(wrap: NodeSpan.Wrap): NodeSpan.Wrap = {
    var spent = false

    def wrap2(nodes: NodeSeq): NodeSeq = {
      if (spent == false) {
        spent = true
        wrap(nodes)
      } else {
        nodes
      }
    }

    (wrap2 _)
  }
}
