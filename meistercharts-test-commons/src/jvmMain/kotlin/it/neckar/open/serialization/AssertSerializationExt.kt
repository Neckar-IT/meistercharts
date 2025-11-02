package it.neckar.open.serialization

import assertk.*
import com.fasterxml.jackson.databind.JsonNode
import it.neckar.open.http.Url
import it.neckar.open.resources.getResourceSafe
import it.neckar.open.test.utils.JsonUtils
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 *
 */
fun Assert<kotlinx.serialization.json.JsonObject>.isJsonEqualTo(
  expectedJsonUrl: Url,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  isJsonEqualTo(javaClass.getResourceSafe(expectedJsonUrl).readText(), actualTreeModifier)
}

fun Assert<kotlinx.serialization.json.JsonObject>.isJsonEqualTo(
  expectedJsonString: String,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  given { current ->
    JsonUtils.assertJsonEquals(expectedJsonString, current.toStringPretty(), actualTreeModifier)
  }
}


/**
 * JSON instance with pretty print enabled
 */
private val JsonPretty: Json = Json {
  defaultJsonConfiguration(true)
}

/**
 * Pretty prints the JSON element
 */
private fun JsonElement.toStringPretty(): String {
  return JsonPretty.encodeToString(JsonElement.serializer(), this)
}

