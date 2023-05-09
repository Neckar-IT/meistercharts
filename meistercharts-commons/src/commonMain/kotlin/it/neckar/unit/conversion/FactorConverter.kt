package it.neckar.unit.conversion

/**
 * Converter using a factor
 */
class FactorConverter(val factor: Double) : Converter {
  override fun convertValue(value: Double): Double {
    return value * factor
  }

  override fun reverseValue(convertedValue: Double): Double {
    return convertedValue / factor
  }
}
