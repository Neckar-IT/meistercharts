package it.neckar.commons.kotlin.js.exception


/**
 * Returns true if this throwable has the magic message "Fail to fetch"
 */
fun Throwable.isFailToFetch(): Boolean {
  return this is Error && this.message == FailToFetchErrorMessage
}

const val FailToFetchErrorMessage: String = "Fail to fetch"
