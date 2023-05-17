package it.neckar.open.collections


inline fun IntArrayList.fastForEach(callback: (Int) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this.getAt(n++))
  }
}

inline fun FloatArrayList.fastForEach(callback: (Float) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this.getAt(n++))
  }
}

inline fun DoubleArrayList.fastForEach(callback: (Double) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this.getAt(n++))
  }
}

inline fun DoubleArrayList.fastForEachReversed(callback: (value: Double) -> Unit) {
  var n = lastIndex
  while (n >= 0) {
    callback(this.getAt(n))
    n--
  }
}

/**
 * Returns true if at least one of the elements matches the given check.
 * [check] is called for all elements until it returns true.
 */
inline fun DoubleArrayList.fastFindAny(check: (Double) -> Boolean): Boolean {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    if (check(this.getAt(n++))) {
      return true
    }
  }

  return false
}

inline fun IntArrayList.fastForEachIndexed(callback: (index: Int, value: Int) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this.getAt(n))
    n++
  }
}

inline fun FloatArrayList.fastForEachIndexed(callback: (index: Int, value: Float) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this.getAt(n))
    n++
  }
}

inline fun DoubleArrayList.fastForEachIndexed(callback: (index: Int, value: Double) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this.getAt(n))
    n++
  }
}

inline fun DoubleArrayList.fastForEachIndexedReversed(callback: (index: Int, value: Double) -> Unit) {
  var n = lastIndex
  while (n >= 0) {
    callback(n, this.getAt(n))
    n--
  }
}

inline fun DoubleArrayList.fastAny(predicate: DoublePredicate): Boolean {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    if (predicate(this.getAt(n))) {
      return true
    }
    n++
  }

  return false
}
