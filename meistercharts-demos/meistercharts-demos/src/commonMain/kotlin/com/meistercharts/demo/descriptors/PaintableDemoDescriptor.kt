package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.StackedBarPaintable
import com.meistercharts.algorithms.layers.barchart.StackedBarWithLabelPaintable
import com.meistercharts.algorithms.layers.circular.CircularChartPaintable
import com.meistercharts.algorithms.layers.compass.GaugePaintable
import com.meistercharts.algorithms.layers.legend.StackedPaintablesPaintable
import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipPaintable
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.CirclePaintable
import com.meistercharts.canvas.paintable.CombinedPaintable
import com.meistercharts.canvas.paintable.DebugPaintable
import com.meistercharts.canvas.paintable.LabelPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.paintable.SymbolAndImagePaintable
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
import com.meistercharts.canvas.paintable.TankPaintable
import com.meistercharts.canvas.paintable.TransparentPaintable
import com.meistercharts.canvas.paintable.toButtonPainter
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.section
import com.meistercharts.design.neckarit.NeckarItFlowPaintable
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.painter.CirclePointPainter
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.TextKey
import com.meistercharts.resources.Icons
import com.meistercharts.resources.LocalResourcePaintable
import com.meistercharts.resources.svg.SvgPaintableProviders

/**
 *
 */
class PaintableDemoDescriptor : ChartingDemoDescriptor<() -> Paintable> {
  override val name: String = "Paintable"
  override val category: DemoCategory = DemoCategory.Paintables

  //language=HTML
  override val description: String = """
    <h2>Paintables demo</h3>
    Dieses Demo nutzt die verschiedenen Paint-Methoden, die ein Paintable mitbringt.

      <h3>Center: Paint-Methode</h4>
      Verwendet die "normale" Paint-Methode des Paintables. D.h. das Paintable ist
      selbst für Positionierung (relativ zur Bildschirm-Mitte) sowie die Größe verantwortlich

      <h3>Ecken</h4>
      <p>
      In den Ecken wird die "paintInBoundingBox"-Methode verwendet. D.h. die Größe der BoundingBox ist vorgegeben.
      </p>

      <p>
      Die Bounding-Boxen sind jeweils rot dargestellt.
       </p>

      <h4>Top Left - mit Größe des Paintables</h4>
      <p>Übergibt die Größe des Paintables selbst.</p>
      <h5>Check</h5>
      Falls oben links nicht mit der Mitte überein stimmt (insbesondere die Größe), liefert das Paintable eine falsche Größe.


      <h4>Bottom Left - ContainNoGrow</h4>
      <p>Das Paintable wird in die Box eingepasst. Wächst aber nicht über die eigene Größe hinaus</p>

      <h5>Check</h5>
      Aspect Ratio muss der Mitte entsprechend. Darf niemals größer sein als die Mitte

      <h4>Top Right - Fill</h4>
      <p>Das Paintable wird in die Box eingepasst - die Aspect Ratio wird nicht berücksichtigt</p>

      <h4>Bottom Right - Contain</h4>
      <p>Das Paintable wird in die Box eingepasst - die Aspect Ratio wird beibehalten</p>

  """.trimIndent()

  override val predefinedConfigurations: List<PredefinedConfiguration<() -> Paintable>> = createPaintableConfigurations()

  override fun createDemo(configuration: PredefinedConfiguration<() -> Paintable>?): ChartingDemo {
    require(configuration != null) { "configuration required" }

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val paintable = configuration.payload()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x: Double = 0.0
            var y: Double = 0.0

            var boundingBoxFactorWidth: Double = 1.4
            var boundingBoxFactorHeight: Double = 1.7

            val boundingBoxFactorWidthFormatted: String
              get() {
                return decimalFormat2digits.format(boundingBoxFactorWidth)
              }

            val boundingBoxFactorHeightFormatted: String
              get() {
                return decimalFormat2digits.format(boundingBoxFactorHeight)
              }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              //"Normal" paint method in center
              gc.saved {
                gc.translateToCenter()
                gc.paintMark(color = Color.silver)

                gc.saved {
                  paintable.paint(paintingContext, x, y)
                }

                gc.translate(x, y)
                gc.paintMark(color = Color.lightgray)

                val boundingBox = paintable.boundingBox(paintingContext)
                gc.stroke(Color.lightgray)
                gc.fill(Color.black)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("paint", boundingBox.topLeft(), Direction.BottomLeft)
              }

              val paintableBoundingBox = paintable.boundingBox(paintingContext)

              //Paint in bounding box - size of paintable
              gc.saved {
                gc.translate(10.0, 20.0)
                gc.paintMark(color = Color.silver)

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.TopLeft, boundingBoxSize = paintableBoundingBox.size, objectFit = ObjectFit.ContainNoGrow)
                }

                val boundingBox = Rectangle(Coordinates.origin, paintableBoundingBox.size)
                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("paintInBoundingBox(paintableSize)", boundingBox.topLeft(), Direction.BottomLeft)
              }

              //Paint in bounding box, grow
              gc.saved {
                gc.translate(10.0, gc.height - 20.0)
                gc.paintMark(color = Color.silver)

                val boundingBox = Rectangle.bottomLeft(paintableBoundingBox.size.times(boundingBoxFactorWidth, boundingBoxFactorHeight))

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.BottomLeft, boundingBox.size, ObjectFit.ContainNoGrow)
                }

                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("ContainNoGrow (paintableSize * ($boundingBoxFactorWidthFormatted, $boundingBoxFactorHeightFormatted)", boundingBox.topLeft(), Direction.BottomLeft)
              }

              //In Bounding box fill
              gc.saved {
                gc.translate(gc.width - 10.0, 20.0)
                gc.paintMark(color = Color.silver)
                val boundingBox = Rectangle.topRight(paintableBoundingBox.size.times(boundingBoxFactorWidth, boundingBoxFactorHeight))

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.TopRight, boundingBox.size, ObjectFit.Fill)
                }

                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("Fill (paintableSize * ($boundingBoxFactorHeightFormatted, $boundingBoxFactorHeightFormatted)", boundingBox.topLeft(), Direction.BottomLeft)
              }

              //Contain
              gc.saved {
                gc.translate(gc.width - 10.0, gc.height - 20.0)
                gc.paintMark(color = Color.silver)
                val boundingBox = Rectangle.bottomRight(paintableBoundingBox.size.times(boundingBoxFactorWidth, boundingBoxFactorHeight))

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.BottomRight, boundingBox.size, ObjectFit.Contain)
                }

                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("Contain (paintableSize * ($boundingBoxFactorHeightFormatted, $boundingBoxFactorWidthFormatted)", boundingBox.topLeft(), Direction.BottomLeft)
              }
            }
          }
          layers.addLayer(layer)

          section("Paint (middle)")
          configurableDouble("X", layer::x) {
            min = -200.0
            max = 200.0
          }
          configurableDouble("Y", layer::y) {
            min = -200.0
            max = 200.0
          }

          section("Bounding box")
          configurableDouble("Width", layer::boundingBoxFactorWidth) {
            max = 5.0
          }
          configurableDouble("Height", layer::boundingBoxFactorHeight) {
            max = 5.0
          }

          if (paintable is DebugPaintable) {
            section("Debug-Paintable")
            configurableDouble("Width", paintable::width) {
              max = 500.0
            }
            configurableDouble("Height", paintable::height) {
              max = 500.0
            }

            configurableDouble("Alignment Point X", paintable::alignmentPointX) {
              min = -100.0
              max = 100.0
            }
            configurableDouble("Alignment Point Y", paintable::alignmentPointY) {
              min = -100.0
              max = 100.0
            }
          }
        }
      }
    }
  }

  companion object {
    fun createPaintableConfigurations(): List<PredefinedConfiguration<() -> Paintable>> {
      return listOf(
        PredefinedConfiguration({ DebugPaintable() }, "Debug"),
        PredefinedConfiguration({ LabelPaintable({ _, _ -> "Hello World!" }) }, "Label"),
        PredefinedConfiguration({ BalloonTooltipPaintable(RectanglePaintable(Size.PX_60, Color.orange)) }, "Balloon Tooltip"),
        PredefinedConfiguration({
          StackedPaintablesPaintable(
            SizedProvider.forValues(
              RectanglePaintable(10.0, 10.0, Color.red),
              RectanglePaintable(30.0, 20.0, Color.blue),
              RectanglePaintable(25.0, 30.0, Color.green),
            )
          )
        }, "VerticalLegendPaintable"),
        PredefinedConfiguration({ UrlPaintable.naturalSize("https://neckar.it/logo/social-media/nit-logo-n-neg_360x360_facebook_insta.png") }, "Url - natural"),
        PredefinedConfiguration({ UrlPaintable.fixedSize("https://neckar.it/logo/social-media/nit-logo-n-neg_360x360_facebook_insta.png", Size.PX_120) }, "Url - fixed"),

        PredefinedConfiguration({
          SvgPaintableProviders.mapMarker.get(Size.PX_120, Color.silver, alignmentPoint = Coordinates(-60.0, -120.0))
        }, "SVG MapMarker"),
        PredefinedConfiguration({ SvgPaintableProviders.neckarItQr.get(Size.PX_120, Color.orange) }, "SVG QR"),
        PredefinedConfiguration({
          val mapMarker = SvgPaintableProviders.mapMarker.get(Size.PX_120, Color.silver, alignmentPoint = Coordinates(-60.0, -120.0))
          val warning = SvgPaintableProviders.warning.get(mapMarker.size.times(0.43), Color.red)

          CombinedPaintable(mapMarker, warning, Distance(-60.0, -mapMarker.size.width * 0.2 - 120.0))
        }, "Combined"),
        PredefinedConfiguration({ CirclePaintable(Color.orangered, 72.0) }, "Circle"),
        PredefinedConfiguration({
          CirclePointPainter(true, true).also {
            it.fill = Color.green
            it.lineWidth = 24.0
            it.pointSize = 40.0
          }
        }, "CirclePoint"),
        PredefinedConfiguration({ LocalResourcePaintable("cable.png") }, "LocalResource"),
        PredefinedConfiguration({ NeckarItFlowPaintable(Size.PX_120) }, "NECKAR.IT Flow"),

        PredefinedConfiguration({ Button({ _: ButtonState -> SvgPaintableProviders.ok.get(Size.PX_30, Color.red) }.toButtonPainter(), 43.0, 20.0) }, "Button"),
        PredefinedConfiguration({ CircularChartPaintable(DoublesProvider.forDoubles(0.1, 0.2, 0.3, 0.4)) }, "CircularChart"),
        PredefinedConfiguration({ GaugePaintable({ ValueRange.percentage }, { 0.75 }, Size.PX_120) }, "Gauge"),
        PredefinedConfiguration({ com.meistercharts.charts.OverflowIndicatorPainter.topIndicatorTriangle(Color.darkgray, Color.white) }, "Top Indicator"),
        PredefinedConfiguration({ com.meistercharts.charts.OverflowIndicatorPainter.bottomIndicatorTriangle(Color.darkgray, Color.white) }, "Bottom Indicator"),
        PredefinedConfiguration({ com.meistercharts.charts.OverflowIndicatorPainter.leftIndicatorTriangle(Color.darkgray, Color.white) }, "Left Indicator"),
        PredefinedConfiguration({ com.meistercharts.charts.OverflowIndicatorPainter.rightIndicatorTriangle(Color.darkgray, Color.white) }, "Right Indicator"),
        PredefinedConfiguration({ com.meistercharts.charts.OverflowIndicatorPainter.rightIndicatorTriangle(Color.darkgray, Color.white, arrowHeadLength = Double.NaN) }, "Overflow Indicator with NaN"),
        PredefinedConfiguration({ RectanglePaintable(120.0, 84.0, Color.green) }, "Rectangle"),
        PredefinedConfiguration({ StackedBarPaintable(width = 15.0, height = 120.0) }, "Stacked Bar"),
        PredefinedConfiguration({
          StackedBarPaintable(
            data = StackedBarPaintable.Data(
              valuesProvider = DoublesProvider.Companion.forDoubles(-1.0, -3.0, 5.0, 7.0),
              valueRange = ValueRange.linear(-10.0, 20.0)
            ),
            width = 15.0,
            height = 120.0
          )
        }, "Stacked Bar - negative"),
        PredefinedConfiguration({ StackedBarWithLabelPaintable(width = 15.0, height = 120.0) }, "Stacked Bar + Label"),
        PredefinedConfiguration({ SymbolAndImagePaintable(CirclePaintable(Color.blue, 15.0), Icons.error()) }, "Symbol + Image"),
        PredefinedConfiguration({ SymbolAndTextKeyPaintable(CirclePaintable(Color.blue, 15.0), TextKey.simple("Hello World")) }, "Symbol + Text"),
        PredefinedConfiguration({ TransparentPaintable(Size.PX_120) }, "Transparent"),
        PredefinedConfiguration({ TankPaintable() }, "Tank"),
        PredefinedConfiguration({ LocalResourcePaintable("tank/sensor-ultrasonic.png", Size(272.0, 320.0).times(0.5), Coordinates(-272.0 * 0.5 * 0.5, -286.0 * 0.5)) }, "tank/sensor-ultrasonic.png"),
        PredefinedConfiguration({ LocalResourcePaintable("tank/sensor-vibration.png", Size(273.0, 818.0).times(0.5), Coordinates(-273.0 * 0.5 * 0.5, -286.0 * 0.5)) }, "tank/sensor-vibration.png"),

        //No size!
        PredefinedConfiguration({ LocalResourcePaintable("solar/panel-vertical.png") }, "solar/panel-vertical.png"),
      )
    }
  }
}
