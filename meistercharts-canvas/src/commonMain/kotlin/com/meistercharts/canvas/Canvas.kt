package com.meistercharts.canvas

import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.components.NativeComponentsSupport
import com.meistercharts.model.Size
import it.neckar.open.dispose.Disposable
import com.meistercharts.events.KeyEventBroker
import com.meistercharts.events.MouseEventBroker
import com.meistercharts.events.PointerEventBroker
import com.meistercharts.events.TouchEventBroker
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import it.neckar.open.unit.other.px

/**
 * Represents a canvas that can be painted.
 * Platform independent interface
 *
 */
interface Canvas : Disposable {
  /**
   * The type of the canvas.
   */
  val type: CanvasType

  /**
   * The graphics context that is used for the paint method.
   * The graphics context depends on the platform (HTML5 canvas or JavaFX Canvas)
   */
  val gc: CanvasRenderingContext

  /**
   * The current size of the canvas
   * Represents the *logical* size of the the canvas - includes the device pixel ratio.
   */
  @px
  val sizeProperty: ReadOnlyObservableObject<Size>

  /**
   * The size of the canvas.
   * Represents the *logical* size of the the canvas - includes the device pixel ratio.
   */
  val size: @px Size

  /**
   * The chart-size classification of this canvas
   */
  val chartSizeClassificationProperty: ReadOnlyObservableObject<ChartSizeClassification>

  /**
   * Returns the chart-size classification for this canvas
   */
  val chartSizeClassification: ChartSizeClassification

  /**
   * Returns the current (logical) width of the canvas
   */
  val width: @px Double
    get() = size.width

  /**
   * Returns the current (logical) height of the canvas
   */
  val height: @px Double
    get() = size.height

  /**
   * The *physical* width of the canvas
   */
  val physicalWidth: @px @PhysicalPixel Double

  /**
   * The *physical* height of the canvas
   */
  val physicalHeight: @px @PhysicalPixel Double

  /**
   * The mouse cursor property
   */
  val mouseCursor: ObservableObject<MouseCursor>

  /**
   * Provides access to native components that will be layouted
   * above (z axis) the canvas.
   */
  val nativeComponentsSupport: NativeComponentsSupport

  /**
   * The tool tip
   */
  val tooltip: ObservableObject<String?>

  /**
   * Handles the mouse events
   */
  val mouseEvents: MouseEventBroker

  /**
   * Handles the key events
   */
  val keyEvents: KeyEventBroker

  /**
   * Handles pointer events
   */
  val pointerEvents: PointerEventBroker

  /**
   * Handles touch events
   */
  val touchEvents: TouchEventBroker

  /**
   * Converts this canvas to an image that can be drawn on a [CanvasRenderingContext]
   *
   * ATTENTION: The returned image may or may not reflect changes to the canvas itself.
   * It is only possible to create one image at a time.
   */
  fun takeSnapshot(): Image
}
