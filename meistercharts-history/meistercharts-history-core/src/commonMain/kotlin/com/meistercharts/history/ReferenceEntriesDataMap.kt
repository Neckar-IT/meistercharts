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
package com.meistercharts.history

import it.neckar.open.collections.cache
import it.neckar.open.collections.fastForEach
import it.neckar.open.i18n.TextKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Resolves [com.meistercharts.history.ReferenceEntryId]s to the objects.
 *
 * Contains *all* entries for *all* data series. Usually within one [HistoryBucket].
 *
 */
@Serializable
sealed interface ReferenceEntriesDataMap {
  /**
   * Returns the reference data for the provided id
   */
  fun get(id: ReferenceEntryId): ReferenceEntryData?

  /**
   * Returns all entries - in a newly instantiated list
   */
  fun getAll(referenceEntryIds: @ReferenceEntryIdInt IntArray): Set<ReferenceEntryData> {
    return referenceEntryIds.map { idAsInt: @ReferenceEntryIdInt Int ->
      get(ReferenceEntryId(idAsInt))
    }.filterNotNull()
      .toSet()
  }

  companion object {
    /**
     * Creates a new map containing exactly the provided values
     */
    fun of(vararg data: ReferenceEntryData): ReferenceEntriesDataMap {
      return DefaultReferenceEntriesDataMap(data.associateBy { it.id })
    }

    /**
     * Does not contain any data - only useful for tests or as default
     */
    val empty: ReferenceEntriesDataMap = Empty

    /**
     * Generates a new [ReferenceEntryData] object for each id
     */
    val generated: ReferenceEntriesDataMap = Generated
  }

  /**
   * Empty - does not contain any data
   */
  @Serializable
  @SerialName("Empty")
  data object Empty : ReferenceEntriesDataMap {
    override fun get(id: ReferenceEntryId): ReferenceEntryData? {
      return null
    }
  }

  /**
   * Automatically generates the entries
   */
  @Serializable
  @SerialName("Generated")
  data object Generated : ReferenceEntriesDataMap {
    private val cache = cache<ReferenceEntryId, ReferenceEntryData>("ReferenceEntriesDataMap.generated", 100)

    override fun get(id: ReferenceEntryId): ReferenceEntryData {
      return cache.getOrStore(id) {
        ReferenceEntryData(id, TextKey.simple("Label $id"))
      }
    }
  }
}


/**
 * Default implementation that stores the values in a mutable map
 */
@Serializable
@SerialName("Default")
data class DefaultReferenceEntriesDataMap(
  /**
   * Contains the entries for each object value id
   */
  internal val entries: Map<ReferenceEntryId, ReferenceEntryData>,
) : ReferenceEntriesDataMap {

  override fun get(id: ReferenceEntryId): ReferenceEntryData? {
    return this.entries[id]
  }

  class Builder {
    /**
     * Contains the entries for each object value id
     */
    private val entries: MutableMap<ReferenceEntryId, ReferenceEntryData> = mutableMapOf()

    /**
     * Stores the data entry.
     * Overwrites existing entries for the same ID
     */
    fun store(data: ReferenceEntryData) {
      this.entries[data.id] = data
    }

    fun get(id: ReferenceEntryId): ReferenceEntryData? {
      return this.entries[id]
    }

    /**
     * Stores all elements
     */
    fun storeAll(dataList: List<ReferenceEntryData?>) {
      dataList.fastForEach { data ->
        if (data != null) {
          store(data)
        }
      }
    }

    fun storeAll(dataList: Set<ReferenceEntryData>) {
      dataList.forEach { data ->
        store(data)
      }
    }


    /**
     * Builds the instance
     */
    fun build(): DefaultReferenceEntriesDataMap {
      return DefaultReferenceEntriesDataMap(entries.toMap())
    }
  }
}

/**
 * Builds a list of map builders
 */
fun List<DefaultReferenceEntriesDataMap.Builder>.build(): List<ReferenceEntriesDataMap> {
  return map {
    it.build()
  }
}
