package it.neckar.open.charting.api.sanitizing

import it.neckar.open.isUndefined
import it.neckar.open.kotlin.lang.enumEntries

/**
 * Ensures that this enum value is in fact an enum value.
 *
 * This is a workaround because the enum values we receive are actually of type string.
 */
actual inline fun <reified T : Enum<T>> T.sanitize(): T {
  if (this.isUndefined()) {
    throw SanitizingFailedException("Could not sanitize [undefined] to Enum.\nPossible values: ${enumEntries<T>().joinToString(", ")}", null)
  }

  try {
    println("This: ")
    console.dir(this)
    console.log("after dir")
    println("Done console.dir")
    println("this: $this")
    println("done printing this")

    return enumValueOf(this.toString())
  } catch (e: Throwable) {
    throwEnumConversionException(this, enumEntries(), e)
  }
}
