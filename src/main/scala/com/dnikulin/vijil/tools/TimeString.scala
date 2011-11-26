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

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

import com.dnikulin.vijil.text.HasTags
import com.dnikulin.vijil.text.Tag

object TimeString {
  val utc        = TimeZone.getTimeZone("UTC")
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val timeFormat = new SimpleDateFormat("HH:mm:ss")

  dateFormat.setTimeZone(utc)
  timeFormat.setTimeZone(utc)

  def makeDateTime(moment: Date): (String, String) = {
    val date = dateFormat.format(moment)
    val time = timeFormat.format(moment)
    return (date, time)
  }

  def makeTags(moment: Date): IndexedSeq[Tag] = {
    val (date, time) = makeDateTime(moment)
    ArrSeq(Tag("Date", date), Tag("Time", time))
  }

  def makeDateTime(): (String, String) =
    return makeDateTime(new Date)

  def makeTags(): IndexedSeq[Tag] =
    makeTags(new Date)

  def format(tags: HasTags[_]): String =
    (tags.tag("Date").take(1) ++ tags.tag("Time").take(1)).mkString(" ").trim
}
