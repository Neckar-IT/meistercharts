package it.neckar.open.test.utils

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.removeWhitespaces
import it.neckar.open.kotlin.lang.toBase64
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Peeks into the actual value

 */
fun <T> Assert<T>.peek(
  block: (T) -> Unit,
): Assert<T> = apply { given(block) }

/**
 * Returns the value of the assertion
 */
fun <T> Assert<T>.value(): T {
  given {
    return it
  }

  throw IllegalStateException("This should never be reached")
}

/**
 *
 */
fun Assert<AtomicBoolean>.isFalse(): Unit = given {
  if (!it.get()) return
  expected("false")
}

fun Assert<AtomicBoolean>.isTrue(): Unit = given {
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

  //Find the first line that is different
  actualLines.fastForEachIndexed { index, actualLine ->
    val expectedLine = expectedLines.getOrNull(index)

    if (actualLine != expectedLine) {
      fail("Comparing Lines failed @ line $index", expected, actual)
    }
  }

  fail("Comparing Lines Time failed", expected, actual)
}

/**
 * Compare the content of the string ignoring whitespaces and empty lines
 */
fun Assert<String>.isEqualIgnoringWhitespacesEmptyLines(expected: String) = given { actual ->
  if (expected == actual) {
    return
  }

  //Left is expected
  //Right is actual
  val expectedNonEmptyLines = expected.getRelevantLines().toList()
  val actualNonEmptyLines = actual.getRelevantLines().toList()

  //Compare the lines
  if (expectedNonEmptyLines
      .map {
        it.removeWhitespaces()
      }
      .toList() == actualNonEmptyLines
      .map {
        it.removeWhitespaces()
      }
      .toList()
  ) {
    return
  }

  //Find the first failing line
  expectedNonEmptyLines.fastForEachIndexed { index, expectedLine ->
    val actualLine = actualNonEmptyLines.getOrNull(index)

    if (expectedLine.removeWhitespaces() != actualLine?.removeWhitespaces()) {
      fail("Comparing Lines failed @ line $index\n$expectedLine\n$actualLine", expected, actual)
    }
  }

  fail("Comparing Lines failed", expected, actual)
}

/**
 * Compares the base64 representation of the byte arrays
 */
fun Assert<ByteArray>.isEqualBase64(expected: ByteArray): Unit = given { actual ->
  assertk.assertThat(actual.toBase64()).isEqualTo(expected.toBase64())
}

fun Assert<ByteArray>.isEqualTo(expected: ByteArray): Unit = given { actual ->
  if (actual.contentEquals(expected)) {
    return
  }

  fail(expected.toBase64(), actual.toBase64())
}

private fun String.getRelevantLines() = lineSequence()
  .map { it.trim().trimIndent() }
  .filter { it.isNotBlank() }
