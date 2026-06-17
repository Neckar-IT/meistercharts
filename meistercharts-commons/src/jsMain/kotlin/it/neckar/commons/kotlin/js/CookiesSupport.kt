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
    return findCookieValue(document.cookie, cookieName) ?: throw IllegalStateException("No cookie for [$cookieName] was found")
  }

  fun findCookieValue(cookieName: CookieName): String? {
    return findCookieValue(document.cookie, cookieName)
  }

  /**
   * Parses the document cookies string.
   *
   * Browsers can occasionally hand out cookies whose token does not split into exactly
   * `key=value` pairs (third-party cookies, malformed values). Skip those instead of
   * throwing — otherwise a single bad cookie breaks every lookup. Aligns with
   * [deleteAllCookiesForKey], which already tolerated this case.
   */
  internal fun findCookieValue(cookiesFromDocument: String, cookieName: CookieName): String? {
    cookiesFromDocument.split(';').filter { it.isNotBlank() }
      .fastForEach { cookieValue ->
        val splitToken = cookieValue.split("=")
        if (splitToken.size != 2) {
          console.error("Invalid Cookie token [$cookieValue]. Could not split token at '='")
          return@fastForEach
        }

        val key = splitToken[0].trim()
        if (key == cookieName.value) {
          return splitToken[1].trim()
        }
      }

    return null
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
    document.cookie.split(';')
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .forEach { token: String ->
      val splitToken = token.split("=")
        if (splitToken.size != 2) {
          //Invalid cookie - ignore
          console.error("Invalid Cookie token [$token]. Could not split token at '='")
          return@forEach
        }

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

