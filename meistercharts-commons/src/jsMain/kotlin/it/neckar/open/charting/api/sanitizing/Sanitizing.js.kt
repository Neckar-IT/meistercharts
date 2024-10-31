package it.neckar.open.charting.api.sanitizing

import it.neckar.open.isUndefined

/**
 * Ensures that this enum value is in fact an enum value.
 *
 * This is a workaround because the enum values we receive are actually of type string.
 */
actual inline fun <reified T : Enum<T>> T.sanitize(): T {
  if (this.isUndefined()) {
    throw SanitizingFailedException("Could not sanitize [undefined] to Enum.\nPossible values: ${kotlin.enums.enumEntries<T>().toList().joinToString(", ")}", null)
  }

  try {
    return enumValueOf(this.toString())
  } catch (e: Throwable) {
    throwEnumConversionException(this, kotlin.enums.enumEntries<T>().toList(), e)
  }
}
