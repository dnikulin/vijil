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

package com.dnikulin.vijil.index

import com.dnikulin.vijil.lexer.TextLexer
import com.dnikulin.vijil.model.TextModel
import com.dnikulin.vijil.result._
import com.dnikulin.vijil.text.TextFile

protected class LexedGroups(val models: Array[TextModel], val groups: Array[Array[LinkSpanSet]])

object SearchSplit {
  type LinkSpanSets = (Array[LinkSpanSet])
  type Search       = (Array[TextModel] => Array[Array[LinkSpanSet]])

  def apply(texts: Seq[TextFile], lexers: Seq[TextLexer], search: Search): Array[Array[LinkSpanSet]] = {
    if (texts.isEmpty || lexers.isEmpty)
      return Array.empty

    // Create parallel arrays for texts and lexers.
    val plexers = lexers.toArray.par
    val ptexts  = texts.toArray.par

    // In parallel for all lexers, search into groups of sets.
    val lgroups = {
      for (lexer <- plexers) yield {
        // Parse all texts in parallel.
        val models = ptexts.map(lexer).toArray

        // Perform search.
        val groups = search(models)

        // Combine into tuple.
        new LexedGroups(models, groups)
      }
    }.seq.toArray

    // Determine number of groups.
    val ngroups = lgroups.head.groups.length
    val indices = (0 until ngroups).toArray.par

    // In parallel for all groups, normalise into shared graph.
    val graphs  = for (index <- indices) yield {
      val graph = new LinkSpanGraph(SpanDomain.CHARACTERS)
      for (lgroup <- lgroups)
        graph.normalise(lgroup.groups(index), lgroup.models)
      graph.result
    }

    return graphs.toArray
  }

  def exact(texts: Seq[TextFile], lexers: Seq[TextLexer], size: Int, extent: Int): (Array[LinkSpanSet], Array[LinkSpanSet]) =
    split2(apply(texts, lexers, models => expand(models, SearchSet(models, size), extent)))

  def stencil(texts: Seq[TextFile], lexers: Seq[TextLexer], stencil: StencilModel, extent: Int): (Array[LinkSpanSet], Array[LinkSpanSet]) =
    split2(apply(texts, lexers, models => expand(models, SearchSet(models, stencil), extent)))

  private def expand(models: Array[TextModel], sets: Array[LinkSpanSet], extent: Int): Array[Array[LinkSpanSet]] = {
    if (extent < 1) Array(sets)
    else Array(sets, ExpandSymbols(models, sets, extent))
  }

  private def split2(items: Array[Array[LinkSpanSet]]): (Array[LinkSpanSet], Array[LinkSpanSet]) = items match {
    case Array(i1, i2) => (i1, i2)
    case Array(i1    ) => (i1, i1)
    case _ => (LinkSpanSet.none, LinkSpanSet.none)
  }
}
