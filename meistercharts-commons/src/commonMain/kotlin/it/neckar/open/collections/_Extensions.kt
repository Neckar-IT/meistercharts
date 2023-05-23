package it.neckar.open.collections

import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

inline fun count(cond: (index: Int) -> Boolean): Int {
  var counter = 0
  while (cond(counter)) counter++
  return counter
}

inline fun <reified T> mapWhile(cond: (index: Int) -> Boolean, gen: (Int) -> T): List<T> = arrayListOf<T>().apply { while (cond(this.size)) this += gen(this.size) }
inline fun <reified T> mapWhileArray(cond: (index: Int) -> Boolean, gen: (Int) -> T): Array<T> = mapWhile(cond, gen).toTypedArray()
inline fun mapWhileInt(cond: (index: Int) -> Boolean, gen: (Int) -> Int): IntArray = IntArrayList().apply { while (cond(this.size)) this += gen(this.size) }.toIntArray()
inline fun mapWhileFloat(cond: (index: Int) -> Boolean, gen: (Int) -> Float): FloatArray = FloatArrayList().apply { while (cond(this.size)) this += gen(this.size) }.toFloatArray()
inline fun mapWhileDouble(cond: (index: Int) -> Boolean, gen: (Int) -> Double): DoubleArray = DoubleArrayList().apply { while (cond(this.size)) this += gen(this.size) }.toDoubleArray()

fun <T> List<T>.getCyclic(index: Int) = this[index umod this.size]
fun <T> List<T>.getCyclicOrNull(index: Int) = this.getOrNull(index umod this.size)
fun <T> Array<T>.getCyclic(index: Int) = this[index umod this.size]
fun IntArrayList.getCyclic(index: Int) = this.getAt(index umod this.size)
fun FloatArrayList.getCyclic(index: Int) = this.getAt(index umod this.size)
fun DoubleArrayList.getCyclic(index: Int) = this.getAt(index umod this.size)

fun <T> Array2<T>.getCyclic(x: Int, y: Int) = this[x umod this.width, y umod this.height]
fun IntArray2.getCyclic(x: Int, y: Int) = this[x umod this.width, y umod this.height]
fun FloatArray2.getCyclic(x: Int, y: Int) = this[x umod this.width, y umod this.height]
fun DoubleArray2.getCyclic(x: Int, y: Int) = this[x umod this.width, y umod this.height]

fun <T : Comparable<T>> comparator(): Comparator<T> = kotlin.Comparator { a, b -> a.compareTo(b) }

fun <K, V> linkedHashMapOf(vararg pairs: Pair<K, V>): LinkedHashMap<K, V> = LinkedHashMap<K, V>().also { for ((key, value) in pairs) it[key] = value }
fun <K, V> Iterable<Pair<K, V>>.toLinkedMap(): LinkedHashMap<K, V> = LinkedHashMap<K, V>().also { for ((key, value) in this) it[key] = value }

fun <K, V> Map<K, V>.flip(): Map<V, K> = this.map { Pair(it.value, it.key) }.toMap()
fun <T> List<T>.countMap(): Map<T, Int> = LinkedHashMap<T, Int>().also { for (key in this) it.incr(key, +1) }

fun <K> MutableMap<K, Int>.incr(key: K, delta: Int = +1): Int {
  val next = this.getOrPut(key) { 0 } + delta
  this[key] = next
  return next
}

/**
 * Returns the index of an item or a negative number in the case the item is not found.
 * The negative index represents the nearest position after negating + 1.
 */
fun IntArray.binarySearch(v: Int, fromIndex: Int = 0, toIndex: Int = size): BSearchResult = (genericBinarySearchResult(fromIndex, toIndex) { this[it].compareTo(v) })
fun FloatArray.binarySearch(v: Float, fromIndex: Int = 0, toIndex: Int = size): BSearchResult = (genericBinarySearchResult(fromIndex, toIndex) { this[it].compareTo(v) })
fun DoubleArray.binarySearch(v: Double, fromIndex: Int = 0, toIndex: Int = size): BSearchResult = (genericBinarySearchResult(fromIndex, toIndex) { this[it].compareTo(v) })
fun IntArrayList.binarySearch(v: Int, fromIndex: Int = 0, toIndex: Int = size): BSearchResult = (genericBinarySearchResult(fromIndex, toIndex) { this.getAt(it).compareTo(v) })
fun FloatArrayList.binarySearch(v: Int, fromIndex: Int = 0, toIndex: Int = size): BSearchResult = (genericBinarySearchResult(fromIndex, toIndex) { this.getAt(it).compareTo(v) })
fun DoubleArrayList.binarySearch(v: Double, fromIndex: Int = 0, toIndex: Int = size): BSearchResult = (genericBinarySearchResult(fromIndex, toIndex) { this.getAt(it).compareTo(v) })
fun <T : Comparable<T>> Array<out T>.binarySearch(v: T, fromIndex: Int = 0, toIndex: Int = size): BSearchResult = (genericBinarySearchResult(fromIndex, toIndex) { this[it].compareTo(v) })
fun <T> Array<out T>.binarySearch(v: T, fromIndex: Int = 0, toIndex: Int = size, comparator: Comparator<in T>): BSearchResult = genericBinarySearchResult(fromIndex, toIndex) { index ->
  comparator.compare(this[index], v)
}

fun <T, K : Comparable<K>> Array<out T>.binarySearchBy(
  v: K,
  fromIndex: Int = 0,
  toIndex: Int = size,
  keyExtractor: (T) -> K,
): BSearchResult {
  return genericBinarySearchResult(fromIndex, toIndex) { index ->
    keyExtractor(this[index]).compareTo(v)
  }
}

fun IntArray.binarySearchLeft(v: Int, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchLeft(fromIndex, toIndex) { this[it].compareTo(v) })
fun FloatArray.binarySearchLeft(v: Float, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchLeft(fromIndex, toIndex) { this[it].compareTo(v) })
fun DoubleArray.binarySearchLeft(v: Double, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchLeft(fromIndex, toIndex) { this[it].compareTo(v) })
fun IntArrayList.binarySearchLeft(v: Int, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchLeft(fromIndex, toIndex) { this.getAt(it).compareTo(v) })
fun FloatArrayList.binarySearchLeft(v: Float, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchLeft(fromIndex, toIndex) { this.getAt(it).compareTo(v) })
fun DoubleArrayList.binarySearchLeft(v: Double, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchLeft(fromIndex, toIndex) { this.getAt(it).compareTo(v) })

fun IntArray.binarySearchRight(v: Int, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchRight(fromIndex, toIndex) { this[it].compareTo(v) })
fun FloatArray.binarySearchRight(v: Float, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchRight(fromIndex, toIndex) { this[it].compareTo(v) })
fun DoubleArray.binarySearchRight(v: Double, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchRight(fromIndex, toIndex) { this[it].compareTo(v) })
fun IntArrayList.binarySearchRight(v: Int, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchRight(fromIndex, toIndex) { this.getAt(it).compareTo(v) })
fun FloatArrayList.binarySearchRight(v: Float, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchRight(fromIndex, toIndex) { this.getAt(it).compareTo(v) })
fun DoubleArrayList.binarySearchRight(v: Double, fromIndex: Int = 0, toIndex: Int = size) = (genericBinarySearchRight(fromIndex, toIndex) { this.getAt(it).compareTo(v) })

inline fun genericBinarySearchResult(fromIndex: Int, toIndex: Int, check: (value: Int) -> Int): BSearchResult = BSearchResult(genericBinarySearch(fromIndex, toIndex, { _, _, low, _ -> -low - 1 }, check))

/**
 * Returns the exact index or the *left* index if there is no exact match.
 * The return values of the given check lambda can be interpreted as:
 * * <0 --> the given index is too small
 * * >0 --> the given index is too large
 * * ==0 --> the given index matches exactly
 *
 * @param check compares the given index with the value to find
 *
 * @return the best index that matches the given check. Always returns a value between fromIndex (inclusive) and toIndex (exclusive)
 *
 */
inline fun genericBinarySearchLeft(fromIndex: @Inclusive Int, toIndex: @Exclusive Int, check: (value: Int) -> Int): Int =
  genericBinarySearch(fromIndex, toIndex, invalid = { from, to, low, high -> min(low, high).coerceIn(from, to - 1) }, check = check)

/**
 * Returns the exact index or the *right* index if there is no exact match.
 *
 * Returns the same value as [genericBinarySearchLeft] as long as an exact hit is found.
 * Only returns different values for hits *between* two values
 */
inline fun genericBinarySearchRight(fromIndex: @Inclusive Int, toIndex: @Exclusive Int, check: (value: Int) -> Int): Int =
  genericBinarySearch(fromIndex, toIndex, invalid = { from, to, low, high -> max(low, high).coerceIn(from, to - 1) }, check = check)

inline fun genericBinarySearch(
  fromIndex: Int,
  toIndex: Int,
  invalid: (from: Int, to: Int, low: Int, high: Int) -> Int = { from, to, low, high -> -low - 1 },
  check: (value: Int) -> Int
): Int {
  var low = fromIndex
  var high = toIndex - 1

  while (low <= high) {
    val mid = (low + high) / 2
    val mval = check(mid)

    when {
      mval < 0 -> low = mid + 1
      mval > 0 -> high = mid - 1
      else -> return mid
    }
  }
  return invalid(fromIndex, toIndex, low, high)
}

/**
 * Result of a binary search
 */
@JvmInline
value class BSearchResult(val raw: Int) {
  /**
   * Returns true if an exact result has been found
   */
  val found: Boolean get() = raw >= 0

  /**
   * Returns the exact index if found, -1 if not found
   */
  val index: Int get() = if (found) raw else -1

  /**
   * Returns the near index.
   * This index can/should be used to insert an element
   */
  val nearIndex: Int get() = if (found) raw else -raw - 1

  override fun toString(): String {
    return "BSearchResult(found=$found, nearIndex=$nearIndex)"
  }

  companion object {
    fun found(index: Int): BSearchResult {
      return BSearchResult(index)
    }

    fun insertAt(nearIndex: Int): BSearchResult {
      return BSearchResult(-nearIndex - 1)
    }
  }
}
