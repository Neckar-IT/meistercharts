package it.neckar.open.environment

import it.neckar.open.http.Url
import it.neckar.open.kotlin.lang.requireNotNull

/**
 * The server root URL - should be configured in the docker compose as "environment"
 */
const val SERVER_ROOT_URL: String = "SERVER_ROOT_URL"

/**
 * Offers access to well-known server environment variables
 */
object ServerEnvironment {
  /**
   * The server root URL
   */
  fun findServerRootUrl(): Url.Absolute? {
    val valueAsString = System.getenv(SERVER_ROOT_URL)
    return valueAsString?.let {
      return Url.Absolute(it)
    }
  }

  fun getServerRootUrl(): Url.Absolute? {
    return findServerRootUrl().requireNotNull {
      "The server root URL is not set. Please set the environment variable $SERVER_ROOT_URL"
    }
  }
}
