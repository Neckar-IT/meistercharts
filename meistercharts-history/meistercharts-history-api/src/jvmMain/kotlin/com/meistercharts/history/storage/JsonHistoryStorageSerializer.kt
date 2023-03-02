package com.meistercharts.history.storage

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.impl.io.SerializableHistoryBucket
import com.meistercharts.history.impl.io.toSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import javax.annotation.WillNotClose

/**
 * Storage serializer that uses json
 */
class JsonHistoryStorageSerializer(val jsonFormat: Json = createPrettyJsonFormat()) : HistoryStorageSerializer {

  /**
   * The serializer that creates a JSON string for a given bucket
   */
  val bucketSerializer: KSerializer<SerializableHistoryBucket> = SerializableHistoryBucket.serializer()

  /**
   * Serializes the given bucket to the given output stream
   */
  override fun serialize(bucket: HistoryBucket, @WillNotClose out: OutputStream) {
    val jsonString = jsonFormat.encodeToString(bucketSerializer, bucket.toSerializable())
    out.write(jsonString.toByteArray(charset))
  }

  /**
   * Deserializes a bucket
   */
  override fun deserialize(@WillNotClose input: InputStream): HistoryBucket {
    val json = input.readBytes().toString(charset)
    return jsonFormat.decodeFromString(bucketSerializer, json).toHistoryBucket()
  }

  companion object {
    val charset: Charset = Charsets.UTF_8
  }
}

/**
 * Returns the pretty json format
 */
fun createPrettyJsonFormat(): Json {
  return Json {
    serializersModule = SerializersModule {
    }

    prettyPrint = true
    prettyPrintIndent = "  "
  }
}
