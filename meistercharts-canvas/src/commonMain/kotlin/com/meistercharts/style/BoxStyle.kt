/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.style

import com.meistercharts.color.Color
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.color.get
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.Insets
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmStatic

/**
 * Defines the style of a box
 */
data class BoxStyle(
  /**
   * The fill for the box.
   * If set to null, this could mean:
   * - no fill is used at all
   * - the fill might be derived from the context (e.g., the current value).
   *
   * If you want to be sure that no fill is selected, use [Color.transparent]
   */
  var fill: ColorProviderNullable = { null },
  /**
   * The optional border color
   */
  var borderColor: ColorProviderNullable = { null },
  /**
   * The width of the border
   */
  @px
  var borderWidth: Double = 1.0,

  /**
   * The padding of the box: The distance between the border and the content of the box (e.g. the text)
   */
  var padding: Insets = Insets.of(5.0),

  /**
   * The (optional) rounded corner radii.
   * If set, a rounded rect will be painted.
   */
  var radii: BorderRadius? = null,

  /**
   * The optional shadow configuration.
   * The shadow is only applied for the [fill].
   *
   * Therefore, if [fill] is null, the shadow will not be visible
   */
  val shadow: Shadow? = null,
) {

  companion object {
    /**
     * No box (no fill, no padding, bo border)
     */
    @JvmStatic
    val none: BoxStyle = BoxStyle(padding = Insets.empty)

    /**
     * Box with a black background
     */
    @JvmStatic
    val black: BoxStyle = BoxStyle(fill = Color.black)

    /**
     * Box with a silver background and gray border
     */
    @JvmStatic
    val gray: BoxStyle = BoxStyle(fill = Color.silver, borderColor = Color.gray)

    /**
     * Box style for balloon tooltips
     */
    @JvmStatic
    val balloon: BoxStyle = BoxStyle(fill = Color.white, borderColor = Color.black, shadow = Shadow.Default, padding = Insets.all10, radii = BorderRadius.all5)

    /**
     * Box with a dark gray background, rounded borders and drop shadow.
     */
    @JvmStatic
    val modernGray: BoxStyle = BoxStyle(
      fill = Color.darkgray,
      radii = BorderRadius.all2,
      shadow = Shadow.LightDrop
    )

    /**
     * Box that is used
     */
    @JvmStatic
    val modernBlue: BoxStyle = BoxStyle(
      fill = Color.white,
      borderColor = Color.blue2,
      radii = BorderRadius.all2,
      shadow = Shadow.LightDrop
    )

    @JvmStatic
    val modernDarkBlue: BoxStyle = BoxStyle(
      fill = Color.white,
      borderColor = Color.blue3,
      radii = BorderRadius.all2,
      shadow = Shadow.LightDrop
    )
  }
}

/**
 * If [fillProvider] is not null, it will be used as the fill.
 * Returns a new instance in that case.
 * Just returns this if [fill] is already set.
 */
fun BoxStyle.withFillIfNull(fillProvider: ColorProviderNullable): BoxStyle {
  if (fill != null || fillProvider == null) {
    return this
  }

  return this.copy(fill = fillProvider)
}

fun BoxStyle.withBorderColorIfNull(borderColorProvider: ColorProviderNullable): BoxStyle {
  if (borderColor != null || borderColorProvider == null) {
    return this
  }

  return this.copy(borderColor = borderColorProvider)
}

/**
 * Returns this if a border color is already set.
 * Creates a new instance with a border-color computed by the given converter (which takes the current fill)
 *
 * Attention! Will overwrite the border - even if set to null purposefully
 */
fun BoxStyle.withBorderColorIfNullOld(
  /**
   * Returns the border color based on the fill
   */
  borderColorForFill: (fill: Color?) -> ColorProviderNullable,
): BoxStyle {
  if (borderColor != null) {
    return this
  }

  val newBorderColor = borderColorForFill(this.fill.get()) ?: return this
  return this.copy(borderColor = newBorderColor)
}
