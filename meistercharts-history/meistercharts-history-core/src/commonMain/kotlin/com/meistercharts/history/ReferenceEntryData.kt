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

import it.neckar.open.annotations.TestOnly
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms
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
   * The start timestamp of this entry - is null if unknown
   */
  val start: @ms @Inclusive Double? = null,

  /**
   * The end timestamp of this entry - is null if unknown
   */
  val end: @ms @Inclusive Double? = null,

  /**
   * The (optional) payload that can be parsed later.
   *
   */
  val payload: UnparsedJson? = null,
) {

  companion object {
    /**
     * Creates a minimal data objet
     */
    @TestOnly
    fun create(id: @ReferenceEntryIdInt Int, label: String): ReferenceEntryData {
      return ReferenceEntryData(ReferenceEntryId(id), TextKey.simple(label))
    }
  }
}

/**
 * Represents unparsed json
 */
@Serializable
@JvmInline
value class UnparsedJson(val value: String) {

}
