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

/**
 * Represents a platform lock - a no-op on the single-threaded web targets (JS + Wasm)
 */
actual class PlatformLock {
  actual fun lock() {
    //noop - not required for JS
  }

  actual fun unlock() {
    //noop - not required for JS
  }

  companion object {
    val Instance: PlatformLock = PlatformLock()
  }
}

actual class PlatformReadWriteLock {
  actual fun readLock(): PlatformReadLock {
    return PlatformReadLock.Instance
  }

  actual fun writeLock(): PlatformWriteLock {
    return PlatformWriteLock.Instance
  }

  companion object {
    val Instance: PlatformReadWriteLock = PlatformReadWriteLock()
  }
}

actual class PlatformReadLock {
  actual fun lock() {
    //NOOP
  }

  actual fun unlock() {
    //NOOP
  }

  companion object {
    val Instance: PlatformReadLock = PlatformReadLock()
  }
}

actual class PlatformWriteLock {
  actual fun lock() {
    //NOOP
  }

  actual fun unlock() {
    //NOOP
  }

  companion object {
    val Instance: PlatformWriteLock = PlatformWriteLock()
  }

}
