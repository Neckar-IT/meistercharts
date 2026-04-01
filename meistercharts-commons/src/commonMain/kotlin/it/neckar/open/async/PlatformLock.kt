/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.async

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Represents a platform lock - depending on the platform, this can be a no-op or a real lock
 */
expect class PlatformLock {
  fun lock()
  fun unlock()
}

expect class PlatformReadLock {
  fun lock()
  fun unlock()
}

expect class PlatformWriteLock {
  fun lock()
  fun unlock()
}

expect class PlatformReadWriteLock() {
  fun readLock(): PlatformReadLock
  fun writeLock(): PlatformWriteLock
}

inline fun <T> PlatformLock.withLock(action: () -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  lock()
  try {
    return action()
  } finally {
    unlock()
  }
}

inline fun <T> PlatformReadWriteLock.read(action: () -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  val readLock = readLock()
  readLock.lock()
  try {
    return action()
  } finally {
    readLock.unlock()
  }
}

inline fun <T> PlatformReadWriteLock.write(action: () -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }

  val wl = writeLock()
  wl.lock()
  try {
    return action()
  } finally {
    wl.unlock()
  }
}
