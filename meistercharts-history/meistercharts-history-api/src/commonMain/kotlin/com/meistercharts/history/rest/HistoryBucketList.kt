package com.meistercharts.history.rest

import com.meistercharts.history.impl.io.SerializableHistoryBucket
import kotlinx.serialization.Serializable

/**
 * Wrapper for a list of history buckets
 */
@Serializable
data class HistoryBucketList(val buckets: List<SerializableHistoryBucket>)
