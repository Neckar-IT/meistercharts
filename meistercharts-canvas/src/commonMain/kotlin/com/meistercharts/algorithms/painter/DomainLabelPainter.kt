package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.label.DomainRelativeLabel
import com.meistercharts.label.LayoutedLabel
import com.meistercharts.label.LayoutedLabels
import it.neckar.open.collections.fastMap
import it.neckar.open.unit.other.px

/**
 * Shows labels for y values.
 * Only paints the labels for the visible values
 *
 */
@Deprecated("Do not use!! Convert directly within the layer")
class DomainLabelPainter(
  private val chartCalculator: ChartCalculator,
  snapXValues: Boolean,
  snapYValues: Boolean,
  /**
   * The height of the window
   */
  @px @Window val windowHeight: Double,
  val styleConfiguration: LabelPainter.Style.() -> Unit = {}
) : AbstractPainter(snapXValues, snapYValues) {

  /**
   * Paints the labels
   * Updates the layout information of the labels during the painting process
   */
  fun paint(gc: CanvasRenderingContext, @DomainRelative labels: List<DomainRelativeLabel>, placement: LabelPlacement): LayoutedLabels {
    val layoutedLabels = labels.toLayoutedLabel()
    val labelPainter = LabelPainter(0.0, windowHeight, isSnapXValues, isSnapYValues, styleConfiguration)
    labelPainter.paintLabels(gc, layoutedLabels, placement)
    return LayoutedLabels(layoutedLabels)
  }

  /**
   * Create a model with window coordinates
   */
  private fun List<DomainRelativeLabel>.toLayoutedLabel(): List<LayoutedLabel> {
    return fastMap {
      @DomainRelative val domainRelativeValue = it.value
      @Window val locationWindow = chartCalculator.domainRelative2windowY(domainRelativeValue)

      return@fastMap LayoutedLabel(it, locationWindow)
    }
  }
}

