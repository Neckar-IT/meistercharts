package it.neckar.unit.conversion

import it.neckar.open.unit.other.CubicFoot
import it.neckar.open.unit.other.UsGallon
import it.neckar.open.unit.other.`in`
import it.neckar.open.unit.other.ft
import it.neckar.open.unit.other.ft.Companion.MM_FEET_RATIO
import it.neckar.open.unit.si.L
import it.neckar.open.unit.si.degC
import it.neckar.open.unit.si.degF
import it.neckar.open.unit.si.m
import it.neckar.open.unit.si.mm


/**
 * Converts from/to imperial units
 */
object ImperialConversion {
  /**
   * Converts celsius to fahrenheit
   */
  val celsius2FahrenheitConverter: Converter = object : Converter {
    override fun convertValue(value: @degC Double): @degF Double {
      return value * 1.8 + 32
    }

    override fun reverseValue(convertedValue: @degF Double): @degC Double {
      return (convertedValue - 32) / 1.8
    }
  }

  fun celsius2fahrenheit(celsius: @degC Double): @degF Double {
    return celsius2FahrenheitConverter.convertValue(celsius)
  }

  fun fahrenheit2celsius(fahrenheit: @degF Double): @degC Double {
    return celsius2FahrenheitConverter.reverseValue(fahrenheit)
  }


  val litre2UsGallonConverter: Converter = object : Converter {
    override fun convertValue(value: @L Double): @UsGallon Double {
      return value / UsGallon.US_GALLON_LITRE_RATIO
    }

    override fun reverseValue(convertedValue: @UsGallon Double): @L Double {
      return convertedValue * UsGallon.US_GALLON_LITRE_RATIO
    }
  }

  fun litre2usGallon(litre: Double): @UsGallon Double {
    return litre2UsGallonConverter.convertValue(litre)
  }

  fun usGallon2litre(usGallon: Double): @L Double {
    return litre2UsGallonConverter.reverseValue(usGallon)
  }


  val litre2cubicFootConverter: Converter = object : Converter {
    override fun convertValue(value: @L Double): @CubicFoot Double {
      return value / CubicFoot.LITRE_PER_CU_FOOT
    }

    override fun reverseValue(convertedValue: @CubicFoot Double): @L Double {
      return convertedValue * CubicFoot.LITRE_PER_CU_FOOT
    }
  }

  fun litre2cubicFoot(litre: @L Double): @CubicFoot Double {
    return litre2cubicFootConverter.convertValue(litre)
  }

  fun cubicFoot2litre(cubicFeet: @CubicFoot Double): @L Double {
    return litre2cubicFootConverter.reverseValue(cubicFeet)
  }


  val meter2footConverter: Converter = object : Converter {
    override fun convertValue(value: @m Double): @ft Double {
      return value * 1000.0 / MM_FEET_RATIO
    }

    override fun reverseValue(convertedValue: @ft Double): @m Double {
      return convertedValue / 1000.0 * MM_FEET_RATIO
    }
  }

  /**
   * Converts meter to foot
   */
  fun meter2foot(meter: @m Double): @ft Double {
    return meter2footConverter.convertValue(meter)
  }

  fun foot2meter(foot: @ft Double): @m Double {
    return meter2footConverter.reverseValue(foot)
  }


  val mm2inchConverter: Converter = object : Converter {
    override fun convertValue(value: @mm Double): @`in` Double {
      return value / `in`.MM_RATIO
    }

    override fun reverseValue(convertedValue: @`in` Double): @mm Double {
      return convertedValue * `in`.MM_RATIO
    }
  }

  /**
   * Converts mm to inch
   */
  fun mm2inch(mm: @mm Double): @`in` Double {
    return mm2inchConverter.convertValue(mm)
  }

  fun inch2mm(inch: @`in` Double): @mm Double {
    return mm2inchConverter.reverseValue(inch)
  }
}
