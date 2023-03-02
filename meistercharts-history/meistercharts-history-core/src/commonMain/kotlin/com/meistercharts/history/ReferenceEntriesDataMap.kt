package com.meistercharts.history

import it.neckar.open.collections.cache
import it.neckar.open.i18n.TextKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Resolves [com.meistercharts.history.ReferenceEntryId]s to the objects.
 *
 * Contains *all* entries for one data series (and one history bucket?)
 */
@Serializable
sealed interface ReferenceEntriesDataMap {
  /**
   * Returns the reference data for the provided id
   */
  fun get(id: ReferenceEntryId): ReferenceEntryData?

  companion object {
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
  object Empty : ReferenceEntriesDataMap {
    override fun get(id: ReferenceEntryId): ReferenceEntryData? {
      return null
    }
  }

  /**
   * Automatically generates the entries
   */
  @Serializable
  @SerialName("Generated")
  object Generated : ReferenceEntriesDataMap {
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
  private val entries: Map<ReferenceEntryId, ReferenceEntryData>,
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
     * Stores the data entry
     */
    fun store(data: ReferenceEntryData) {
      this.entries[data.id] = data
    }

    /**
     * Builds the instance
     */
    fun build(): DefaultReferenceEntriesDataMap {
      return DefaultReferenceEntriesDataMap(entries.toMap())
    }
  }
}
