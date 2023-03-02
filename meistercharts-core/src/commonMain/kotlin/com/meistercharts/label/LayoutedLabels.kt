package com.meistercharts.label

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.other.px

/**
 * Wrapper for layouted labels
 */
@Deprecated("No longer used")
data class LayoutedLabels(
  @Domain

  val labels: List<LayoutedLabel>
) {

  /**
   * Returns the label model at the given location
   */
  @Domain
  fun findLabel(@Window location: Coordinates): LayoutedLabel? {
    return labels
      .asSequence()
      .firstOrNull {
        it.bounds.contains(location)
      }
  }

  @Domain
  fun findLabel(@Window @px y: Double): LayoutedLabel? {
    return labels
      .asSequence()
      .firstOrNull {
        it.containsActual(y)
      }
  }

  /**
   * Creates a paint result with updated paint bounds that "reverts" the given translation
   */
  fun fixTranslation(tx: Double, ty: Double) {
    labels.fastForEach {
      it.moveBounds(tx, ty)
    }
  }
}
