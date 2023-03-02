package com.meistercharts.algorithms.painter

/**
 * Represents a list of path actions that can be painted on the rendering context
 */
interface PathActions {
  /**
   * Returns the path actions
   */
  val actions: List<PathAction>
}
