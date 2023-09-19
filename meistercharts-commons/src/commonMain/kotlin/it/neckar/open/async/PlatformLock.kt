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
