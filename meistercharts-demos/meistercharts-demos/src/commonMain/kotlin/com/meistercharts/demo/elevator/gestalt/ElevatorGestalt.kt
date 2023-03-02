package com.meistercharts.demo.elevator.gestalt

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.BackgroundImageLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.toolbar.ToolbarButtonFactory
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.CombinedPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.charts.ChartGestalt
import com.meistercharts.charts.ToolbarGestalt
import com.meistercharts.design.corporateDesign
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom

/**
 *
 */
@ExperimentalStdlibApi
class ElevatorGestalt(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  private val roofHeight = @ContentArea 50.0
  private val storyHeight = @ContentArea 200.0

  val model: ElevatorModel = ElevatorModel()
  val elevatorAnimationManager: ElevatorAnimationManager = ElevatorAnimationManager(model)

  init {
  }

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    with(meisterChartBuilder) {
      @Zoomed val marginOther = 15.0

      configure {
        chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize
      }

      zoomAndTranslationDefaults {
        object : ZoomAndTranslationDefaults {
          override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
            val chartState = chartCalculator.chartState
            if (chartState.hasZeroSize) {
              return Zoom.default
            }

            @Zoomed val roofHeightZoomed = chartCalculator.contentArea2zoomedY(roofHeight)

            @Zoomed val windowSize = chartState.windowSize

            @Zoomed val windowNetWidth = windowSize.width - marginOther - marginOther
            @Zoomed val windowNetHeight = windowSize.height - roofHeightZoomed - 2 * marginOther

            if (windowNetHeight <= 0.0 || windowNetHeight <= 0.0) {
              return Zoom.default
            }

            val contentAreaSize = chartState.contentAreaSize

            return Zoom.of(
              1.0 / contentAreaSize.width * windowNetWidth,
              1.0 / contentAreaSize.height * windowNetHeight
            ).smallerValueForBoth()
          }

          override fun defaultTranslation(chartCalculator: ChartCalculator): Distance {
            @Zoomed val roofHeightZoomed = chartCalculator.contentArea2zoomedY(roofHeight)
            return Distance.of(marginOther, roofHeightZoomed + marginOther)
          }
        }
      }

      enableZoomAndTranslation = false


      contentAreaSizingStrategy = FixedContentAreaSize {
        Size(1000.0, model.floorRange.numberOfFloors * storyHeight)
      }


      configure {
        chartSupport.onRefresh(elevatorAnimationManager)

        model.requestedFloorsProperty.consume {
          markAsDirty()
        }
        model.elevatorTargetProperty.consume {
          markAsDirty()
        }
      }

      val toolbarButtonFactory = ToolbarButtonFactory()
      val buttons = buildList {
        add(toolbarButtonFactory.button(getButtonPaintableProvider("EG"), ElevatorResources.buttonSize) {
          elevatorAnimationManager.pressedFloorButton(0)
        }.also { toggleButton ->
          toggleButton.selectedIfFloorRequested(0)
        })

        //add the remaining buttons
        for (targetFloor in 1..4) {
          add(toolbarButtonFactory.button(getButtonPaintableProvider("$targetFloor"), ElevatorResources.buttonSize) {
            elevatorAnimationManager.pressedFloorButton(targetFloor)
          }.also { toggleButton ->
            toggleButton.selectedIfFloorRequested(targetFloor)
          })
        }
      }

      val elevatorToolbarGestalt = ToolbarGestalt(
        ToolbarGestalt.Data(buttons)
      ).also {
        it.toolbarLayer.configuration.anchorDirection = Direction.CenterRight
        it.toolbarLayer.configuration.layoutOrientation = Orientation.Vertical
      }

      configure {
        layers.addClearBackground()

        layers.addLayer(BackgroundImageLayer().also {
          it.style.backgroundImage = ElevatorResources.backgroundImage
        })

        layers.addLayer(ElevatorLayer(model))

        layers.addVersionNumberHidden()
      }

      if (false) {
        //Add again if zooming and panning is enabled
        ToolbarGestalt().configure(this)
      }
      elevatorToolbarGestalt.configure(this)
    }
  }

  /**
   * Binds the selected state of this button.
   * If the floor number is requested, the selected state is set to true, false otherwise
   */
  private fun Button.selectedIfFloorRequested(targetFloor: Int) {
    selectedProperty.bind(model.requestedFloorsProperty.map {
      it.contains(targetFloor)
    })
  }

  private fun getButtonPaintableProvider(label: String): (ButtonState) -> Paintable {
    return { buttonState ->
      val image: Paintable = when (buttonState.simpleToggle) {
        ButtonState.SimpleToggle.Default                                    -> ElevatorResources.floorElevatorButton
        ButtonState.SimpleToggle.Pressed, ButtonState.SimpleToggle.Selected -> ElevatorResources.floorElevatorButtonActive
        ButtonState.SimpleToggle.Hover                                      -> ElevatorResources.floorElevatorButtonHover
      }

      val combinedPaintable = CombinedPaintable(image, object : Paintable {
        override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
          return Rectangle.zero
        }

        override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
          val gc = paintingContext.gc
          gc.font(corporateDesign.h3)
          gc.fill(
            if (buttonState.pressed || buttonState.selected) {
              Color.lime
            } else {
              Color.white
            }
          )
          gc.fillText(label, x, y, Direction.Center)
        }
      }, Distance.none)

      combinedPaintable
    }
  }


  class Data(
  )

  class Style {
  }
}
