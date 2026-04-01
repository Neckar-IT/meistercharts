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
package it.neckar.commons.kotlin.js.config

import kotlinx.browser.document
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLScriptElement


/**
 * Supports parsing of JSON script tags:
 *
 * ```
 *     <script type="application/json" id="my-json-data">{
 *       "foo": "asdf",
 *       "fooBar": 1234
 *     }</script>
 *
 * ```
 */
class JsonScriptTagSupport(
  /**
   * The JSON parser used to parse the JSON data
   */
  val jsonDecoder: Json,
) {

  /**
   * Parses the JSON data from a script tag with the given id.
   *
   * This can be used to find script tags that might be anywhere in the document.
   */
  inline fun <reified T> parseTagById(id: String): T {
    return decode<T>(getJsonContentById(id))
  }

  /**
   * Decodes the given JSON string
   */
  inline fun <reified T> decode(jsonString: String): T = jsonDecoder.decodeFromString<T>(jsonString)

  /**
   * Returns the JSON content from the script tag with the given ID
   */
  fun getJsonContentById(id: String): String {
    val foundElement = document.getElementById(id) ?: throw IllegalStateException("No element found with id [$id]")

    val jsonElement = (foundElement as HTMLScriptElement)
    return jsonElement.innerText
  }
}
