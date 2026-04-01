/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
