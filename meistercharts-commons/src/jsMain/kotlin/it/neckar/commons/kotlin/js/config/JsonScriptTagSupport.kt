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
  val json: Json = Json,
) {

  /**
   * Parses the JSON data from a script tag with the given id.
   *
   * This can be used to find script tags that might be anywhere in the document.
   */
  inline fun <reified T> parseTagById(id: String): T {
    return decode<T>(getJsonContentById(id))
  }

  inline fun <reified T> decode(jsonString: String): T = json.decodeFromString<T>(jsonString)

  /**
   * Returns the JSON content from the script tag with the given ID
   */
  fun getJsonContentById(id: String): String {
    val foundElement = document.getElementById(id) ?: throw IllegalStateException("No element found with id [$id]")

    val jsonElement = (foundElement as HTMLScriptElement)
    return jsonElement.innerText
  }
}
