package com.meistercharts.fx.painter.lane

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.label.DomainRelativeLabel
import com.meistercharts.label.LabelData
import it.neckar.open.unit.other.pct
import com.google.common.collect.ImmutableList

/**
 * Contains edges and positions for a given point in time
 */
class LanesInformation(
  val lanes: ImmutableList<out Lane>,
  val edges: ImmutableList<out Edge>,
  @Domain val valueRange: ValueRange
) {

  constructor(
    lanes: Collection<out Lane>,
    edges: Collection<out Edge>,
    @Domain
    valueRange: ValueRange
  ) : this(ImmutableList.copyOf(lanes), ImmutableList.copyOf(edges), valueRange)


  @Domain
  fun extractEdgeLabelInfos(): List<DomainRelativeLabel> {
    return edges
      .filter { it.label != null }
      .map {
        @Domain val relativeValue = valueRange.toDomainRelative(it.position)
        val label = requireNotNull(it.label)
        DomainRelativeLabel(relativeValue, LabelData(label, it.color))
      }
  }

  /**
   * Describes one edge
   */
  data class Edge(
    /**
     * The position of the edge
     */
    @Domain
    val position: Double,
    /**
     * The brightness below the edge
     */
    @pct
    val brightnessBelow: Double,
    /**
     * The brightness above the edge
     */
    @pct
    val brightnessAbove: Double,

    /**
     * The label for the edge.
     * If set to null, the label is not painted
     */
    val label: String?,

    /**
     * The color for the edge
     */
    val color: Color,
  )

  /**
   * Describes one lane
   */
  data class Lane(
    /**
     * The color of the lane
     */
    @pct val brightness: Double,
    /**
     * The start of the pane
     */
    @Domain val lower: Double,
    /**
     * The end of the lane
     */
    @Domain val upper: Double
  )

  companion object {
    val EMPTY: LanesInformation = LanesInformation(ImmutableList.of(), ImmutableList.of(), ValueRange.percentage)
  }
}
