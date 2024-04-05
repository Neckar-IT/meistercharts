package it.neckar.open.charting.api.sanitizing

/**
 * Ensures that this enum value is in fact an enum value.
 *
 * This is a workaround because the enum values we receive are actually of type string.
 */
actual inline fun <reified T : Enum<T>> T.sanitize(): T {
  return this as T
}
