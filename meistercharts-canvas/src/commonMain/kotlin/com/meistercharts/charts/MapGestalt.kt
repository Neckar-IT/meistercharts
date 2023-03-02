package com.meistercharts.charts

import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.delegate
import com.meistercharts.algorithms.layers.ScrollWithoutModifierMessageLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addScrollWithoutModifierHint
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMap
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapDe
import com.meistercharts.algorithms.layers.slippymap.SlippyMapCenter
import com.meistercharts.algorithms.layers.slippymap.SlippyMapLayer
import com.meistercharts.algorithms.layers.slippymap.SlippyMapProvider
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.layers.toolbar.ToolbarLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.DefaultButtonColorProvider
import com.meistercharts.canvas.paintable.SingleButtonColorProvider
import com.meistercharts.canvas.paintable.ZoomButtons
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject

/**
 * Gestalt for Slippy Map
 *
 */
class MapGestalt(
  val chartId: ChartId,
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  val slippyMapLayer: SlippyMapLayer = SlippyMapLayer(chartId, OpenStreetMapDe) {
  }

  val zoomInToolbarButton: Button = Button({ paintingContext, state, width, height ->
    val gc = paintingContext.gc

    gc.lineWidth = toolbarButtonSymbolLineWidth
    gc.fill(style.toolbarButtonBackgroundProvider(state))
    gc.stroke(style.toolbarButtonForegroundProvider(state))

    ZoomButtons.paintZoomIn(gc, toolbarButtonSymbolSize, width, height)
  }, toolbarButtonWidth, toolbarButtonHeight)

  val zoomOutToolbarButton: Button = Button({ paintingContext, state, width, height ->
    val gc = paintingContext.gc
    gc.fill(style.toolbarButtonBackgroundProvider(state))
    gc.fillRect(0.0, 0.0, width, height)
    gc.stroke(style.toolbarButtonForegroundProvider(state))
    gc.lineWidth = toolbarButtonSymbolLineWidth

    ZoomButtons.paintZoomOut(gc, toolbarButtonSymbolSize, width, height)
  }, toolbarButtonWidth, toolbarButtonHeight)

  val toolbarLayer: ToolbarLayer = ToolbarLayer(listOf(zoomInToolbarButton, zoomOutToolbarButton)) {
    gap = 20.0
    buttonGap = 5.0
    anchorDirection = Direction.BottomRight
    layoutOrientation = Orientation.Vertical
  }

  /**
   * Shows the legal notice if configured in the style
   * see https://www.openstreetmap.org/copyright/en
   * TODO add a Link-Layer
   */
  val legalNoticeLayer: TextLayer = TextLayer({ _, _ -> listOf(slippyMapLayer.data.slippyMapProvider.legalNotice.orEmpty()) }) {
    textColor = Color.gray
    font = FontDescriptorFragment(10.0)
    anchorDirection = Direction.BottomLeft
    anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
  }

  init {
    data.slippyMapProviderProperty.consumeImmediately {
      slippyMapLayer.data.slippyMapProvider = it
      slippyMapLayer.tileProvider.clear()
    }
  }

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    with(meisterChartBuilder) {

      SlippyMapBaseGestalt().configure(meisterChartBuilder)

      zoomAndTranslationDefaults(data::slippyMapCenter.delegate())

      configure {
        zoomInToolbarButton.action {
          chartSupport.zoomAndTranslationSupport.modifyZoom(true, AxisSelection.Both)
        }

        zoomOutToolbarButton.action {
          chartSupport.zoomAndTranslationSupport.modifyZoom(false, AxisSelection.Both)
        }

        layers.addClearBackground()
        layers.addLayer(slippyMapLayer)
        layers.addLayer(legalNoticeLayer.visibleIf(style.showCopyrightMarkerProperty))
        layers.addLayer(toolbarLayer.visibleIf(style.showToolbarProperty))

        layers.addScrollWithoutModifierHint(chartSupport, listOf(ScrollWithoutModifierMessageLayer.textKeyUseCtrlZoom))
      }
    }
  }

  class Data {
    /**
     * The center that is used when resetting the zoom
     */
    var slippyMapCenter: SlippyMapCenter = SlippyMapCenter.neckarItCenter

    /**
     * Provides the slippy map
     */
    val slippyMapProviderProperty: ObservableObject<SlippyMapProvider> = ObservableObject(OpenStreetMap)

    /**
     * Provides the slippy map
     */
    var slippyMapProvider: SlippyMapProvider by slippyMapProviderProperty
  }

  @StyleDsl
  class Style {
    /**
     * If set to true the copy right marker is shown at the bottom left
     */
    val showCopyrightMarkerProperty: ObservableBoolean = ObservableBoolean(true)

    var showCopyrightMarker: Boolean by showCopyrightMarkerProperty

    /**
     * Whether to show the toolbar or not
     */
    val showToolbarProperty: ObservableBoolean = ObservableBoolean(true)

    var showToolbar: Boolean by showToolbarProperty

    /**
     * Provides the color to be used as background for toolbar buttons
     */
    var toolbarButtonBackgroundProvider: ButtonColorProvider = DefaultButtonColorProvider(
      disabledColor = Color("#A3AAAE"),
      pressedColor = Color("rgba(116, 126, 131, 1.0)"),
      hoverColor = Color("rgba(116, 126, 131, 0.9)"),
      focusedColor = Color("rgba(116, 126, 131, 0.9)"),
      defaultColor = Color("rgba(116, 126, 131, 0.75)")
    )

    /**
     * Provides the color to be used as foreground for toolbar buttons
     */
    var toolbarButtonForegroundProvider: ButtonColorProvider = SingleButtonColorProvider(Color.white)
  }

  companion object {
    private const val toolbarButtonWidth: @Zoomed Double = 20.0
    private const val toolbarButtonHeight: @Zoomed Double = 20.0
    private const val toolbarButtonSymbolSize: @Zoomed Double = 8.0
    private const val toolbarButtonSymbolLineWidth: @Zoomed Double = 2.0
  }
}
