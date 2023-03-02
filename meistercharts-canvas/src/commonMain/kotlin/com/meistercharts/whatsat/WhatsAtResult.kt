package com.meistercharts.whatsat

import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates

/**
 * Contains information about stuff that is currently at a given location
 */
data class WhatsAtResult(
  /**
   * The location for this result (from the request)
   */
  val location: @Window Coordinates,
  /**
   * The precision for this result (from the request)
   */
  val precision: WhatsAtSupport.Precision,

  /**
   * The elements
   */
  val elements: List<WhatsAtResultElement<*>>,
)
