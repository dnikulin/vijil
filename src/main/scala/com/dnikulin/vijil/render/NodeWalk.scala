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

object NodeWalk {
  def find(tree: Node, where: (Node => Boolean)): List[Node] = {
    // Prepare output list builder.
    val found = List.newBuilder[Node]

    def walk(node: Node) {
      // Check this node.
      if (where(node) == true)
        found += node

      // Check all child nodes.
      node.child.foreach(walk)
    }

    // Start walking from the root.
    walk(tree)

    // Return collected nodes.
    return found.result
  }

  def map(node: Node, each: (Node => NodeSeq)): NodeSeq = node match {
    case Elem(prefix, label, attributes, scope, child @ _*) =>
      // Map all children first.
      val child2 = child.flatMap(map(_, each))

      // Re-construct node with new children.
      val elem2 = new Elem(prefix, label, attributes, scope, child2:_*)

      // Map new node.
      each(elem2)

    case _ =>
      each(node)
  }
}
