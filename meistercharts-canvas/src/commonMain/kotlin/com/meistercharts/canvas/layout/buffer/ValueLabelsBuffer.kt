/*
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
package com.meistercharts.canvas.layout.buffer

import com.meistercharts.annotations.Window
import com.meistercharts.color.Color
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.number.MayBeNaN

/**
 * Buffers value labels (e.g. for the cross wire): the label location, the label text and its styling - without creating any objects.
 * All values are indexed by the same label index.
 *
 * After [prepare] the location is [Double.NaN] and the styles are the defaults - entries that are not [set] (skipped values) keep these defaults.
 *
 * Is *NOT* thread safe!
 */
class ValueLabelsBuffer(
  /**
   * The box style that is used for entries that have not been [set]
   */
  defaultBoxStyle: BoxStyle,
  /**
   * The text color that is used for entries that have not been [set]
   */
  defaultTextColor: Color,
) : LayoutVariableWithSize {
  /**
   * The y locations of the labels
   */
  private val locationsY: @Window @MayBeNaN DoubleMultiBuffer = DoubleMultiBuffer()

  /**
   * The label texts
   */
  private val labels: StringMultiBuffer = StringMultiBuffer()

  /**
   * The box styles for the labels
   */
  private val boxStyles: ObjectMultiBuffer<BoxStyle> = ObjectMultiBuffer(defaultBoxStyle)

  /**
   * The text colors for the labels
   */
  private val textColors: ObjectMultiBuffer<Color> = ObjectMultiBuffer(defaultTextColor)

  override val size: Int
    get() = locationsY.size

  override fun reset() {
    locationsY.reset()
    labels.reset()
    boxStyles.reset()
    textColors.reset()
  }

  override fun resize(size: Int) {
    locationsY.resize(size)
    labels.resize(size)
    boxStyles.resize(size)
    textColors.resize(size)
  }

  /**
   * Sets the label location, text and styling for the given index
   */
  fun set(index: Int, locationY: @Window Double, label: String, boxStyle: BoxStyle, textColor: Color) {
    locationsY[index] = locationY
    labels[index] = label
    boxStyles[index] = boxStyle
    textColors[index] = textColor
  }

  /**
   * Returns the y location of the label for the given index
   */
  fun locationYAt(index: Int): @Window @MayBeNaN Double {
    return locationsY[index]
  }

  /**
   * Returns the label text for the given index
   */
  fun labelAt(index: Int): String {
    return labels[index]
  }

  /**
   * Returns the box style for the given index
   */
  fun boxStyleAt(index: Int): BoxStyle {
    return boxStyles[index]
  }

  /**
   * Returns the text color for the given index
   */
  fun textColorAt(index: Int): Color {
    return textColors[index]
  }
}
