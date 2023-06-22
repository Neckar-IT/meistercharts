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
package com.meistercharts.canvas

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ChartState
import com.meistercharts.algorithms.GlobalCacheSupport
import com.meistercharts.algorithms.KeepCenterOnWindowResize
import com.meistercharts.algorithms.MutableChartState
import com.meistercharts.algorithms.UpdateReason
import com.meistercharts.algorithms.WindowResizeBehavior
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.ZoomAndTranslationSupport
import com.meistercharts.algorithms.ZoomLevelCalculator
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.environment
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.PaintingProperties
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.updateEnvironment
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.components.NativeComponentsSupport
import com.meistercharts.canvas.resize.ResizeHandlesSupport
import com.meistercharts.charts.ChartId
import com.meistercharts.events.KeyEventBroker
import com.meistercharts.events.MouseEventBroker
import com.meistercharts.events.PointerEventBroker
import com.meistercharts.events.TouchEventBroker
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.service.ServiceRegistry
import com.meistercharts.whatsat.WhatsAtSupport
import it.neckar.open.async.TimerSupport
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.dispose.OnDispose
import it.neckar.open.i18n.DefaultTextService
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.I18nSupport
import it.neckar.open.i18n.addFallbackTextResolver
import it.neckar.open.kotlin.lang.isNanOrInfinite
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Contains the relevant services and stuff related to the chart.
 *
 * For layer related stuff take a look at [LayerSupport]
 *
 * ## Lifecycle
 * The [ChartSupport] is instantiated once per chart in [com.meistercharts.canvas.MeisterChartBuilder.createChartSupport(com.meistercharts.canvas.Canvas)].
 * It is provided via [com.meistercharts.algorithms.layers.LayerPaintingContext] during layout and paint to each layer.
 *
 * In some cases it is necessary to get a reference outside of layout/painting.
 * An example implementation is done in [com.meistercharts.charts.AbstractChartGestalt].
 */
class ChartSupport constructor(
  /**
   * The canvas that is painted on
   */
  val canvas: Canvas,

  /**
   * The zoom and pan modifier that is used to enforce the limits for zooming and panning.
   * @see [ZoomAndTranslationSupport]
   */
  zoomAndTranslationModifier: ZoomAndTranslationModifier = ZoomAndTranslationModifier.none,
  /**
   * Provides the defaults for zoom and translation
   * @see [ZoomAndTranslationSupport]
   */
  zoomAndTranslationDefaults: ZoomAndTranslationDefaults = ZoomAndTranslationDefaults.noTranslation,
  /**
   * How the zoom is changed between two consecutive zoom levels
   */
  zoomChangeFactor: Double = ZoomLevelCalculator.SQRT_2,

  /**
   * The binding strategy that is used to bind the window size to the canvas size
   */
  windowSizeBindingStrategy: WindowSizeBindingStrategy = ImmediateWindowSizeBindingStrategy,

  /**
   * Identifies the chart. This ID must be unique!
   * The ID is used as identifier for caches
   */
  val chartId: ChartId = ChartId.next(),
) : Disposable, OnDispose {
  /**
   * Disposable for the chart
   */
  private val disposeSupport: DisposeSupport = DisposeSupport().also {
    it.onDispose {
      //Cleanup all global caches
      GlobalCacheSupport.cleanup(chartId)
    }
  }

  /**
   * Returns true if this chart support has been disposed
   */
  val disposed: Boolean
    get() = disposeSupport.disposed

  /**
   * Holds the dirty state
   */
  val dirtySupport: DirtySupport = DirtySupport()

  /**
   * If set to true, refreshing and painting is disabled.
   * This can be used to avoid unnecessary paintings - e.g. if the canvas is currently not shown in the UI
   */
  val disabledProperty: ObservableBoolean = ObservableBoolean(false)

  /**
   * If set to true the canvas does *not* paint itself
   */
  var disabled: Boolean by disabledProperty

  /**
   * The root chart state - as configured for the chart itself
   */
  val rootChartState: MutableChartState = DefaultChartState()

  /**
   * The current chartState.
   *
   * Contains information about panning/zooming.
   */
  var currentChartState: ChartState = rootChartState
    @Deprecated("do not use directly! Use the method withCurrentChartState instead")
    set(value) {
      field = value
      chartCalculator = ChartCalculator(value)
    }

  /**
   * Adds a new chart state on the stack and executes the given action with the new current chart state
   */
  inline fun <T> withCurrentChartState(chartState: ChartState, action: () -> T): T {
    contract {
      callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }

    val oldCurrentChartState = currentChartState
    try {
      @Suppress("DEPRECATION")
      currentChartState = chartState
      return action()
    } finally {
      //Reset the original current chart state
      @Suppress("DEPRECATION")
      currentChartState = oldCurrentChartState
    }
  }

  /**
   * Adds a new chart state on the stack and executes the given action with the new current chart state
   */
  inline fun <T> withCurrentChartState(chartStateProvider: ChartState.() -> ChartState, action: () -> T): T {
    return withCurrentChartState(chartStateProvider(currentChartState), action)
  }

  /**
   * Returns the chart calculator that can be used to convert domain values in pixels  - for the current chart state.
   *
   * The chart calculator is automatically recreated when a new current chart state is set ([currentChartState]).
   */
  var chartCalculator: ChartCalculator = ChartCalculator(currentChartState)
    get() {
      require(field.chartState == currentChartState) { "Uups" }
      return field
    }
    private set

  /**
   * The zoom and pan support is used to zoom and/or pan the canvas (e.g. from mouse interactions)
   */
  val zoomAndTranslationSupport: ZoomAndTranslationSupport = ZoomAndTranslationSupport(chartCalculator, rootChartState, zoomAndTranslationModifier, zoomAndTranslationDefaults, zoomChangeFactor)

  /**
   * Contains the behavior for when the canvas size is changed (e.g. resize of the stage)
   */
  val windowResizeBehaviorProperty: ObservableObject<WindowResizeBehavior> = ObservableObject(KeepCenterOnWindowResize)

  /**
   * The resize behavior
   */
  var windowResizeBehavior: WindowResizeBehavior by windowResizeBehaviorProperty

  /**
   * The target refresh rate property
   */
  val targetRefreshRateProperty: ObservableObject<TargetRefreshRate> = ObservableObject(TargetRefreshRate.veryFast60)

  /**
   * The target refresh rate.
   * Attention: The target refresh rate describes the calls to the [RefreshListener]s.
   * Not every refresh results in a repaint!
   *
   * The canvas will only be repainted if [markAsDirty] has been called and therefore [dirtySupport] returns [DirtySupport.dirty] `true`.
   */
  var targetRefreshRate: TargetRefreshRate by targetRefreshRateProperty

  /**
   * The service registry that provides services related to the chart support
   */
  val serviceRegistry: ServiceRegistry = ServiceRegistry().also {
    disposeSupport.onDispose {
      it.dispose()
    }
  }

  /**
   * Interface to mouse events of the [canvas]
   */
  val mouseEvents: MouseEventBroker = canvas.mouseEvents

  /**
   * Interface to key events of the [canvas]
   */
  val keyEvents: KeyEventBroker = canvas.keyEvents

  /**
   * Interface to pointer events of the [canvas]
   */
  val pointerEvents: PointerEventBroker = canvas.pointerEvents

  /**
   * Interface to touch events of the [canvas]
   */
  val touchEvents: TouchEventBroker = canvas.touchEvents

  /**
   * The refresh listeners are notified on every refresh (more often than the paint listeners)
   */
  internal val refreshListeners = mutableListOf<RefreshListener>()

  /**
   * These refresh listeners will be removed before the next call.
   * Used to avoid concurrent modification exceptions
   */
  internal val refreshListenersToRemove = mutableListOf<RefreshListener>()

  /**
   * Contains the paint listeners that are notified on every paint.
   * A repaint only happens if the canvas has been marked as dirty
   */
  private val paintListeners = mutableListOf<PaintListener>()

  init {
    require(canvas.type == CanvasType.Main) { "A canvas with type main is required" }

    onRefresh { _, _, _ -> //Update the device pixel ratio from the environment - once before each repaint
      updateEnvironment()
      devicePixelRatioSupport.updateDevicePixelRatio(environment.devicePixelRatio)
    }

    //Bind the window size to the canvas size
    windowSizeBindingStrategy.bind(rootChartState, canvas, disposeSupport)

    rootChartState.onChange {
      //Mark as dirty whenever the chart state changes
      markAsDirty(DirtyReason.ChartStateChanged)
    }

    //Repaint is necessary if one of the snap options has been changed
    @Suppress("LeakingThis")
    pixelSnapSupport.snapConfigurationProperty.registerDirtyListener(this, DirtyReason.ConfigurationChanged)

    //On tooltip changes, repaint
    @Suppress("LeakingThis")
    tooltipSupport.tooltip.registerDirtyListener(this, DirtyReason.Tooltip)

    //Repaint if the paint disabled property is changed
    disabledProperty.consume(false) { newValue ->
      if (newValue) {
        //Paint painting disabled text
        paintPaintDisabledText(canvas.gc)
      } else {
        //repaint is necessary after a change
        markAsDirty(DirtyReason.Initial)
      }
    }

    //Mark as dirty initially to ensure at least one repaint
    markAsDirty(DirtyReason.Initial)
  }


  /**
   * Manages the layers
   */
  val layerSupport: LayerSupport = DefaultLayerSupport(this)


  /**
   * Marks the canvas as dirty - the canvas will be painted on the next tick.
   *
   * This method can be called often
   */
  fun markAsDirty(reason: DirtyReason) {
    dirtySupport.markAsDirty(reason)
  }

  /**
   * The last time the refresh has been called
   * Double.NaN represents a non value
   */
  private var lastRefreshTime: Double = Double.NaN

  /**
   * The next target refresh time.
   * If now is greater than the time stored in this variable, a refresh is required.
   *
   * We try to refresh on the first event in each interval defined by the [targetRefreshRate].
   */
  private var nextRefreshMinTime: @ms Double = 0.0

  /**
   * The last time we painted
   */
  @ms
  private var lastPaintTime = 0.0

  /**
   * The last painting index
   */
  private var lastPaintingLoopIndex = PaintingLoopIndex(-1)

  /**
   * Refreshes the canvas. Do *not* call this method directly.
   * This method must only be called by a timer.
   *
   * Use [markAsDirty] instead to mark the canvas as dirty and trigger a paint as soon as possible.
   *
   * @param frameTimestamp the timestamp of the current frame
   */
  fun refresh(frameTimestamp: @ms Double) {
    require(!disposeSupport.disposed) {
      "Already disposed! Do not paint anymore"
    }

    if (disabled) {
      return
    }

    if (nextRefreshMinTime > frameTimestamp) {
      //next refresh time is in the future. Skip this refresh
      return
    }

    //Calculate the next refresh min
    targetRefreshRate.distance?.let {
      nextRefreshMinTime += it
      //The following lines are relevant for the first timestamp and
      //if refresh events don't come as fast as requested.
      if (nextRefreshMinTime < frameTimestamp) {
        nextRefreshMinTime = frameTimestamp + it * 0.5
      }
    }

    //Refresh all necessary things.
    @ms val refreshDelta: Double = if (lastRefreshTime.isNaN()) {
      Double.NaN
    } else {
      frameTimestamp - lastRefreshTime
    }

    //Remember the refresh time
    lastRefreshTime = frameTimestamp

    //clean up the refresh listeners
    if (refreshListenersToRemove.isNotEmpty()) {
      refreshListeners.removeAll(refreshListenersToRemove)
      refreshListenersToRemove.clear()
    }

    //Store the current frame timestamp
    currentFrameTimestampOrNull = frameTimestamp

    try {
      refreshListeners.fastForEach {
        it.refresh(this, frameTimestamp, refreshDelta)
      }

      //If somebody has marked the canvas as dirty it is repainted
      dirtySupport.ifDirty { dirtyReasons: DirtyReasonBitSet ->
        if (canvas.width <= 0.0 || canvas.height <= 0.0) {
          // do not paint if there is nothing to see
          return@ifDirty
        }

        if (currentChartState.hasZeroSize) {
          //The chart state has zeros (windows size or content area size) - do not paint
          //This is possible if the DelayedWindowSizeBindingStrategy is used.
          return@ifDirty
        }

        //The canvas might have been reset (HTML canvas on resize)
        //Therefore it is necessary to set the defaults
        canvas.gc.applyDefaults()

        //Paint with a saved canvas
        canvas.gc.saved {
          @ms val repaintDelta = if (lastPaintTime == 0.0) 0.0 else frameTimestamp - lastPaintTime
          lastPaintTime = frameTimestamp

          val paintingIndex = lastPaintingLoopIndex.next().also {
            lastPaintingLoopIndex = it
          }

          for (i in 0 until paintListeners.size) {
            paintListeners[i].paint(frameTimestamp, repaintDelta, paintingIndex, dirtyReasons)
          }
        }
      }
    } finally {
      //Set the frame timestamp to null after painting
      currentFrameTimestampOrNull = null
    }
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }

  /**
   * Registers a paint listener that is notified on every repaint
   */
  fun onPaint(paintListener: PaintListener) {
    paintListeners.add(paintListener)
  }


  /**
   * Registers a refresh listener that is notified whenever refresh is called
   */
  fun onRefresh(refreshListener: RefreshListener) {
    refreshListeners.add(refreshListener)
  }

  /**
   * Unregisters the refresh listener.
   *
   * Attention: The refresh listener is unregistered on the *next* run to avoid concurrent modifications of the listeners list.
   */
  fun removeOnRefresh(refreshListener: RefreshListener) {
    //Schedule for removal
    refreshListenersToRemove.add(refreshListener)
  }
}

/**
 * The backing field for the current frame timestamp.
 * Will be set to null when no frame is currently being painted.
 */
private var currentFrameTimestampOrNull: @ms Double? = null

/**
 * Returns the timestamp of the current frame.
 *
 * ATTENTION: Will throw an exception if called from outside the frame paint method!
 */
val currentFrameTimestamp: @ms Double
  get() {
    return currentFrameTimestampOrNull ?: throw IllegalStateException("Not currently in a frame")
  }


/**
 * Is called on refresh
 */
fun interface RefreshListener {
  /**
   * Is called on refresh
   */
  fun refresh(
    chartSupport: ChartSupport,
    /**
     * The timestamp of the current frame
     */
    frameTimestamp: @ms Double,
    /**
     * The delta since the last refresh call.
     * Contains [Double.NaN] on the first refresh.
     */
    refreshDelta: @ms Double
  )
}

/**
 * Is called on paint
 */
fun interface PaintListener {
  /**
   * Paints the canvas.
   * @param frameTimestamp the timestamp of the current frame
   * @param delta the delta to the last paint (0 on the first paint)
   * @param paintingLoopIndex the index of the paint call
   * @param dirtyReasons the reasons why the canvas is dirty
   */
  fun paint(frameTimestamp: @ms Double, delta: @ms Double, paintingLoopIndex: PaintingLoopIndex, dirtyReasons: DirtyReasonBitSet)
}


/**
 * Paints a template text when paining is disabled.
 * This way it becomes obvious if [ChartSupport.disabled] is not set back to false
 */
fun paintPaintDisabledText(gc: CanvasRenderingContext) {
  gc.fillStyle(Color.lightgray)
  gc.fillRect(0.0, 0.0, gc.width, gc.height)

  //Paint a text
  gc.fillStyle(Color.darkgray)
  gc.fillText("Painting is disabled", 0.0, 0.0, Direction.TopLeft)
}

/**
 * Registers a dirty listener that marks the chart support as dirty, every time the property changes
 */
fun ReadOnlyObservableObject<out Any?>.registerDirtyListener(chartSupport: ChartSupport, reason: DirtyReason) {
  consume {
    chartSupport.markAsDirty(reason)
  }
}

/**
 * Manages the properties calculators.
 */
val ChartSupport.paintingProperties: PaintingProperties
  get() {
    return serviceRegistry.get(PaintingProperties::class) {
      PaintingProperties()
    }
  }

/**
 * Device pixel ratio related calculations
 */
val ChartSupport.devicePixelRatioSupport: DevicePixelRatioSupport
  get() {
    return serviceRegistry.get(DevicePixelRatioSupport::class) {
      DevicePixelRatioSupport().apply {
        //if the device pixel ratio has changed repaint is necessary
        devicePixelRatioProperty.registerDirtyListener(this@devicePixelRatioSupport, DirtyReason.ConfigurationChanged)
      }
    }
  }

/**
 * The text service to be used for every text painted on the canvas
 */
val ChartSupport.textService: DefaultTextService
  get() {
    return serviceRegistry.get(DefaultTextService::class) {
      DefaultTextService().also {
        it.addFallbackTextResolver()
      }
    }
  }

/**
 * Configures the tooltip
 */
val ChartSupport.tooltipSupport: TooltipSupport
  get() {
    return serviceRegistry.get(TooltipSupport::class) {
      TooltipSupport()
    }
  }

/**
 * Provides information about what is located at a provided location
 */
val ChartSupport.whatsAt: WhatsAtSupport
  get() {
    return serviceRegistry.get(WhatsAtSupport::class) {
      WhatsAtSupport()
    }
  }

/**
 * The debug configuration
 */
val ChartSupport.debug: DebugConfiguration
  get() {
    return canvas.gc.debug
  }

/**
 * Returns the native components support from the canvas
 */
val ChartSupport.nativeComponentsSupport: NativeComponentsSupport
  get() = canvas.nativeComponentsSupport

/**
 * Support for mouse cursor state
 */
val ChartSupport.mouseCursorSupport: MouseCursorSupport
  get() {
    return serviceRegistry.get(MouseCursorSupport::class) {
      MouseCursorSupport(canvas.mouseCursor)
    }
  }

/**
 * Model for the resize handlers
 */
val ChartSupport.resizeHandlesSupport: ResizeHandlesSupport
  get() {
    return serviceRegistry.get(ResizeHandlesSupport::class) {
      ResizeHandlesSupport()
    }
  }


/**
 * Contains the locales that should be used for the canvas
 */
val ChartSupport.i18nSupport: I18nSupport
  get() {
    return serviceRegistry.get(I18nSupport::class) {
      I18nSupport()
    }
  }

/**
 * Returns the i18n configuration
 */
val ChartSupport.i18nConfiguration: I18nConfiguration
  get() {
    return i18nSupport.configuration
  }

/**
 * Supports starting of timers
 */
val ChartSupport.timerSupport: TimerSupport
  get() {
    return serviceRegistry.get(TimerSupport::class) {
      TimerSupport(this)
    }
  }

/**
 * Returns the device pixel ratio (shortcut for a call to [devicePixelRatioSupport])
 */
val ChartSupport.devicePixelRatio: Double
  get() {
    return devicePixelRatioSupport.devicePixelRatio
  }

/**
 * Support for snapping pixels
 */
val ChartSupport.pixelSnapSupport: PixelSnapSupport
  get() {
    return serviceRegistry.get(PixelSnapSupport::class) {
      PixelSnapSupport()
    }
  }

/**
 * Support for translating a chart over time.
 * Usually used when the x-axis shows the current time.
 */
val ChartSupport.translateOverTime: ChartTranslateOverTimeService
  get() {
    return serviceRegistry.get(ChartTranslateOverTimeService::class) {
      ChartTranslateOverTimeService(this).also {
        onRefresh(it)
      }
    }
  }

/**
 * Zooms in while computing the center of the zoom from [zoomCenterFactorX] and [zoomCenterFactorY] which are
 * applied to the width and height of the canvas, respectively.
 * @see zoomOut
 * @see resetOnlyZoom
 */
fun ChartSupport.zoomIn(@pct zoomCenterFactorX: Double = 0.5, @pct zoomCenterFactorY: Double = 0.5, reason: UpdateReason) {
  zoomAndTranslationSupport.modifyZoom(
    true,
    AxisSelection.Both,
    canvas.width * zoomCenterFactorX,
    canvas.height * zoomCenterFactorY,
    reason = reason,
  )
}

/**
 * Zooms out while computing the center of the zoom from [zoomCenterFactorX] and [zoomCenterFactorY] which are
 * applied to the width and height of the canvas, respectively.
 * @see zoomIn
 * @see resetOnlyZoom
 */
fun ChartSupport.zoomOut(@pct zoomCenterFactorX: Double = 0.5, @pct zoomCenterFactorY: Double = 0.5, reason: UpdateReason) {
  zoomAndTranslationSupport.modifyZoom(
    false,
    AxisSelection.Both,
    canvas.width * zoomCenterFactorX,
    canvas.height * zoomCenterFactorY,
    reason = reason,
  )
}

/**
 * Resets (only!) the zoom while computing the center of the zoom from [zoomCenterFactorX] and [zoomCenterFactorY] which are
 * applied to the width and height of the canvas, respectively.
 *
 * Usually it is better to call [resetZoomAndTranslationToDefaults]
 *
 * @see zoomIn
 * @see zoomOut
 * @see resetZoomAndTranslationToDefaults
 */
fun ChartSupport.resetOnlyZoom(@pct zoomCenterFactorX: Double = 0.5, @pct zoomCenterFactorY: Double = 0.5, reason: UpdateReason) {
  zoomAndTranslationSupport.resetZoom(
    zoomCenter = Coordinates.of(
      canvas.width * zoomCenterFactorX,
      canvas.height * zoomCenterFactorY
    ),
    reason = reason,
  )
}

/**
 * Resets the view to the defaults (zoom and translation)
 */
fun ChartSupport.resetZoomAndTranslationToDefaults(reason: UpdateReason) {
  zoomAndTranslationSupport.resetToDefaults(reason = reason)
}

/**
 * Binds the visible range of this [ChartSupport] to the visible range of the [other] and vice versa.
 *
 * Initially, the visible range of [other] will be applied to this [ChartSupport]
 */
fun ChartSupport.bindVisibleRangeBidirectional(other: ChartSupport, axisSelection: AxisSelection) {
  //also bind the animated property
  translateOverTime.animatedProperty.bindBidirectional(other.translateOverTime.animatedProperty)

  var updating = false

  fun apply(source: ChartSupport, target: ChartSupport) {
    if (!updating) {
      updating = true
      try {
        @DomainRelative val topLeft = source.chartCalculator.window2domainRelative(0.0, 0.0)
        @DomainRelative val bottomRight = source.chartCalculator.window2domainRelative(source.currentChartState.windowWidth, source.currentChartState.windowHeight)

        if (axisSelection.containsX) {
          if (!topLeft.x.isNanOrInfinite() && !bottomRight.x.isNanOrInfinite() && topLeft.x != bottomRight.x) {
            target.zoomAndTranslationSupport.fitX(topLeft.x, bottomRight.x, UpdateReason.BoundToOtherChart)
          }
        }
        if (axisSelection.containsY) {
          if (!topLeft.y.isNanOrInfinite() && !bottomRight.y.isNanOrInfinite() && topLeft.y != bottomRight.y) {
            target.zoomAndTranslationSupport.fitY(topLeft.y, bottomRight.y, UpdateReason.BoundToOtherChart)
          }
        }
      } finally {
        updating = false
      }
    }
  }

  //applies the visible range of this chart support to the other chart support
  fun applyThis2Other() {
    apply(this, other)
  }

  //applies the visible range of the other chart support to this chart support
  fun applyOther2This() {
    apply(other, this)
  }

  //apply values from other to this initially
  other.rootChartState.windowSizeProperty.consumeImmediately {
    applyOther2This()
  }
  other.rootChartState.windowTranslationProperty.consumeImmediately {
    applyOther2This()
  }

  //apply values from this to other when changed
  rootChartState.windowSizeProperty.consume {
    applyThis2Other()
  }
  rootChartState.windowTranslationProperty.consume {
    applyThis2Other()
  }

}
