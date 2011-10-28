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

import scala.xml.NodeSeq

import com.dnikulin.vijil.parse.StringSpan

object NodeSpan {
  type Wrap = (NodeSeq => NodeSeq)

  def empty(nodes: NodeSeq): NodeSeq =
    Nil

  def identity(nodes: NodeSeq): NodeSeq =
    nodes

  def foldLeft(wraps: Seq[Wrap])(nodes: NodeSeq): NodeSeq =
    wraps.foldLeft(nodes)((n, f) => f(n))

  def foldRight(wraps: Seq[Wrap])(nodes: NodeSeq): NodeSeq =
    wraps.foldRight(nodes)((f, n) => f(n))

  def bold(nodes: NodeSeq): NodeSeq =
    <b>{nodes}</b>

  def italic(nodes: NodeSeq): NodeSeq =
    <i>{nodes}</i>

  def spanStyle(style: String)(nodes: NodeSeq): NodeSeq =
    <span style={style}>{nodes}</span>

  def spanClass(klass: String)(nodes: NodeSeq): NodeSeq =
    <span class={klass}>{nodes}</span>

  def spanID(id: String)(nodes: NodeSeq): NodeSeq =
    <span id={id}>{nodes}</span>

  def anchor(name: String)(nodes: NodeSeq): NodeSeq =
    <a name={name}>{nodes}</a>

  def apply(span: StringSpan, wrapper: NodeSpan.Wrap, depth: Int): NodeSpan =
    new NodeSpan(span, wrapper, depth)

  def apply(span: StringSpan, wrapper: NodeSpan.Wrap): NodeSpan =
    apply(span, wrapper, 0)
}

case class NodeSpan(
  override val data:    String,
  override val min:     Int,
  override val max:     Int,
           val span:    StringSpan,
           val wrapper: NodeSpan.Wrap,
           val depth:   Int
) extends StringSpan {

  def this(span: StringSpan, wrapper: NodeSpan.Wrap, depth: Int) {
    this(span.data, span.min, span.max, span, wrapper, depth)
  }

  def this(span: StringSpan, wrapper: NodeSpan.Wrap) {
    this(span, wrapper, 0)
  }

  override def cut(min1: Int, max1: Int): NodeSpan =
    new NodeSpan(span.cut(min1, max1), wrapper, depth)

  def apply(nodes: NodeSeq): NodeSeq =
    wrapper(nodes)
}
