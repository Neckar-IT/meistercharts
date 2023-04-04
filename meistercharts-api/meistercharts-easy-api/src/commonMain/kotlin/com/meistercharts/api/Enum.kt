package com.meistercharts.api

/**
 * Enumeration configuration for an enumeration
 */
expect interface EnumConfiguration {
  /**
   * A string description of the enum configuration
   * "Name" of the enum
   */
  val description: String

  /**
   * The possible values for this enum configuration.
   * At least one value is required, at most 29 values are supported.
   *
   * Each values must have a different ordinal
   */
  val values: Array<EnumValue>
}

/**
 * Represents one possible value for a enum
 */
expect interface EnumValue {
  /**
   * The position of this enum-value within the corresponding enum.
   * Values from 0..28 (inclusive) are allowed.
   */
  val ordinal: Int

  /**
   * The label of the enum-value (user visible)
   */
  val label: String
}
