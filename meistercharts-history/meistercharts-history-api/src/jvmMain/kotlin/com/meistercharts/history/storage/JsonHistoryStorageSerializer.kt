/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
