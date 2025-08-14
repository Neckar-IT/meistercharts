package it.neckar.time

import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.s
import it.neckar.open.unit.time.h
import it.neckar.open.unit.time.min
import it.neckar.time.io.MillisAsIsoDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Wrapper for a double value representing milliseconds.
 */
@JvmInline
@Serializable(with = MillisAsIsoDateTimeSerializer::class)
value class Millis(val value: @ms Double) {
  val millis: @ms Double
    get() = value

  /**
   * Converts the millis to seconds
   */
  val seconds: @s Double
    get() = value / 1000.0

  /**
   * Converts the millis to minutes
   */
  val minutes: @min Double
    get() = value / (1000.0 * 60.0)

  /**
   * Converts the millis to hours
   */
  val hours: @h Double
    get() = value / (1000.0 * 60.0 * 60.0)

  override fun toString(): String {
    return "$value ms"
  }
}
