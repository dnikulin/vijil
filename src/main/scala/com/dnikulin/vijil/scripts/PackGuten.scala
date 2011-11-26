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

import scala.collection.mutable.ArrayBuilder
import scala.xml._

import java.io._
import java.util.regex.Pattern

import akka.actor._
import akka.actor.Actor._

import com.google.common.io.Files

import com.dnikulin.vijil.actor._
import com.dnikulin.vijil.file._
import com.dnikulin.vijil.lexer._
import com.dnikulin.vijil.parse._
import com.dnikulin.vijil.text._

object PackGuten {
  val rexTitle    = Pattern.compile("\nTitle:(.*?)\n\\s*?\n",
                                    Pattern.MULTILINE | Pattern.DOTALL)

  val rexAuthor   = Pattern.compile("Author:(.*)")

  val rexAuthors  = Pattern.compile("Authors:(.*)")

  val rexLanguage = Pattern.compile("Language:(.*)")

  val rexSpaces   = Pattern.compile("\\s+")

  val lexer = LuceneEnglishLexer

  def main(args: Array[String]): Unit = {
    val iroot = new File("data/plain/gutenberg/")
    require(iroot.exists)
    require(iroot.isDirectory)

    val oroot = new File("data/packs/gutenberg/")
    require(oroot.exists)
    require(oroot.isDirectory)

    val saver  = actorOf(new SavePackActor(oroot, false))
    val packer = actorOf(new CollectPackActor(4 * 1024 * 1024, saver))
    val parser = actorOf(new ParseActor(lexer, 100, packer))

    saver.start()
    packer.start()
    parser.start()

    println("Finding paths")

    val paths = findFiles(iroot)
    val npaths = paths.length

    println("Found %d paths".format(npaths))

    for ((path, index) <- paths.zipWithIndex) {
      if ((index % 1000) == 0) {
        println("%8d of %8d (%3d%%)".format(
          index, npaths, (index * 100) / npaths))
      }

      parseFile(path, parser)
    }

    parser ! Shutdown

    while (saver.isRunning == true) {
      println("Waiting for packer")
      Thread.sleep(1000)
    }
  }

  def parseFile(path: File, parser: ActorRef) {
    val bytes = Files.toByteArray(path)
    val hash = Hash.hash(bytes)

    val string = new String(bytes, Hash.utf8)
    val header = string.take(10000)

    var ntitles    = 0
    var nauthors   = 0
    var nlanguages = 0

    var title      = ""
    var authors    = List[String]()
    var languages  = List[String]()

    for (title2 <- matches(header, rexTitle)) {
      title = respace(title2)
      ntitles += 1
    }

    for (author <- matches(header, rexAuthor)) {
      authors ::= respace(author)
      nauthors = authors.length
    }

    for (author <- matches(header, rexAuthors)) {
      authors :::= author.trim.split(" and ").map(respace).toList
      nauthors = authors.length
    }

    for (language2 <- matches(header, rexLanguage)) {
      languages ::= respace(language2)
      nlanguages += 1
    }

    if (ntitles != 1) {
      println("Text [%s] has %d titles".format(hash, ntitles))
      return
    }

    languages match {
      case "English" :: Nil =>

      case _ =>
        println("Text [%s] has languages '%s'".format(hash, languages.mkString(", ")))
        return
    }

    val authorTags   =   authors.toArray.map(Tag("Author",   _))
    val languageTags = languages.toArray.map(Tag("Language", _))

    val tags = IndexedSeq(
      Tag("Collection", "Project Gutenberg"),
      Tag("Title",      title),
      Tag("Bytes",      bytes.length.toString),
      Tag("Characters", string.length.toString)
    ) ++ authorTags ++ languageTags

    val text = new TextFile(string, hash, tags)
    parser ! RawTexts(List(text))
  }

  def findFiles(root: File): Array[File] = {
    val files = Array.newBuilder[File]
    findFiles(root, files)
    files.result
  }

  def findFiles(root: File, files: ArrayBuilder[File]): Unit = {
    for (child <- root.listFiles) {
      if (child.getName.startsWith(".") == false) {
        if (child.isDirectory)
          findFiles(child, files)
        else
          files += child
      }
    }
  }

  def matches(string: String, pattern: Pattern): Array[String] = {
    val out = new ArrayBuilder.ofRef[String]
    val matcher = pattern.matcher(string)
    while (matcher.find)
      out += matcher.group(1)
    out.result
  }

  def respace(string: String): String =
    rexSpaces.matcher(string.trim).replaceAll(" ").trim
}
