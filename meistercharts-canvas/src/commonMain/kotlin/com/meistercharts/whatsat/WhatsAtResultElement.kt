package com.meistercharts.whatsat

import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle

/**
 * Represents one elements for a [WhatsAtResult].
 */
data class WhatsAtResultElement<T>(
  /**
   * The type for the result.
   */
  val type: ResultElementType<T>,

  /**
   * The exact location of the element - *not* the required location
   */
  val location: @Window Coordinates? = null,

  /**
   * The optional bounding box of the element (if there is any)
   */
  val boundingBox: @Window Rectangle? = null,

  /**
   * The (optional) label
   */
  val label: String? = null,
  /**
   * The (optional) value
   */
  val value: Number? = null,
  /**
   * The (optional) formatted value
   */
  val valueFormatted: String? = null,

  /**
   * Additional (more specific) data
   */
  val data: T,
)
