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

import scala.collection.JavaConversions._
import scala.io.Source

import java.io._
import java.util.HashMap
import java.util.zip.ZipFile
import java.util.regex.Pattern

final class WordCount(val word: String) {
  var total = 0L
  var texts = 0L
}

object ReadGoogleNGram {
  val wordPattern = Pattern.compile("[a-z][a-z\\-']+")

  def main(args: Array[String]): Unit = {
    val paths      = args.map(new File(_)).filter(_.isFile)
    val totalPath  = paths.head
    val dataPaths  = paths.tail

    println("Path  to totals : " + totalPath.getAbsolutePath)
    println("Paths to files  :")
    for (path <- dataPaths)
      println("  " + path.getAbsolutePath)

    // Read total words and total texts.
    val (totalWords, totalTexts) = readTotals(totalPath)

    val counts = new HashMap[String, WordCount]

    for (path <- dataPaths) {
      println("Reading " + path.getAbsolutePath)

      val zip = new ZipFile(path, ZipFile.OPEN_READ)

      for (entry <- zip.entries) {
        val stream = zip.getInputStream(entry)
        readData(counts, stream)
        stream.close()
      }

      zip.close()
    }

    val out = new PrintWriter(new BufferedWriter(new FileWriter("google.txt")))
    out.println("%d words in %d texts".format(totalWords, totalTexts))
    for (word <- counts.values.toSeq.toArray.sortBy(_.word))
      out.println(word.word + " " + word.total + " " + word.texts)
    out.flush()
    out.close()
  }

  def readTotals(path: File): (Long, Long) = {
    var totalWords = 0L
    var totalTexts = 0L

    val source     = Source.fromFile(path, "utf-8")

    for (line <- source.getLines) {
      line.trim.split("\\s+") match {
        // Require number of words and number of texts.
        // Ignore year and number of pages.
        case Array(_, swords, _, stexts) =>
          totalWords += swords.toLong
          totalTexts += stexts.toLong
          assert(totalWords > 0L)
          assert(totalTexts > 0L)

        case _ =>
      }
    }

    source.close()

    return (totalWords, totalTexts)
  }

  def readData(counts: HashMap[String, WordCount], stream: InputStream) {
    val source = Source.fromInputStream(stream, "utf-8")

    var word = ""
    var count : WordCount = null

    for (line <- source.getLines) {
      line.trim.split("\\s+") match {
        // Require word, total and number of texts.
        // Ignore year and number of pages.
        case Array(sword, _, stotal, spages, stexts)
        if wordPattern.matcher(sword).matches =>

          val nword = sword.toLowerCase
          val total = stotal.toLong
          val texts = stexts.toLong

          assert(total > 0L)
          assert(texts > 0L)

          // New word, make or find count.
          if (word != nword) {
            word = nword
            // Find existing word count, if any.
            count = counts.get(word)

            if (count eq null) {
              // Create and insert zeroed word count.
              count = new WordCount(word)
              counts.put(word, count)
            }
          }

          // Update word count.
          count.total += total
          count.texts += texts
          assert(count.total > 0L)
          assert(count.texts > 0L)

        case _ =>
      }
    }

    source.close()
  }
}
