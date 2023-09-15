package it.neckar.open.collections

import it.neckar.open.annotations.TestOnly
import it.neckar.open.async.PlatformReadWriteLock
import it.neckar.open.async.read
import it.neckar.open.async.write
import it.neckar.open.unit.si.K
import kotlin.jvm.JvmOverloads


/**
 * Wraps a [CacheMap] and provides additional features like hit counter or other custom methods.
 *
 * Depending on the platform, the cache is synchronized by [lock].
 */
class Cache<K, V>
@Deprecated("use cache() method instead to allow use for better logging", level = DeprecationLevel.WARNING)
@JvmOverloads constructor(
  maxSize: Int = 16,
  /**
   * Is called when an element is removed from the cache
   */
  free: (K, V) -> Unit = { _, _ -> }
) {

  /**
   * The cache map that holds the values
   */
  val map: CacheMap<K, V> = CacheMap(maxSize, free)

  val lock = PlatformReadWriteLock()

  val maxSize: Int
    get() {
      lock.read {
        return map.maxSize
      }
    }

  fun updateMaxSize(newMaxSize: Int) {
    lock.write {
      map.updateMaxSize(newMaxSize)
    }
  }

  val size: Int
    get() {
      lock.read {
        return map.size
      }
    }

  /**
   * Does not work well in multi threading environments
   */
  @TestOnly
  val keys: Set<K>
    get() {
      lock.read {
        return map.keys
      }
    }

  /**
   * Returns all values
   */
  @TestOnly
  val values: Collection<V>
    get() {
      lock.read {
        return map.values
      }
    }

  /**
   * Returns the filtered values
   */
  fun filteredValues(predicate: (V) -> Boolean): List<V> {
    lock.read {
      return map.values.filter(predicate)
    }
  }

  /**
   * Counts the number of cache hits
   *
   * ATTENTION: This field is not synchronized - since it is only used as estimation.
   */
  var cacheHitCounter: Int = 0
    private set

  /**
   * Do *NOT* use directly
   *
   * ATTENTION: This field is not synchronized - since it is only used as estimation.
   */
  var cacheMissCounter: Int = 0

  /**
   * Removes all elements the predicate returns true for
   */
  fun removeIf(predicate: (K) -> Boolean) {
    lock.write {
      val iterator = map.iterator()

      while (iterator.hasNext()) {
        val entry = iterator.next()

        if (predicate(entry.key)) {
          iterator.remove()
        }
      }
    }
  }

  operator fun get(key: K): V? {
    lock.read {
      val result = map[key]
      if (result != null) {
        cacheHitCounter++
      }
      return result
    }
  }

  inline operator fun set(key: K, value: V) {
    store(key, value)
  }

  /**
   * Stores a new value in the cache. Returns the old value - if there has been one
   */
  fun store(key: K, value: V): V? {
    lock.write {
      return map.put(key, value)
    }
  }

  /**
   * Returns the value for the given key. If the key is not found in the cache, calls the [provider] function,
   * puts its result into the cache under the given key and returns it.
   *
   * Note that the operation is not guaranteed to be atomic.
   */
  inline fun getOrStore(key: K, provider: () -> V): V {
    //First check without write lock
    val found = get(key)
    if (found != null) {
      return found
    }

    lock.write {
      return map.getOrPut(key) {
        cacheMissCounter++
        provider()
      }
    }
  }

  /**
   * Clears the cache
   */
  fun clear() {
    lock.write {
      map.clear()
    }
  }

  fun remove(k: K): V? {
    lock.write {
      return map.remove(k)
    }
  }

  operator fun contains(k: K): Boolean {
    lock.read {
      return map.contains(k)
    }
  }

  override fun toString(): String {
    lock.read {
      return map.toString()
    }
  }

  /**
   * Marks the entry with the given [key] as new
   */
  fun markAsNew(key: K) {
    lock.write {
      map.markAsNew(key)
    }
  }

  /**
   * Calls the callback for each entry.
   *
   * ATTENTION: Only holds a read lock!
   */
  fun forEach(callback: (K, V) -> Unit) {
    lock.read {
      map.forEach { entry ->
        callback(entry.key, entry.value)
      }
    }
  }
}

/**
 * Creates a new cache. Use this method when creating a cache to allow registration of observers
 *
 * [freed] is called whenever an element has been removed from the cache
 */
@Suppress("DEPRECATION")
@JvmOverloads
fun <K, V> cache(
  description: String,
  maxSize: Int,
  /**
   * Is called when an element has been removed from the cache
   */
  freed: (K, V) -> Unit = { _, _ -> }
): Cache<K, V> {
  return cacheStatsHandler.let {
    if (it != null) {
      Cache<K, V>(maxSize) { k, v ->
        it.freed(description, k, v)
        freed(k, v)
      }.also { cache ->
        it.cacheCreated(description, cache)
      }
    } else {
      Cache(maxSize, freed)
    }
  }
}

/**
 * The cache stats handler that can be registered and will be notified about caches
 */
var cacheStatsHandler: CacheStatsHandler? = null


/**
 * Handles cache statistics
 */
interface CacheStatsHandler {
  /**
   * Is called when a cache has been created
   */
  fun <K, V> cacheCreated(description: String, cache: Cache<K, V>)

  /**
   * Is called when a key has been removed
   */
  fun <K, V> freed(description: String, k: K, v: V)
}
