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
import java.io.InputStream
import java.io.OutputStream
import javax.annotation.WillNotClose

/**
 * Serializer that reads/writes files in the history storage.
 *
 * ATTENTION: This interface *must* only be used in the context of the history storage.
 * It is *not* meant to be used for other cases (e.g. REST).
 *
 */
interface HistoryStorageSerializer {
  /**
   * Serializes the given bucket to the given output stream
   */
  fun serialize(bucket: HistoryBucket, @WillNotClose out: OutputStream)

  /**
   * Deserializes a bucket
   */
  fun deserialize(@WillNotClose input: InputStream): HistoryBucket
}
