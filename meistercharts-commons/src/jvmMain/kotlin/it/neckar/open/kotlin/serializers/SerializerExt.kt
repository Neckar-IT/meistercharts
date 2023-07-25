package it.neckar.open.kotlin.serializers

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File


/**
 * Deserializes a JSON file.
 * Throws an improved exception including the file name if a [SerializationException] is thrown
 */
inline fun <reified T> Json.deserializeJsonFile(configurationFile: File): T {
  try {
    val fileContent = configurationFile.readText()
    return decodeFromString(fileContent)
  } catch (e: SerializationException) {
    throw SerializationException("Could not deserialize ${configurationFile.absolutePath}", e)
  }
}
