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
