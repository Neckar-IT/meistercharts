package it.neckar.commons.js

/**
 * The error message that is returned by the browsers on network errors.
 */
const val FailToFetchErrorMessage: String = "Fail to fetch"

/**
 * Returns true if this throwable has the magic message "Fail to fetch"
 */
fun Throwable.isFailToFetch(): Boolean {
  return this is Error && this.message == FailToFetchErrorMessage
}
