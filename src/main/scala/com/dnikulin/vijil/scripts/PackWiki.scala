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

import scala.xml._

import java.io._

import akka.actor._
import akka.actor.Actor._

import org.apache.commons.compress.compressors.bzip2._

import com.dnikulin.vijil.actor._
import com.dnikulin.vijil.file._
import com.dnikulin.vijil.lexer._
import com.dnikulin.vijil.parse._
import com.dnikulin.vijil.text._

object PackWiki {
  val baseURL = "http://en.wikipedia.org/wiki/"

  val lexer = LuceneEnglishLexer

  def main(args: Array[String]): Unit = {
    val path = new File("data/plain/enwiki-latest-pages-articles.xml.bz2")
    require(path.exists)
    require(path.isFile)

    val root = new File("data/packs/wikipedia/")
    require(root.exists)
    require(root.isDirectory)

    val saver  = actorOf(new SavePackActor(root, false))
    val packer = actorOf(new CollectPackActor(4 * 1024 * 1024, saver))
    val parser = actorOf(new ParseActor(lexer, 100, packer))

    saver.start()
    packer.start()
    parser.start()

    val wparser = new WikiParser {
      override def haveText(title: String, string: String): Unit = {
        val bytes = string.getBytes(Hash.utf8)
        val hash = Hash.hash(bytes)

        val url = (baseURL + title.trim)

        val tags = List(
          Tag("Collection", "English Wikipedia"),
          Tag("Title",      title.trim),
          Tag("URL",        url),
          Tag("Bytes",      bytes.length.toString),
          Tag("Characters", string.length.toString)
        )

        val text = new TextFile(string, hash, tags)
        parser ! RawTexts(List(text))
      }
    }

    val file = new FileInputStream(path)
    val buf1 = new BufferedInputStream(file)
    val bzip = new BZip2CompressorInputStream(buf1)
    val buf2 = new BufferedInputStream(bzip)

    val sax = XML.parser
    sax.parse(buf2, wparser)
    buf2.close()

    parser ! Shutdown

    while (saver.isRunning == true) {
      println("Waiting for packer")
      Thread.sleep(1000)
    }
  }
}
