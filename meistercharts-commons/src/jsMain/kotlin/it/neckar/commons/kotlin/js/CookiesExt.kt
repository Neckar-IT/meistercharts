package it.neckar.commons.kotlin.js

import kotlinx.browser.document

object CookiesSupport {
  /** Searches the cookies for the given name and returns the value of the specified cookie if the cookie is present. Throws exception if the cookie is not present.*/
  fun getCookieValue(cookieName: String): String {
    val cookieMap = mutableMapOf<String, String>();
    document.cookie.split(';').forEach { token: String ->
      val splitToken = token.split("=")
      cookieMap[splitToken[0].trim()] = splitToken[1].trim()
    }
    return cookieMap[cookieName] ?: throw Exception("access Token: $cookieName was not found in keys: ${cookieMap.keys}")
  }

/** Creates a new Cookie with the given name and value. If a cookie with the same name already exists, the value of the cookie is updated */
  fun setCookie(cookieName: String, cookieValue: String){
    document.cookie = "$cookieName=$cookieValue"
  }


}
