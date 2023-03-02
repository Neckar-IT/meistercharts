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
