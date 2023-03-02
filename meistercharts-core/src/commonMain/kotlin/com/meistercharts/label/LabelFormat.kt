package com.meistercharts.label


import com.meistercharts.annotations.Domain

/**
 * The value format that is used to format
 */
@Deprecated("No longer used")
fun interface LabelFormat {
  /**
   * Formats the domain value
   */
  fun format(@Domain domainValue: Double): String
}
