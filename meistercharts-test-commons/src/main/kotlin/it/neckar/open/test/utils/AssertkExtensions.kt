package it.neckar.open.test.utils

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte1.other
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 */
fun Assert<AtomicBoolean>.isFalse() = given {
  if (!it.get()) return
  expected("false")
}

fun Assert<AtomicBoolean>.isTrue() = given {
  if (it.get()) return
  expected("true")
}

fun Assert<File>.doesNotExist(): Unit = given { actual ->
  if (!actual.exists()) return
  expected("to not exist")
}

fun <T> Assert<List<T>>.containsExactly(vararg elements: T) {
  all {
    hasSize(elements.size)
    isEqualTo(elements.toList())
  }
}

fun <T> Assert<List<T>>.anyMatch(predicate: (T) -> Boolean): Unit = given {
  if (it.any(predicate)) return
  expected("to have at least one match:${show(predicate)}")
}

fun Assert<Double>.isNaN(): Unit = given {
  if (it.isNaN()) return
  expected("to be NaN but was ${show(it)}")
}


/**
 * Runs the test on the first element of the given iterable
 */
fun <E> Assert<Iterable<E>>.first(f: (Assert<E>) -> Unit): Unit = given { actual ->
  all {
    val firstElement = actual.first()
    f(assertThat(firstElement, name = "${name.orEmpty()}${show(firstElement, "[]")}"))
  }
}

fun <E> Assert<List<E>>.last(f: (Assert<E>) -> Unit): Unit = given { actual ->
  all {
    val lastElement = actual.last()
    f(assertThat(lastElement, name = "${name.orEmpty()}${show(lastElement, "[]")}"))
  }
}

fun Assert<DoubleArray>.isCloseTo(expected: DoubleArray, delta: Double): Unit = given {
  this.hasSize(expected.size)

  it.withIndex().forEach { (index, currentValue) ->
    val expectedValue = expected[index]
    assertThat(currentValue).isCloseTo(expectedValue, delta)
  }
}


/**
 * Compares all lines - trimming each line
 */
fun Assert<String>.isEqualComparingLinesTrim(expected: String): Unit = given { actual ->
  val actualLines = actual.getRelevantLines().toList()
  val expectedLines = expected.getRelevantLines().toList()

  if (actualLines == expectedLines) {
    return
  }

  fail(expected, actual)
}

private fun String.getRelevantLines() = lineSequence()
  .map { it.trim() }
  .filter { it.isNotBlank() }
