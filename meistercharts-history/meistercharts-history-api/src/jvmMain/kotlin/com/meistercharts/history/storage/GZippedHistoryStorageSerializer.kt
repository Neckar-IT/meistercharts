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
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Wraps a storage serializer and applies a gzip stream
 */
class GZippedHistoryStorageSerializer(
  val delegate: HistoryStorageSerializer
) : HistoryStorageSerializer {
  override fun serialize(bucket: HistoryBucket, out: OutputStream) {
    GzipCompressorOutputStream(out).use {
      delegate.serialize(bucket, it)
    }
  }

  override fun deserialize(input: InputStream): HistoryBucket {
    return GzipCompressorInputStream(input).use {
      delegate.deserialize(it)
    }
  }
}
