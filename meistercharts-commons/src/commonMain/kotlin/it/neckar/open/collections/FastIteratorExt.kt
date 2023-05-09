package it.neckar.open.collections


/**
 * Iterates over the entries of this [ByteArray]
 */
inline fun ByteArray.fastForEach(callback: (value: Byte) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this[n++])
  }
}

/**
 * Iterates over the entries of this [ByteArray]
 */
inline fun ByteArray.fastForEachIndexed(callback: (index: Int, value: Byte) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this[n++])
  }
}

/**
 * Iterates over the entries of a int array
 *
 * Do *not* modify the underlying data structure while iterating
 */
inline fun IntArray.fastForEach(callback: (value: Int) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this[n++])
  }
}

inline fun IntArray.fastForEachIndexed(callback: (index: Int, value: Int) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this[n++])
  }
}

inline fun BooleanArray.fastForEach(callback: (value: Boolean) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this[n++])
  }
}

inline fun BooleanArray.fastForEachIndexed(callback: (index: Int, value: Boolean) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this[n++])
  }
}

/**
 * Iterates over the entries of a double array
 *
 * Do *not* modify the underlying data structure while iterating
 */
inline fun DoubleArray.fastForEach(callback: (value: Double) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this[n++])
  }
}

inline fun DoubleArray.fastAny(predicate: DoublePredicate): Boolean {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    val element = this[n++]
    if (predicate(element)) {
      return true
    }
  }

  return false
}

/**
 * Returns true if [predicate] returns true for all elements
 */
inline fun DoubleArray.fastAll(predicate: DoublePredicate): Boolean {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    val element = this[n++]
    if (!predicate(element)) {
      return false
    }
  }

  return true
}

/**
 * Iterates over the entries of a double array
 *
 * Do *not* modify the underlying data structure while iterating
 */
inline fun DoubleArray.fastForEachIndexed(callback: (index: Int, value: Double) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this[n++])
  }
}

inline fun DoubleArray.fastForEachIndexedReverse(callback: (index: Int, value: Double, isFirst: Boolean) -> Unit) {
  val currentSize = size

  for (i in (currentSize - 1) downTo 0) {
    callback(i, this[i], i == currentSize - 1)
  }
}

fun DoubleArray.fastContains(value: Double): Boolean {
  this.fastForEach {
    if (it == value) {
      return true
    }
  }

  return false
}

/**
 * Iterates over the entries of a list
 *
 * Do *not* modify the underlying data structure while iterating
 */
inline fun <T> List<T>.fastForEach(callback: (value: T) -> Unit) {
  fastForEach(size, callback)
}

/**
 * ATTENTION: Only required if the size of the list is forced to another value!
 *
 * Usually the method without the parameter [currentSize] should be used
 */
inline fun <T> List<T>.fastForEach(currentSize: Int, callback: (value: T) -> Unit) {
  var n = 0
  while (n < currentSize) {
    callback(this[n++])
  }
}

inline fun <T, V> List<T>.fastMapNotNull(mapper: (value: T) -> V?): List<V> {
  val targetList = mutableListOf<V>()

  this.fastForEach { original ->
    mapper(original)?.let {
      targetList.add(it)
    }
  }

  return targetList
}

inline fun <T, V> List<T>.fastMap(mapper: (value: T) -> V): List<V> {
  val targetList = mutableListOf<V>()

  this.fastForEach { original ->
    mapper(original).let {
      targetList.add(it)
    }
  }

  return targetList
}

inline fun <V> DoubleArray.fastMap(mapper: (value: Double) -> V): List<V> {
  val targetList = mutableListOf<V>()

  this.fastForEach { original ->
    mapper(original).let {
      targetList.add(it)
    }
  }

  return targetList
}

inline fun DoubleArray.fastMapDouble(mapper: (value: Double) -> Double): DoubleArray {
  return DoubleArray(size) {
    mapper(this[it])
  }
}


/**
 *
 */
inline fun <S, T : S> List<T>.fastReduce(operation: (acc: S, T) -> S): S {
  require(this.isNotEmpty()) { "not supported for empty lists" }

  var reduced: S = get(0)

  fastForEachIndexed { index, value ->
    if (index == 0) {
      return@fastForEachIndexed
    }

    reduced = operation(reduced, value)
  }

  return reduced
}

inline fun <T> List<T>.fastForEachReversed(callback: (T) -> Unit) {
  val currentSize = size
  var n = currentSize - 1
  while (n >= 0) {
    callback(this[n])
    n--
  }
}

inline fun <T> List<T>.fastForEachFiltered(predicate: (T) -> Boolean, callback: (value: T) -> Unit) {
  fastForEachFiltered(size, predicate, callback)
}

/**
 * ATTENTION: Only required if the size of the list is forced to another value!
 *
 * Usually the method without the parameter [currentSize] should be used
 */
inline fun <T> List<T>.fastForEachFiltered(currentSize: Int, predicate: (T) -> Boolean, callback: (value: T) -> Unit) {
  var n = 0
  while (n < currentSize) {
    val element = this[n++]

    if (predicate(element)) {
      callback(element)
    }
  }
}

inline fun DoubleArray.fastForEachFiltered(predicate: (Double) -> Boolean, callback: (value: Double) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    val element = this[n++]

    if (predicate(element)) {
      callback(element)
    }
  }
}

inline fun <T> Array<T>.fastForEach(callback: (T) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this[n++])
  }
}

inline fun <T> Array<T>.fastForEachReversed(callback: (T) -> Unit) {
  val currentSize = size
  var n = currentSize - 1
  while (n >= 0) {
    callback(this[n])
    n--
  }
}


/**
 * Iterates over the entries of a list
 *
 * Do *not* modify the underlying data structure while iterating
 */
inline fun <T> List<T>.fastForEachIndexed(callback: (index: Int, value: T) -> Unit) {
  fastForEachIndexed(size, callback)
}

/**
 * ATTENTION: Only required if the size of the list is forced to another value!
 *
 * Usually the method without the parameter [currentSize] should be used
 */
inline fun <T> List<T>.fastForEachIndexed(currentSize: Int, callback: (index: Int, value: T) -> Unit) {
  var n = 0
  while (n < currentSize) {
    callback(n, this[n])
    n++
  }
}

inline fun <T> List<T>.fastForEachIndexedAdvanced(callback: (index: Int, isLast: Boolean, value: T) -> Unit) {
  fastForEachIndexedAdvanced(size, callback)
}

inline fun <T> List<T>.fastForEachIndexedAdvanced(currentSize: Int, callback: (index: Int, isLast: Boolean, value: T) -> Unit) {
  var n = 0
  while (n < currentSize) {
    callback(n, n == currentSize - 1, this[n])
    n++
  }
}

inline fun <T> Array<T>.fastForEachIndexed(callback: (index: Int, value: T) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this[n])
    n++
  }
}


inline fun <T> List<T>.fastForEachWithIndex(callback: (index: Int, value: T) -> Unit) {
  var n = 0
  while (n < size) {
    callback(n, this[n])
    n++
  }
}

inline fun <T> Array<T>.fastForEachWithIndex(callback: (index: Int, value: T) -> Unit) {
  var n = 0
  while (n < size) {
    callback(n, this[n])
    n++
  }
}

inline fun <T> List<T>.fastForEachReverse(callback: (T) -> Unit) {
  fastForEachReverse(size, callback)
}

inline fun <T> List<T>.fastForEachReverse(currentSize: Int, callback: (T) -> Unit) {
  var n = 0
  while (n < currentSize) {
    callback(this[currentSize - n - 1])
    n++
  }
}

inline fun <T> ArrayList<T>.fastIterateRemove(callback: (T) -> Boolean): ArrayList<T> {
  var n = 0
  var m = 0
  while (n < size) {
    if (m >= 0 && m != n) this[m] = this[n]
    if (callback(this[n])) m--
    n++
    m++
  }
  while (this.size > m) this.removeAt(this.size - 1)
  return this
}

/**
 * Returns the max value - but always at least [minimumValue].
 *
 * If the list is empty the [minimumValue] is returned
 */
inline fun <T> List<T>.fastMaxBy(minimumValue: Double, callback: (value: T) -> Double): Double {
  val currentSize = size
  if (currentSize == 0) {
    return minimumValue
  }

  var max = minimumValue
  var n = 0
  while (n < currentSize) {
    max = callback(this[n++]).coerceAtLeast(max)
  }

  return max
}

/**
 * Returns the max value - but always at least [minimumValue].
 *
 * If the list is empty the [minimumValue] is returned
 */
inline fun <T> Array<T>.fastMaxBy(minimumValue: Double, callback: (value: T) -> Double): Double {
  val currentSize = size
  if (currentSize == 0) {
    return minimumValue
  }

  var max = minimumValue
  var n = 0
  while (n < currentSize) {
    max = callback(this[n++]).coerceAtLeast(max)
  }

  return max
}

inline fun <T> List<T>.fastSumBy(callback: (value: T) -> Double): Double {
  val currentSize = size
  if (currentSize == 0) {
    return 0.0
  }

  var sum = 0.0
  var n = 0
  while (n < currentSize) {
    sum += callback(this[n++])
  }

  return sum
}
