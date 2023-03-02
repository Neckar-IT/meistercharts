package com.meistercharts.algorithms.layers

/**
 * Identifies the type of layer.
 *
 * The type of layer is used to order the different layers
 */
enum class LayerType {
  /**
   * For layers that only execute calculations. These are executed at first (even before the background layers) and should
   * never paint anything or receive any events
   */
  Calculations,

  /**
   * Background layers that always have to be in the back
   */
  Background,

  /**
   * The "default" layer category - above the background and below the notification layers
   */
  Content,

  /**
   * Notification layers are painted above the "default" layers
   */
  Notification;


  /**
   * Returns true if this type is below the given type
   */
  fun below(type: LayerType): Boolean {
    return this.ordinal < type.ordinal
  }

  /**
   * Returns true if this type is the same as or below the given type
   */
  fun sameOrBelow(type: LayerType): Boolean {
    return this.ordinal <= type.ordinal
  }
}
