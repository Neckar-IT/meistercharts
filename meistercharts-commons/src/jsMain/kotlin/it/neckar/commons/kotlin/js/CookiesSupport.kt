package it.neckar.commons.kotlin.js

import it.neckar.commons.js.CookieName
import it.neckar.open.collections.fastForEach
import kotlinx.browser.document

/**
 * Supports reading and writing of cookies
 */
object CookiesSupport {
  /**
   * Searches the cookies for the given name and returns the value of the specified cookie if the cookie is present. Throws exception if the cookie is not present.
   */
  fun getCookieValue(cookieName: CookieName): String {
    document.cookie.split(';').fastForEach { token ->
      val splitToken = token.split("=")
      if (splitToken.size != 2) throw IllegalStateException("Cookie $token is invalid")

      val key = splitToken[0].trim()
      if (key == cookieName.value) {
        return splitToken[1].trim()
      }
    }

    throw IllegalStateException("No cookie for $cookieName was found")
  }

  /**
   * Creates a new Cookie with the given name and value. If a cookie with the same name already exists, the value of the cookie is updated
   */
  fun setCookie(cookieName: CookieName, cookieValue: String, path: String = "/", sameSite: SameSite = SameSite.Lax) {
    document.cookie = "$cookieName=$cookieValue;path=$path;SameSite=${sameSite.name}"
  }

  /**
   * Deletes the cookie with the given name
   */
  fun deleteAllCookiesForKey(cookieName: CookieName) {
    document.cookie.split(';').forEach { token: String ->
      val splitToken = token.split("=")
      val key = splitToken[0].trim()
      val value = splitToken[1].trim()

      if (key == cookieName.value) {
        document.cookie = "$cookieName=$value;max-age=-1"
        return
      }
    }
  }

  /**
   * Contains all possible values for the SameSite attribute. If None is chosen the 'Secure' attribute must be used as well.
   * Source: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#samesitesamesite-value
   */
  enum class SameSite {
    /**
     *  means that the browser sends the cookie with both cross-site and same-site requests. The Secure attribute must also be set when setting this value, like so SameSite=None; Secure. If Secure is missing an error will be logged:
     */
    None,

    /**
     * Means that the cookie is not sent on cross-site requests, such as on requests to load images or frames, but is sent when a user is navigating to the origin site from an external site (for example, when following a link). This is the default behavior if the SameSite attribute is not specified.
     */
    Lax,

    /**
     * Means that the browser sends the cookie only for same-site requests, that is, requests originating from the same site that set the cookie. If a request originates from a different domain or scheme (even with the same domain), no cookies with the SameSite=Strict attribute are sent.
     */
    Strict
  }
}

