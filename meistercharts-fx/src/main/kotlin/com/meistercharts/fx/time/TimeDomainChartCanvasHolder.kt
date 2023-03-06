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
package com.meistercharts.fx.time

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.fx.CanvasHolder
import com.meistercharts.fx.binding.toJavaFx
import it.neckar.open.time.nowMillis
import it.neckar.open.javafx.properties.*
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.dispose.alsoRegisterAt
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.ns
import com.meistercharts.fx.time.BaseTimeDomainChartCanvas
import javafx.animation.AnimationTimer
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import java.util.concurrent.Callable

/**
 * Holds an scrolls a [BaseTimeDomainChartCanvas] with time.
 *
 * ATTENTION: System.nanos() is *not* correct.
 */
class TimeDomainChartCanvasHolder
@JvmOverloads
constructor(
  /**
   * The canvas is wrapped within a stack pane to
   */
  val canvas: BaseTimeDomainChartCanvas,
  /**
   * The given time is used to calculate the insets for the right and left side.
   * The insets are used to calculate the insets for the canvas holder.
   */
  @ms val insetsTime: Double = 0.0
) : Pane(), Disposable {

  private val disposeSupport = DisposeSupport()

  private val animationTimer: TranslationAnimationTimer

  /**
   * If set to true, the animation (over time) is active
   */
  val animatedProperty: BooleanProperty = SimpleBooleanProperty()
  var animated: Boolean by animatedProperty


  init {
    //Disable snap x values when animated
    canvas.snapXValuesToPixelProperty().bind(animatedProperty.not())

    updateClip()

    //Add the canvas. Width/height are automatically bound
    val canvasHolder = CanvasHolder(canvas)

    //Bind the insets based upon the delay
    canvasHolder.canvasInsetsProperty.bind(Bindings.createObjectBinding(Callable {
      @Zoomed val delayZoomed = canvas.timeDuration2zoomed(insetsTime)
      Insets(0.0, -delayZoomed, 0.0, -delayZoomed)
    }, canvas.chartState.contentAreaSizeProperty.toJavaFx()))

    children.add(canvasHolder)

    animationTimer = TranslationAnimationTimer().alsoRegisterAt(disposeSupport)
    animatedProperty.addListener { _, _, newValue ->
      if (newValue!!) {
        animationTimer.start()
      } else {
        animationTimer.stop()
        canvas.translateX = 0.0
      }
    }

    if (DEBUG_ENABLED) {
      val rect = Rectangle()
      rect.fill = null
      rect.stroke = Color.ORANGE
      rect.widthProperty().bind(widthProperty().subtract(200))
      rect.heightProperty().bind(heightProperty())
      rect.x = 100.0
      children.add(rect)
    }
  }

  /**
   * Starts the animation
   */
  fun startAnimation() {
    animated = true
  }

  /**
   * Stops the animation
   */
  fun stopAnimation() {
    animated = false
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  override fun layoutChildren() {
    val node = children[0]
    node.resizeRelocate(0.0, 0.0, width, height)

    if (DEBUG_ENABLED) {
      val paddingX = 100
      node.resizeRelocate(paddingX.toDouble(), 0.0, width - paddingX * 2, height)
    }
  }

  /**
   * Ensures that only the bounds of the node are drawn
   */
  private fun updateClip() {
    val clipRect = Rectangle()
    clipRect.widthProperty().bind(widthProperty())
    clipRect.heightProperty().bind(heightProperty())
    clip = clipRect
  }

  /**
   * Animation timer that translates the canvas
   */
  private inner class TranslationAnimationTimer : AnimationTimer(), Disposable {
    override fun handle(@ns now: Long) {
      @ms val nowInMillis = nowMillis()

      //The value for the right side.
      @ms val to = canvas.timeRange.end

      //Calculate the relative position of now
      @px @Window val positionNow = canvas.time2window(nowInMillis)
      @px @Window val positionTo = canvas.time2window(to)
      @px @Zoomed val delta = positionTo - positionNow

      canvas.translateX = delta
    }

    override fun dispose() {
      stop()
    }
  }

  companion object {
    private const val DEBUG_ENABLED = false
  }
}
