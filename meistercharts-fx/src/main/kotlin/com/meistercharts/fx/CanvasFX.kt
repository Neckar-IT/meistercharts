package com.meistercharts.fx

import com.meistercharts.algorithms.environment
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.CanvasType
import com.meistercharts.canvas.ChartSizeClassification
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.Image
import com.meistercharts.model.Size
import it.neckar.open.time.nanos2millis
import it.neckar.open.time.nowMillis
import it.neckar.open.javafx.consume
import com.meistercharts.events.EventConsumption
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import it.neckar.open.unit.other.Relative
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ns
import javafx.animation.AnimationTimer
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.TouchEvent
import javafx.scene.paint.Color

/**
 * Contains a JavaFX canvas
 */
class CanvasFX(
  val canvas: Canvas = Canvas(),
  type: CanvasType
) : com.meistercharts.canvas.AbstractCanvas(type) {
  override val gc: CanvasRenderingContext = CanvasRenderingContextFX(this)

  @px
  override val sizeProperty: ReadOnlyObservableObject<Size> = ObservableObject(Size.zero)

  override val size: Size by sizeProperty

  override val physicalWidth: @PhysicalPixel Double
    get() = canvas.width

  override val physicalHeight: @PhysicalPixel Double
    get() = canvas.height

  override val chartSizeClassificationProperty: ReadOnlyObservableObject<ChartSizeClassification> = sizeProperty.map {
    ChartSizeClassification.get(it)
  }

  override val chartSizeClassification: ChartSizeClassification by chartSizeClassificationProperty

  init {
    canvas.apply {
      //This is required to ensure the keyboard events are published
      isFocusTraversable = true

      //Request the focus on mouse pressed
      addEventHandler(MouseEvent.MOUSE_PRESSED) {
        requestFocus()
      }
    }

    chartSizeClassificationProperty.map { }

    canvas.widthProperty().consume {
      recalculateCanvasRenderingSize()
    }
    canvas.heightProperty().consume {
      recalculateCanvasRenderingSize()
    }

    if (type == CanvasType.Main) {
      //Update the mouse cursor
      mouseCursor.consumeImmediately {
        canvas.cursor = it.toJavaFx()
      }

      setUpMouseEventHandler()
      setupTouchEventHandler()
      setUpKeyEventHandler()
    }
  }

  override fun dispose() {
    //reset the canvas size - this *might* improve performance and/or garbage collection - but there is no proof for it at the moment
    canvas.height = 0.0
    canvas.width = 0.0
  }

  /**
   * Updates the width/height of the *rendering* size of the canvas
   */
  internal fun recalculateCanvasRenderingSize() {
    // Set the width and height attributes to target rendering size
    val devicePixelRatio = environment.devicePixelRatio

    (sizeProperty as ObservableObject).value = Size.of(canvas.widthProperty().get() / devicePixelRatio, canvas.heightProperty().get() / devicePixelRatio)
  }


  private fun setUpMouseEventHandler() {
    requireMainCanvas()

    canvas.addEventHandler(MouseEvent.MOUSE_CLICKED) { mouseEvent ->
      //Skip synthesized events (from a touch display)
      if (mouseEvent.isSynthesized) {
        return@addEventHandler
      }

      if (mouseEvent.clickCount == 2) {
        notifyMouseDoubleClickHandlers(mouseEvent)
      } else {
        notifyMouseClickHandlers(mouseEvent)
      }
    }

    canvas.addEventHandler(MouseEvent.MOUSE_MOVED) { mouseEvent ->
      //Skip synthesized events (from a touch display)
      if (mouseEvent.isSynthesized) {
        return@addEventHandler
      }
      notifyMouseMovedHandlers(mouseEvent)
    }

    canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED) { mouseEvent ->
      //Skip synthesized events (from a touch display)
      if (mouseEvent.isSynthesized) {
        //ATTENTION isSynthesized seems to be always false for MOUSE_DRAGGED
        return@addEventHandler
      }
      notifyMouseDraggedHandlers(mouseEvent)
    }

    canvas.addEventHandler(MouseEvent.MOUSE_ENTERED) { mouseEvent ->
      //Skip synthesized events (from a touch display)
      if (mouseEvent.isSynthesized) {
        return@addEventHandler
      }
      notifyMouseMovedHandlers(mouseEvent)
    }

    canvas.addEventHandler(MouseEvent.MOUSE_EXITED) { mouseEvent ->
      //Skip synthesized events (from a touch display)
      if (mouseEvent.isSynthesized) {
        return@addEventHandler
      }
      notifyMouseExitedHandlers(mouseEvent)
    }

    canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) { mouseEvent ->
      //Skip synthesized events (from a touch display)
      if (mouseEvent.isSynthesized) {
        return@addEventHandler
      }
      notifyMousePressedHandlers(mouseEvent)
    }

    canvas.addEventHandler(MouseEvent.MOUSE_RELEASED) { mouseEvent ->
      //Skip synthesized events (from a touch display)
      if (mouseEvent.isSynthesized) {
        return@addEventHandler
      }
      notifyMouseReleasedHandlers(mouseEvent)
    }

    canvas.addEventHandler(ScrollEvent.SCROLL) { scrollEvent ->
      //Skip synthesized events (from a touch display)

      //we do *not* want to delegate synthetic scroll events from touch screens
      if (scrollEvent.isDirect) {
        return@addEventHandler
      }

      notifyMouseWheelHandlers(scrollEvent)
    }
  }

  private fun setupTouchEventHandler() {
    requireMainCanvas()

    canvas.addEventHandler(TouchEvent.TOUCH_PRESSED) { touchEvent ->
      notifyTouchPressedHandlers(touchEvent)
      touchEvent.consume()
    }

    canvas.addEventHandler(TouchEvent.TOUCH_MOVED) { touchEvent ->
      notifyTouchMovedHandlers(touchEvent)
      touchEvent.consume()
    }

    canvas.addEventHandler(TouchEvent.TOUCH_RELEASED) { touchEvent ->
      notifyTouchReleasedHandlers(touchEvent)
      touchEvent.consume()
    }

    canvas.addEventHandler(TouchEvent.TOUCH_STATIONARY) { touchEvent ->
      //Ignore!
      touchEvent.consume()
    }
  }

  private fun setUpKeyEventHandler() {
    requireMainCanvas()

    canvas.addEventHandler(KeyEvent.KEY_PRESSED) { keyEvent ->
      notifyKeyPressedHandlers(keyEvent)
    }

    canvas.addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
      notifyKeyReleasedHandlers(keyEvent)
    }

    canvas.addEventHandler(KeyEvent.KEY_TYPED) { keyEvent ->
      notifyKeyTypedHandlers(keyEvent)
    }
  }

  private fun notifyKeyPressedHandlers(event: KeyEvent) {
    requireMainCanvas()

    event.convertDown().let {
      keyEvents.notifyDown(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyKeyReleasedHandlers(event: KeyEvent) {
    event.convertUp().let {
      keyEvents.notifyUp(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyKeyTypedHandlers(event: KeyEvent) {
    event.convertType().let {
      keyEvents.notifyTyped(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseExitedHandlers(event: MouseEvent) {
    //Mouse exited is the same as mouse move with coordinates of null
    event.convertExit().let {
      mouseEvents.notifyMove(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseWheelHandlers(event: ScrollEvent) {
    //Mouse exited is the same as mouse move with coordinates of null
    event.convertWheel().let {
      mouseEvents.notifyWheel(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMousePressedHandlers(event: MouseEvent) {
    //Mouse exited is the same as mouse move with coordinates of null
    event.convertDown().let {
      mouseEvents.notifyDown(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseReleasedHandlers(event: MouseEvent) {
    //Mouse exited is the same as mouse move with coordinates of null
    event.convertUp().let {
      mouseEvents.notifyUp(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseMovedHandlers(event: MouseEvent) {
    event.convertMove().let {
      mouseEvents.notifyMove(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseDraggedHandlers(event: MouseEvent) {
    event.convertDrag().let {
      mouseEvents.notifyDrag(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseClickHandlers(event: MouseEvent) {
    event.convertClick().let {
      mouseEvents.notifyClick(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseDoubleClickHandlers(event: MouseEvent) {
    event.convertDoubleClick().let {
      mouseEvents.notifyDoubleClick(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyTouchPressedHandlers(event: TouchEvent) {
    //Touch exited is the same as touch move with coordinates of null
    event.convertStart(canvas).let {
      touchEvents.notifyOnStart(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyTouchMovedHandlers(event: TouchEvent) {
    //Touch exited is the same as touch move with coordinates of null
    event.convertMove(canvas).let {
      touchEvents.notifyOnMove(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyTouchReleasedHandlers(event: TouchEvent) {
    //Touch exited is the same as touch move with coordinates of null
    event.convertEnd(canvas).let {
      touchEvents.notifyOnEnd(it)
        .cancelIfConsumed(event)
    }
  }

  /**
   * The snapshot image that is filled and returned by [takeSnapshot].
   *
   * Attention: The image is recreated if the canvas size has changed
   */
  private var snapshotImage: WritableImage? = null
    get() {
      @PhysicalPixel val currentCanvasWidth = physicalWidth
      @PhysicalPixel val currentCanvasHeight = physicalHeight

      require(currentCanvasWidth > 0.0) {
        "Canvas width is 0.0"
      }
      require(currentCanvasHeight > 0.0) {
        "Canvas height is 0.0"
      }

      val currentImage = field
      if (currentImage != null && currentImage.width == currentCanvasWidth && currentImage.height == currentCanvasHeight) {
        return currentImage
      }

      val newImage = WritableImage(currentCanvasWidth.toInt(), currentCanvasHeight.toInt())
      field = newImage

      return newImage
    }

  override fun takeSnapshot(): Image {
    requireOffScreenCanvas()
    val snapshot = snapshot()
    return Image(snapshot, snapshot.size)
  }

  /**
   * Renders the content of this canvas into a writable image
   */
  fun snapshot(): WritableImage {
    val snapshotParameters = SnapshotParameters().apply { fill = Color.TRANSPARENT }
    return canvas.snapshot(snapshotParameters, snapshotImage)
  }
}

/**
 * Starts a timer that paints this canvas for every frame if necessary.
 */
fun ChartSupport.scheduleRepaints() {
  //Start the animation timer that calls refresh
  val animationTimer = object : AnimationTimer() {
    override fun handle(relativeNowInNanos: @ns @Relative Long) {
      //We try to calculate the absolute time stamp as exactly as possible
      if (deltaRelativeMillisToAbsolute == 0.0) {
        val now = nowMillis()
        deltaRelativeMillisToAbsolute = now - relativeNowInNanos.nanos2millis()
      }

      val exactAbsoluteTimestamp = relativeNowInNanos.nanos2millis() + deltaRelativeMillisToAbsolute
      refresh(exactAbsoluteTimestamp)
    }
  }

  animationTimer.start()

  onDispose {
    animationTimer.stop()
  }
}

/**
 * Cancels the event if this == [EventConsumption.Consumed]
 */
private fun EventConsumption.cancelIfConsumed(event: javafx.event.Event) {
  if (consumed) {
    event.consume()
  }
}


/**
 * Stores the delta between the relative millis to absolute millis
 */
private var deltaRelativeMillisToAbsolute: Double = 0.0
