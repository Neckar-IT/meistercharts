package com.meistercharts.history

import it.neckar.open.unit.si.mm
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a unit
 */
@Serializable
@JvmInline
value class HistoryUnit(val name: String?) {

  companion object {
    /**
     * Empty unit
     */
    val None: HistoryUnit = HistoryUnit(null)

    /**
     * millimeter
     */
    val mm: @mm HistoryUnit = HistoryUnit("mm")
    val ml: @mm HistoryUnit = HistoryUnit("ml")
  }
}
