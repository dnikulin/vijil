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

package com.dnikulin.vijil.traits

/**
 * An arbitrary, optional marking for a text or text span.
 *
 * Runes may be added and interpreted arbitrarily, allowing
 * freestyle extension of the information associated with a text.
 *
 * The rune metaphor is used to suggest that runes are meaningful to a
 * specific sub-system and completely opaque to all others.
 *
 * Scala pattern matching is a convenient way to extract appropriate
 * runes and ignore all others.
 */
trait Rune

/**
 * Instances of HasRunes are required to contain a (potentially empty)
 * sequence of Rune instances.
 */
trait HasRunes {
  /**
   * Return all runes associated with this instance.
   */
  val runes: Seq[Rune]

  /**
   * Returns all runes that are instances of the given class.
   */
  def runesOfClass[T <: Rune](implicit ev: ClassManifest[T]): Seq[T] = runes.flatMap {
    // Use reflection API for type safe filter.
    case rune if ev.erasure.isAssignableFrom(rune.getClass) =>
      Some(rune.asInstanceOf[T])

    case _ =>
      None
  }
}
