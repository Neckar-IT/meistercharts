package it.neckar.open.charting.api.sanitizing

import it.neckar.open.kotlin.lang.enumEntries


/**
 * Ensures that this enum value is in fact an enum value.
 *
 * This is a workaround because the enum values we receive are actually of type string.
 */
inline fun <reified T : Enum<T>> Enum<T>.sanitize(): T {
  try {
    return enumValueOf(this.toString())
  } catch (e: Throwable) {
    throwEnumConversionException(this, enumEntries(), e)
  }
}

/**
 * Helper method that throws an exception
 */
fun <E : Enum<E>> throwEnumConversionException(value: Enum<E>, enumValues: List<E>, e: Throwable): Nothing {
  throw SanitizingFailedException("Could not sanitize [$value] to Enum.\nPossible values: ${enumValues.joinToString(", ")}", e)
}

/**
 * Sanitizes a JS boolean
 */
@Suppress("SimplifyBooleanWithConstants")
fun Boolean.sanitize(): Boolean {
  return when {
    this == true -> {
      true
    }

    this == false -> {
      false
    }

    else -> throw SanitizingFailedException("Could not sanitize [$this] to Boolean")
  }
}

fun Boolean?.sanitize(): Boolean? {
  if (this == null) {
    return null
  }

  return sanitize()
}

/**
 * Sanitizes a JS double
 */
fun Double.sanitize(): Double {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is Double).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Double")
  }

  return this
}

fun Int.sanitize(): Int {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is Int).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Int")
  }
  return this
}

fun String.sanitize(): String {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is String).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to String")
  }
  return this
}

fun <T> Array<T>.sanitize(): Array<T> {
  @Suppress("USELESS_IS_CHECK") //undefined could be null
  if ((this is Array<T>).not()) {
    throw SanitizingFailedException("Could not sanitize [$this] to Array<T>")
  }
  return this
}

class SanitizingFailedException(message: String, cause: Throwable? = null) : Exception(message, cause) {
}
