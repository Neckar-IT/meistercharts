/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.test.utils

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.removeWhitespaces
import it.neckar.open.kotlin.lang.toBase64
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.function.Executable
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
  if (it.get().not()) return
  expected("false")
}

fun Assert<AtomicBoolean>.isTrue(): Unit = given {
  if (it.get()) return
  expected("true")
}

fun Assert<File>.doesNotExist(): Unit = given { actual ->
  if (actual.exists().not()) return
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
    return@given
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
    return@given
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


/**
 * Compares both objects. If they are not equal, the JSON representation is compared.
 *
 * This method is very useful for complex objects and simplify debugging.
 */
fun <T> Assert<T>.isEqualToUsingJson(
  expected: T,
) {
  given { current ->
    if (current == expected) {
      return
    }

    assertThat(objectMapper.writeValueAsString(current)).isJsonEqualTo(
      objectMapper.writeValueAsString(expected),
    )
  }
}

fun dumpAsJson(obj: Any): String {
  return objectMapper.writeValueAsString(obj)
}

private val objectMapper = ObjectMapper().apply {
  registerModule(kotlinModule())
  registerModule(Jdk8Module())
  registerModule(JavaTimeModule())
  registerModule(ParameterNamesModule())
}

private typealias SuspendExecutableCollection = Collection<suspend () -> Unit>

/**
 * Asserts that all suspend functions in the given collection can be executed without throwing an exception.
 *
 * This is useful for testing collections of suspend functions.
 */
inline fun assertAllSuspend(executables: SuspendExecutableCollection, checkForPlausibleSize: Boolean = true) {
  require(executables.isNotEmpty()) { "The collection of suspend functions must not be empty" }
  if (checkForPlausibleSize) {
    require(executables.size > 1) { "The collection of suspend functions should contain at least two elements. Did you use map correctly?" }
  }

  Assertions.assertAll(executables.convert())
}

fun SuspendExecutableCollection.convert(): List<Executable> = map { suspendExecutable ->
  Executable({
    runBlocking {
      suspendExecutable()
    }
  })
}

inline fun <reified T> Assert<List<T>>.containsExactlyList(expectedElements: List<T>) {
  this.containsExactly(*expectedElements.toTypedArray())
}

inline fun <reified T> Assert<List<T>>.containsExactlyInAnyOrderList(expectedElements: List<T>) {
  hasSize(expectedElements.size)

  all {
    expectedElements.fastForEach {
      contains(it)
    }
  }
}

/**
 * Asserts that the list does not contain null elements
 */
fun <T> Assert<List<T>?>.doesNotContainNull() {
  given { actual ->
    if (actual == null) {
      fail("List is null")
    } else {
      actual.fastForEachIndexed { index, element ->
        if (element == null) {
          fail("Element at index $index is null")
        }
      }
    }
  }
}

/**
 * Asserts that the value is not null and runs the given [assertions] on the non-null value.
 *
 * Replaces the common pattern of `assertThat(value).isNotNull(); assertThat(value!!.x).isEqualTo(...)`
 * with a single readable chain. All [assertions] are grouped inside `all { }` so multiple failures
 * are collected.
 *
 * Example:
 * ```
 * val name: String? = fetch()
 * assertThat(name).isNotNullAnd {
 *   isEqualTo("Alice")
 *   hasLength(5)
 * }
 * ```
 */
fun <T> Assert<T?>.isNotNullAnd(assertions: Assert<T & Any>.() -> Unit) {
  isNotNull().all(assertions)
}

/**
 * Asserts that the list has exactly one element and runs the given [assertions] on it.
 *
 * Combines the size check + single element extraction + property assertions into one readable
 * operation. Multiple failures inside [assertions] are collected via `all { }`.
 *
 * Example:
 * ```
 * assertThat(loaded.editions).hasSingleElement {
 *   prop(BookEdition::copies).isEqualTo(Edition.Copies(5000))
 * }
 * ```
 */
fun <T> Assert<List<T>>.hasSingleElement(assertions: Assert<T>.() -> Unit) {
  single().all(assertions)
}

/**
 * Asserts that the throwable's message contains every given fragment (each fragment must appear
 * somewhere in the message; order and position are not checked).
 *
 * Requires at least one fragment ([firstFragment] is separate from [additionalFragments] so the
 * empty-call is a compile error rather than a runtime failure).
 *
 * Example:
 * ```
 * assertFailure { doSomething() }.messageContainsAllOf(
 *   "Serializer for class 'Foo' is not found",
 *   "Please provide",
 * )
 * ```
 */
fun Assert<Throwable>.messageContainsAllOf(firstFragment: String, vararg additionalFragments: String): Unit = given { actual ->
  val allFragments = listOf(firstFragment) + additionalFragments

  val message = actual.message
    ?: expected("message to contain ${show(allFragments)} but message was null")

  val missing = allFragments.filterNot { message.contains(it) }
  if (missing.isNotEmpty()) {
    expected("message to contain all of ${show(allFragments)} but missing ${show(missing)} in ${show(message)}")
  }
}
