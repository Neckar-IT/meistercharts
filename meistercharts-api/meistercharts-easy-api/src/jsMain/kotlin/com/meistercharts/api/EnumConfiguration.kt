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
package com.meistercharts.api

/**
 * Enumeration configuration for an enumeration
 */
@JsExport
actual external interface EnumConfiguration {
  /**
   * A string description of the enum configuration
   * "Name" of the enum
   */
  actual val description: String

  /**
   * The possible values for this enum configuration.
   * At least one value is required, at most 29 values are supported.
   *
   * Each values must have a different ordinal
   */
  actual val values: Array<EnumValue>
}

/**
 * Represents one possible value for a enum
 */
@JsExport
actual external interface EnumValue {
  /**
   * The position of this enum-value within the corresponding enum.
   * Values from 0..28 (inclusive) are allowed.
   */
  actual val ordinal: Int

  /**
   * The label of the enum-value (user visible)
   */
  actual val label: String
}
