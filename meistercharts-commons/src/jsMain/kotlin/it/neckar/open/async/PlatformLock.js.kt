package it.neckar.open.async

/**
 * Represents a platform lock - depending on the platform, this can be a no-op or a real lock
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
