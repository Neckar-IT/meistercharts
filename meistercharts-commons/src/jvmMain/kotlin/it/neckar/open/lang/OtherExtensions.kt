package it.neckar.open.lang

import java.net.URL
import java.util.Optional
import java.util.UUID


/**
 * Returns value or null from Optional. Useful when using kotlin-like T? and Optional<T>.
 * */
fun <T> Optional<T>.orNull(): T? = this.orElse(null)

/**
 * Creates a string like "className(description)", for example "Double(42.0)"
 * Useful e.g. for implementing .toString() override.
 *
 * @param description the content to be displayed inside the brackets.
 * @param brackets a two-character string like "{}", default = "()".
 * @param className the string to be displayed before the brackets; default = the class name of [this].
 */
fun Any.toLongString(description: String, brackets: String = "()", className: String? = null): String {
  val actualClassName = className ?: this.javaClass.simpleName
  val actualBrackets = if (brackets.length == 2) brackets else "<>"
  return "$actualClassName${actualBrackets[0]}$description${actualBrackets[1]}"
}

/**
 * Check whether a given string is a valid UUID.
 *
 * @param candidateUUID A candidate UUID to be checked.
 * @return true iff [candidateUUID] is a valid UUID.
 */
fun isUUID(candidateUUID: String): Boolean = runCatching { UUID.fromString(candidateUUID) }.isSuccess

/**
 * Check whether a given string is a valid URL.
 *
 * Please note that this function is not all mighty and it only tries to convert the given string to URL and then to URI.
 * This means that it fails to recognize invalid urls such as  `https://sm.ai,` or `https://`.
 * For more complex validation, one should probably use Apache URL Validator.
 *
 * For the sample cases when this simple method fails please see `isUrlFalsePositives` test in the file OtherExtensionsTest.kt.
 *
 * @param candidateUrl A candidate URL to be checked.
 * @return true iff [candidateUrl] is a valid URL.
 */
fun isURL(candidateUrl: String): Boolean = runCatching { URL(candidateUrl).toURI() }.isSuccess

/**
 * Retrieves environment variable from the system.
 */
fun getEnv(variableName: String): String? = System.getenv(variableName)

/**
 * Shortcut for [System.lineSeparator].
 */
val newLine: String get() = System.lineSeparator()
