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
package com.meistercharts.fx

import it.neckar.open.javafx.properties.getValue
import it.neckar.open.javafx.properties.setValue
import it.neckar.open.unit.other.px
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Color

/**
 * Holds a chart canvas and resizes the canvas automatically
 *
 */
open class CanvasHolder(
  val canvas: Canvas,
  sizeBindingType: SizeBindingType = SizeBindingType.BOTH
) : Pane() {

  /**
   * Contains the insets of the canvas.
   * Can be used to avoid flickering (negative insets)
   */
  val canvasInsetsProperty: ObjectProperty<Insets> = SimpleObjectProperty(Insets.EMPTY)
  var canvasInsets: Insets by canvasInsetsProperty

  init {
    if (sizeBindingType.isBindWidth) {
      canvas.widthProperty().bind(Bindings.createDoubleBinding({ widthProperty().get() - canvasInsets.left - canvasInsets.right }, widthProperty(), canvasInsetsProperty))
    }

    if (sizeBindingType.isBindHeight) {
      canvas.heightProperty().bind(Bindings.createDoubleBinding({ heightProperty().get() - canvasInsets.top - canvasInsets.bottom }, heightProperty(), canvasInsetsProperty))
    }

    children.add(canvas)

    minHeight = 20.0
    minWidth = 20.0

    //request a new layout if the insets have changed
    canvasInsetsProperty.addListener { _, _, _ -> requestLayout() }

    //Set the background of the canvas holder to white
    background = Background(BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))
  }

  override fun layoutChildren() {
    //Layout the canvas in the middle to distribute the (optional) overscan on both sides
    assert(children.size == 1)

    val canvas = children[0] as Canvas

    @px val canvasWidthWithInsets = canvas.width - canvasInsets.left - canvasInsets.right
    @px val canvasHeightWithInsets = canvas.height - canvasInsets.top - canvasInsets.bottom

    //The resize is ignored because the canvas is not resizable
    canvas.resizeRelocate(0 + canvasInsets.left, 0 + canvasInsets.top, canvasWidthWithInsets, canvasHeightWithInsets)
  }

  /**
   * Removes the background
   */
  fun removeBackground() {
    background = null
  }

  public override fun setWidth(value: Double) {
    super.setWidth(value)
  }

  public override fun setHeight(value: Double) {
    super.setHeight(value)
  }
}

/**
 * The type of the size binding of the canvas to the canvas holder
 */
enum class SizeBindingType(
  val isBindWidth: Boolean,
  val isBindHeight: Boolean
) {
  BOTH(true, true),
  ONLY_WIDTH(true, false),
  ONLY_HEIGHT(false, true),
  NONE(false, false)
}
