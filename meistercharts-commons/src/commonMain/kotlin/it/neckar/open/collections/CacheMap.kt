package it.neckar.open.collections

open class CacheMap<K, V> private constructor(
  private val map: LinkedHashMap<K, V> = LinkedHashMap(),
  maxSize: Int = 16,
  val free: (K, V) -> Unit = { _, _ -> }
) : MutableMap<K, V> by map {
  constructor(
    maxSize: Int = 16,
    free: (K, V) -> Unit = { _, _ -> }
  ) : this(LinkedHashMap(), maxSize, free)

  override val size: Int get() = map.size

  var maxSize: Int = maxSize
    private set
  fun updateMaxSize(newMaxSize: Int) {
    this.maxSize = newMaxSize

    //Reduce the size of the map
    while (size >= maxSize) remove(map.keys.first())
  }

  /**
   * Marks the entry with the given [key] as new
   */
  fun markAsNew(key: K) {
    // do not call 'free' here
    val value = map.remove(key)
    value?.let {
      map[key] = value
    }
  }

  override fun remove(key: K): V? {
    val value = map.remove(key)
    if (value != null) free(key, value)
    return value
  }

  override fun putAll(from: Map<out K, V>) = run { for ((k, v) in from) put(k, v) }
  override fun put(key: K, value: V): V? {
    while (size >= maxSize && map.containsKey(key).not()) {
      val keys = map.keys
      require(keys.size == size){
        "Expect same size for keys: <${keys.size}> and map: <$size>"
      }
      remove(keys.first())
    }

    val oldValue = map[key]
    if (oldValue != value) {
      remove(key) // remove entry first to force a refresh when the new value is put into the map
      map[key] = value
    }
    return oldValue
  }

  override fun clear() {
    val keys = map.keys.toList()
    for (key in keys) remove(key)
  }

  /**
   * Removes all entries the provided predicate returns true for
   */
  fun removeIf(predicate: (K) -> Boolean) {
    val keysToRemove = map.keys.filter(predicate)

    keysToRemove.fastForEach {
      map.remove(it)
    }
  }

  override fun toString(): String = map.toString()

  override fun equals(other: Any?): Boolean = (other is CacheMap<*, *>) && (this.map == other.map) && (this.free == other.free)
  override fun hashCode(): Int = this.map.hashCode() + maxSize
}
