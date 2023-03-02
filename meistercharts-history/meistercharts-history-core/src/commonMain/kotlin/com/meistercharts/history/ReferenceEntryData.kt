package com.meistercharts.history

import it.neckar.open.i18n.TextKey
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Contains the values relevant for an [ReferenceEntryId].
 * Contains some well known elements - that could/should exist for each entry.
 * Also contains some kind of
 */
@Serializable
data class ReferenceEntryData(
  val id: ReferenceEntryId,
  /**
   * The key for the label
   */
  val label: TextKey,

  //Todo necessary? helpful?
  ///**
  // * The state for the object
  // */
  //val state: HistoryEnum.HistoryEnumValue,
  //
  //TODO add additional well know

  /**
   * The (optional) payload that can be parsed later.
   *
   */
  val payload: UnparsedJson? = null,
) {

}

/**
 * Represents unparsed json
 */
@Serializable
@JvmInline
value class UnparsedJson(val value: String) {

}
