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

import com.dnikulin.vijil.text._
import com.dnikulin.vijil.tools.ArrSeq

object RenderNotes {
  def apply(root: TextSpan, notes: Seq[TextNote]): IndexedSeq[NodeSpan] = {
    for (note <- ArrSeq.convert(notes); if root.includes(note.at)) yield {
      // Empty spans are not rendered, so try to make a 1-letter span.
      val cmin = note.at
      val cmax = (cmin + 1) min (root.max - 1)
      val span = root.cut(cmin, cmax)

      def wrap(nodes: NodeSeq): NodeSeq = {
        // Insert note before the 1-letter span.
        <a class="vijil-note" href="#" title={note.body}> ({note.label}) </a> ++ nodes
      }

      // Give a very high depth so that this is not repeated.
      new NodeSpan(span, wrap, 1000)
    }
  }
}
