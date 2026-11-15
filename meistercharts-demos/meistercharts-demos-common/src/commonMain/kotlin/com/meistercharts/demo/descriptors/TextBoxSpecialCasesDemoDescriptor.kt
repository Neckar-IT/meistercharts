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
package com.meistercharts.demo.descriptors


import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.descriptors.TextBoxCase.Companion.LineHeight
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import kotlin.uuid.Uuid

/**
 */
class TextBoxSpecialCasesDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("fbb82a2f-6ae1-487f-9283-218dfb8c9b90")
  override val name: String = "Text Box - Special cases"
  override val description: String = "Shows special cases of text boxes."
  override val category: DemoCategory = DemoCategory.Text
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable
  /**
   * Reasoning for "?" Fallback: TextBoxes should always be filled, because otherwise they would not be visible. A empty TextBoxes serves no purpose as it is not used for spacing or navigation in meistercharts.
   * As soon as a TextBox is not empty, there might be a reason for empty/blank lines (eg. heading, paragraph in TextBox)
   */
  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content


            val leftLabelAnchorDirection = Direction.TopRight
            val textBoxAnchorDirection = Direction.TopLeft
            val expectedAnchorDirection = Direction.TopLeft

            // x offset at which the "expected" column starts (relative to the actual box anchor at x=0)
            val expectedColumnOffset = 200.0

            // line spacing used when the "expected" column lists more than one line
            val expectedLineHeight = 20.0

            // Horizontal spacing between column origins
            val columnWidth = 520.0
            // Top offset of the first row in each column
            val initialY = 20.0

            // Test cases shown by this demo. Built once — the lambdas capture the
            // layer's anchor / box-style fields, so this must run after those fields
            // are initialized (which is the case for object literals).
            val cases: List<TextBoxCase> = buildCases()

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translateToCenterX()

              // Each column may use the full canvas height (minus the header gap).
              val columnHeightLimit = gc.height - initialY

              val columnCount = countColumns(columnHeightLimit)
              val firstColumnX = -(columnCount - 1) * columnWidth / 2.0

              // "Should show:" header above each column
              for (c in 0 until columnCount) {
                gc.fillText("Should show:", firstColumnX + c * columnWidth + expectedColumnOffset, 0.0, expectedAnchorDirection, 0.0)
              }

              // Greedy column distribution: fill a column top-down until adding
              // the next case would exceed [columnHeightLimit], then advance.
              var column = 0
              var y = initialY
              cases.fastForEach { case ->
                if (y + case.rowHeight > columnHeightLimit && y > initialY) {
                  column++
                  y = initialY
                }
                val x = firstColumnX + column * columnWidth

                gc.fillText(case.label, x, y, leftLabelAnchorDirection, 10.0)
                gc.saved {
                  gc.translate(x, y)
                  case.paint(gc)
                }
                case.expected.fastForEachIndexed { lineIdx, expectedText ->
                  if (expectedText.isNotEmpty()) {
                    gc.fillText(expectedText, x + expectedColumnOffset, y + lineIdx * expectedLineHeight, expectedAnchorDirection, 0.0)
                  }
                }

                y += case.rowHeight
              }
            }

            /**
             * Pre-computes how many columns the [cases] need so the block can be centered.
             * Mirrors the layout loop in [paint] exactly; keep both in sync.
             */
            private fun countColumns(columnHeightLimit: Double): Int {
              var column = 0
              var y = initialY
              cases.fastForEach { case ->
                if (y + case.rowHeight > columnHeightLimit && y > initialY) {
                  column++
                  y = initialY
                }
                y += case.rowHeight
              }
              return column + 1
            }

            private fun buildCases(): List<TextBoxCase> {
              // Helpers — every textbox uses the same anchor and box style.
              fun case(label: String, content: String, expected: List<String>): TextBoxCase =
                TextBoxCase(label, { it.paintTextBox(content, textBoxAnchorDirection, boxStyle = BoxStyle.gray) }, expected)

              fun case(label: String, content: List<String>, expected: List<String>): TextBoxCase =
                TextBoxCase(label, { it.paintTextBox(content, textBoxAnchorDirection, boxStyle = BoxStyle.gray) }, expected)

              return listOf(
                // Single-string overloads
                case("Empty string:", "", listOf("?")),
                case("Blank string:", " ", listOf("?")),

                // Single-element list
                case("Single element, empty:", listOf(""), listOf("?")),
                case("Single element, blank:", listOf(" "), listOf("?")),
                case("Single element, text:", listOf("only line"), listOf("only line")),

                // Empty list
                case("empty lines list:", listOf(), listOf("?")),

                // All-empty / all-blank / all-text
                case("3 empty lines:", listOf("", "", ""), listOf("?", "?", "?")),
                case("3 blank lines:", listOf(" ", " ", " "), listOf("?", "?", "?")),
                case("3 lines:", listOf("first line", "second line", "third line"), listOf("first line", "second line", "third line")),

                // Empty/blank in the middle
                case("3 lines, empty in the middle:", listOf("first line", "", "third line"), listOf("first line", "", "third line")),
                case("3 lines, blank in the middle:", listOf("first line", "  ", "third line"), listOf("first line", "", "third line")),

                // Empty/blank at start
                case("3 lines, empty at start:", listOf("", "second line", "third line"), listOf("", "second line", "third line")),
                case("3 lines, blank at start:", listOf(" ", "second line", "third line"), listOf("", "second line", "third line")),

                // Empty/blank at end
                case("3 lines, empty at end:", listOf("first line", "second line", ""), listOf("first line", "second line", "")),
                case("3 lines, blank at end:", listOf("first line", "second line", " "), listOf("first line", "second line", "")),

                // Two empties / two blanks following text
                case("3 lines, 2 empty:", listOf("first line", "", ""), listOf("first line", "", "")),
                case("3 lines, 2 blanks:", listOf("first line", " ", " "), listOf("first line", "", "")),

                // Text only in the middle
                case("3 lines, empty top & bottom:", listOf("", "second line", ""), listOf("", "second line", "")),
                case("3 lines, blank top & bottom:", listOf(" ", "second line", " "), listOf("", "second line", "")),

                // Mixed empty + blank around middle text
                case("3 lines, empty/text/blank:", listOf("", "second line", " "), listOf("", "second line", "")),
                case("3 lines, blank/text/empty:", listOf(" ", "second line", ""), listOf("", "second line", "")),
              )
            }
          }
          layers.addLayer(layer)
        }
      }
    }
  }
}

/**
 * Single demo row: label, the actual `paintTextBox` invocation and the
 * expected output. [expected] is positional — its index is the line slot and
 * an empty string means nothing is shown at that slot. The number of slots
 * (i.e. [expected].size) determines how much vertical space the row occupies.
 */
private class TextBoxCase(
  val label: String,
  val paint: (CanvasRenderingContext) -> Unit,
  val expected: List<String>,
) {
  /** Vertical space allocated to this row — one [LineHeight] per expected slot. */
  val rowHeight: Double = LineHeight * expected.size.coerceAtLeast(1)

  companion object {
    private const val LineHeight = 40.0
  }
}

