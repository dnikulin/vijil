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

package com.dnikulin.vijil.tools

import scala.xml._

import javax.xml._
import javax.xml.parsers._

import java.io.ByteArrayInputStream

import net.liftweb.util.Helpers.tryo

object XmlTools {
  def attribute(node: Node, key: String): Option[String] =
    node.attribute(key).map(_.text.trim)

  def attributes(node: Node, keys: Array[String]): Array[String] =
    keys.flatMap(node.attribute(_)).map(_.text.trim)

  def attributes(node: Node, keys: String*): Array[String] =
    attributes(node, keys.toArray)

  def childStrings(node: Node, name: String): Array[String] =
    (node \ name).toArray.map(_.text)

  def readNode(bytes: Array[Byte]): Option[Node] = tryo {
    // Create paranoid XML parser factory.
    val prefix = "http://xml.org/sax/features/"
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(false)
    factory.setValidating(false)
    factory.setXIncludeAware(false)
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    factory.setFeature(prefix + "external-general-entities", false)
    factory.setFeature(prefix + "external-parameter-entities", false)
    factory.setFeature(prefix + "validation", false)

    // Create input stream.
    val stream = new ByteArrayInputStream(bytes)

    // Create and deploy paranoid XML parser.
    val parser = factory.newSAXParser()
    XML.withSAXParser(parser).load(stream)
  }
}
