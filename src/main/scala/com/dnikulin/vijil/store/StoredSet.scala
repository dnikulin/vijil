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

package com.dnikulin.vijil.store

import java.util.concurrent.ConcurrentHashMap

import com.dnikulin.vijil.tools.Empty.{bytes => noBytes}

private sealed class ItemState
private case object IsPresent extends ItemState
private case object IsAbsent extends ItemState

class StoredSet(private val store: ObjectStore[Array[Byte]]) {
  private val cache = new ConcurrentHashMap[String, ItemState]

  def add(key: String): Boolean = {
    // Mark the key as present.
    if (cache.put(key, IsPresent) ne IsPresent) {
      // Store it and return true if it was not previously present.
      store.put(key, noBytes)
      true
    } else {
      false
    }
  }

  def has(key: String): Boolean = {
    // Check cache for state of this key.
    // If the state is known, return it.
    val known = cache.get(key)
    if (known ne null)
      return (known eq IsPresent)

    // Check store for state of this key.
    // If stored, update state to present.
    val stored = store.get(key)
    if (stored.isDefined) {
      // Key is stored, so mark it as present.
      cache.put(key, IsPresent)
      return true
    } else {
      // Key is not stored, so mark it as absent.
      cache.put(key, IsAbsent)
      return false
    }
  }
}
