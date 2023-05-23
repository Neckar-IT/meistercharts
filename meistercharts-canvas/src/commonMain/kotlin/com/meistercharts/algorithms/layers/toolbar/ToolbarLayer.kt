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
package com.meistercharts.algorithms.layers.toolbar

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AbstractPaintingVariables
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.layout.PaintableIndex
import com.meistercharts.algorithms.layout.PaintablesLayouter
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.findXWithAnchor
import com.meistercharts.canvas.findYWithAnchor
import com.meistercharts.canvas.mouseCursorSupport
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonPriority
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import it.neckar.logging.LoggerFactory
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastMap
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.reduceObservables
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.asMultiProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.other.px

/**
 * A toolbar with interactive buttons.
 * Attention: [#initialize] must be called.
 */
class ToolbarLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    buttons: List<Button>,
    styleConfiguration: Configuration.() -> Unit = {},
  ) : this(Configuration(buttons), styleConfiguration)

  init {
    configuration.also(additionalConfiguration)
  }

  override val type: LayerType
    get() = LayerType.Content

  /**
   * Initializes the layer using the given painting context
   */
  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    val chartSupport = paintingContext.chartSupport

    //force repaint if the state has changed
    configuration.buttons.fastForEach { button ->
      button.stateProperty.consume {
        chartSupport.markAsDirty(DirtyReason.UiStateChanged)
      }
    }

    //Collect all button states
    val buttonStates: List<ObservableObject<ButtonState>> = configuration.buttons.fastMap { it.stateProperty }

    //Is set to true if any of the buttons has the state hover
    val anyButtonHover = reduceObservables(
      buttonStates
    ) { statesList ->
      statesList.any {
        it.hover
      }
    }

    // Show the hand cursor if any button is hovered
    chartSupport.mouseCursorSupport.cursorProperty(this).bind(anyButtonHover.map {
      if (it) {
        MouseCursor.Hand
      } else {
        null
      }
    })
  }

  /**
   * Returns the index of the button at the given window coordinates.
   * Returns null if no button could be found
   */
  private fun findButtonIndex(coordinates: @Window Coordinates): PaintableIndex? {
    @Zoomed val relativeX = coordinates.x - paintingVariables.anchorX
    @Zoomed val relativeY = coordinates.y - paintingVariables.anchorY

    return paintingVariables.layouter.findIndex(relativeX, relativeY)
  }

  /**
   * Handle mouse events on buttons
   */
  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      val foundIndex = findButtonIndex(event.coordinates ?: return Ignored)

      //Update all buttons if necessary
      paintingVariables.buttonsToPaint.fastForEachIndexed { paintableIndexAsInt, button ->
        val paintableIndex = PaintableIndex(paintableIndexAsInt)
        button.hover(paintableIndex == foundIndex)
      }

      return Ignored
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      val foundIndex = findButtonIndex(event.coordinates)

      paintingVariables.buttonsToPaint.fastForEachIndexed { paintableIndexAsInt, button ->
        val paintableIndex = PaintableIndex(paintableIndexAsInt)
        button.hover(paintableIndex == foundIndex)
      }

      return Ignored
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      val foundIndex = findButtonIndex(event.coordinates) ?: return Ignored

      paintingVariables.buttonsToPaint.valueAt(foundIndex.value).onDown()

      return Consumed
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      val foundIndex = findButtonIndex(event.coordinates) ?: return Ignored

      paintingVariables.buttonsToPaint.valueAt(foundIndex.value)
        .onUp(chartSupport, event.coordinates)

      return Consumed
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      findButtonIndex(event.coordinates) ?: return Ignored
      //Consume double clicks on the button to avoid consumption from other layers
      return Consumed
    }
  }

  override fun paintingVariables(): PaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : AbstractPaintingVariables() {
    val layouter = PaintablesLayouter()

    var anchorX: @px Double = 0.0
    var anchorY: @px Double = 0.0

    var buttonShowMode = ButtonShowMode.All

    /**
     * The buttons that shall be painted
     */
    var buttonsToPaint: SizedProvider<Button> = SizedProvider.empty()

    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      val gc = paintingContext.gc

      anchorX = gc.findXWithAnchor(gc.width, configuration.gap, configuration.anchorDirection.horizontalAlignment)
      anchorY = gc.findYWithAnchor(gc.height, configuration.gap, configuration.anchorDirection.verticalAlignment)

      layouter.configuration.horizontalAlignment = configuration.anchorDirection.horizontalAlignment //TODO is this correct??
      layouter.configuration.verticalAlignment = configuration.anchorDirection.verticalAlignment //TODO is this correct??
      layouter.configuration.layoutOrientation = configuration.layoutOrientation

      layouter.configuration.gap = configuration.buttonGap

      //Calculate with all buttons
      layouter.calculate(paintingContext, configuration.allButtonsProvider, configuration.anchorDirection)
      val totalSize = layouter.totalSize()
      if (totalSize.width > paintingContext.width || totalSize.height > paintingContext.height) {
        // Too large - hide buttons with low priority
        buttonsToPaint = configuration.highPriorityButtonsProvider
        buttonShowMode = ButtonShowMode.OnlyLowPriority

        logger.warn("Warning! Not implemented correctly!")

        //recalculate the layout for the low priority buttons
        layouter.calculate(paintingContext, configuration.highPriorityButtonsProvider)
      } else {
        buttonsToPaint = configuration.allButtonsProvider
        buttonShowMode = ButtonShowMode.All
      }
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    paintingVariables.verifyLoopIndex(paintingContext)

    val gc = paintingContext.gc

    gc.translate(paintingVariables.anchorX, paintingVariables.anchorY)
    paintingVariables.layouter.paintAllPaintables(paintingContext, paintingVariables.buttonsToPaint.asMultiProvider())
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.algorithms.layers.toolbar.ToolbarLayer")
  }

  /**
   * Which buttons are shown
   */
  enum class ButtonShowMode {
    All, OnlyLowPriority
  }

  @ConfigurationDsl
  class Configuration constructor(
    /**
     * The (fixed) list of buttons.
     * The buttons must not change during runtime!
     */
    val buttons: List<Button>,
  ) {

    /**
     * Provides all buttons (including low priority).
     * Always returns the *same* buttons
     */
    val allButtonsProvider: SizedProvider<Button> = SizedProvider.forList(buttons)

    /**
     * Provides only the high priority buttons
     * Always returns the *same* buttons
     */
    val highPriorityButtonsProvider: SizedProvider<Button> = SizedProvider.forList(buttons.filter { it.priority == ButtonPriority.AlwaysVisible })

    /**
     * where to place the toolbar - relative to the window
     *
     * Attention: Set [layoutOrientation], too
     */
    var anchorDirection: Direction = Direction.TopCenter

    /**
     * The distance of the toolbar from the anchor
     */
    var gap: @px Double = 5.0

    /**
     * How the buttons are laid out.
     *
     * Attention: Set [anchorDirection], too
     */
    var layoutOrientation: Orientation = Orientation.Horizontal

    /**
     * The gap between the buttons
     */
    @px
    var buttonGap: Double = 4.0
  }
}
