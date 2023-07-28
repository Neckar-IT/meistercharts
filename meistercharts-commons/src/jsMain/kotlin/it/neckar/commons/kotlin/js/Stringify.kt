package it.neckar.commons.kotlin.js


/**
 * Avoids circular references in JSON.stringify.
 *
 * Typical error message: "Converting circular structure to JSON"
 */
fun safeStringify(obj: Any): String {
  val cache = mutableSetOf<Any?>()

  val json = JSON.stringify(obj) { key, value ->
    val type = jsTypeOf(value)

    if (type == "object" && value != null) {
      if (cache.contains(value)) {
        return@stringify undefined //duplicate found
      }

      cache.add(value) //store value in cache
    }

    value
  }
  return json
}
