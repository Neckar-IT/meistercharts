package it.neckar.open.async

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock

/**
 * Represents a platform lock - depending on the platform, this can be a no-op or a real lock
 */
actual typealias PlatformLock = ReentrantLock
actual typealias PlatformReadLock = ReadLock
actual typealias PlatformWriteLock = WriteLock

actual typealias PlatformReadWriteLock = ReentrantReadWriteLock
