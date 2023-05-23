package it.neckar.unit.conversion

/**
 * A converter between two units
 */
interface Converter {
  /**
   * Converts a value
   */
  fun convertValue(value: Double): Double

  /**
   * Reverses a converted value to the original value
   */
  fun reverseValue(convertedValue: Double): Double

  companion object {
    /**
     * Creates a new converter for that factor
     */
    fun withFactor(factor: Double): Converter {
      return FactorConverter(factor)
    }

    /**
     * Noop converter that does not do anything
     */
    val noop: Converter = object : Converter {
      override fun convertValue(value: Double): Double {
        return value
      }

      override fun reverseValue(convertedValue: Double): Double {
        return convertedValue
      }
    }
  }
}
