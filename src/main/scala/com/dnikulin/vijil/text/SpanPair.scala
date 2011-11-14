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

package com.dnikulin.vijil.text

import scala.collection.mutable.HashMap

import net.liftweb.json._

import com.dnikulin.vijil.file.Hash
import com.dnikulin.vijil.result._
import com.dnikulin.vijil.tools._
import com.dnikulin.vijil.traits._

case class SpanPair(
  val sample: TextSpan,
  val source: TextSpan
) extends HasIdentity with HasSpans[TextSpan] with ToJson {

  override val identity =
    Hash.hash("%s_!_%s".format(sample.identity, source.identity))

  override val spans = List(sample, source)

  def texts = spans.flatMap(_.findText)

  override def toJValue: JValue = {
    JArray(List(sample.toJValue, source.toJValue))
  }
}

object SpanPair extends FromJson[SpanPair] {
  def apply(texts: List[TextFile], exact: List[LinkSpanSet], expand: List[LinkSpanSet]): List[SpanPair] = {
    assert((exact ::: expand).forall(_.domain == SpanDomain.CHARACTERS))

    val allExact = {
      for (mset  <- exact;
           mspan <- mset.spans.toList;
           span  <- TextSpan(texts, mspan))
        yield span
    }

    // Create TextSpans mapped by code.
    val linked = new HashMap[Int, TextSpan]
    for (set <- expand; mspan <- set.spans; tspan <- TextSpan(texts, mspan)) {
      import tspan._
      val exact  = allExact.filter(overlaps)
      val span2  = new TextSpan(data, hash, min, max, tags, exact.toList)
      linked(mspan.code) = span2
    }

    // Create pairs for all linked spans.
    val pairs = List.newBuilder[SpanPair]
    for (set    <- expand;
         mspan1 <- set.spans;    tspan1 <- linked.get(mspan1.code);
         mspan2 <- mspan1.links; tspan2 <- linked.get(mspan2.code)) {

      // Verify that the links are between distinct texts.
      assert (mspan1.hash != mspan2.hash)

      // Record the pair.
      pairs += SpanPair(tspan1, tspan2)
    }

    return pairs.result
  }

  override def fromJValue(jv: JValue): Option[SpanPair] = jv match {
    case JArray(List(jsample, jsource)) =>
      for (sample <- TextSpan.fromJValue(jsample);
           source <- TextSpan.fromJValue(jsource))
        yield new SpanPair(sample, source)

    case _ =>
      None
  }
}
