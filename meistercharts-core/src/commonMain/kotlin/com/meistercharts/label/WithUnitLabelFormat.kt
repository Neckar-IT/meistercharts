package com.meistercharts.label

/**
 * Appends a unit to the value
 *
 */
@Deprecated("No longer used")
class WithUnitLabelFormat(
  val delegate: LabelFormat,
  val unit: String
) : LabelFormat by delegate {

  override fun format(domainValue: Double): String {
    return delegate.format(domainValue) + " " + unit
  }
}
