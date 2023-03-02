package demo

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.EmptyLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.canvas.FixedContentAreaWidth
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Size
import com.meistercharts.model.Vicinity

fun createBuilder(): MeisterChartBuilder {
  throw UnsupportedOperationException("Not implemented")
}


/**
 * Shows how the content area can be bound
 */
fun bindContentAreaSize() {
  val meisterChartBuilder: MeisterChartBuilder = createBuilder()

  // tag::bindContentAreaSize[]

  //Content area has the same size as the window
  meisterChartBuilder.contentAreaSizingStrategy = BindContentAreaSize2ContentViewport() //<1>

  //Content area has a fixed width
  //Height is bound to the window height
  meisterChartBuilder.contentAreaSizingStrategy = FixedContentAreaWidth(1000.0) //<2>

  //Content area has a fixed size
  meisterChartBuilder.contentAreaSizingStrategy = FixedContentAreaSize(Size(1024.0, 768.0)) //<3>
  // end::bindContentAreaSize[]
}


/**
 * Shows how the content area can be bound for a bar chart
 */
fun barChartContentAreaSizeBinding() {
  val meisterChartBuilder: MeisterChartBuilder = createBuilder()

  // tag::barChartContentAreaSizeBinding[]

  //Content area has the same size as the window
  meisterChartBuilder.contentAreaSizingStrategy = BindContentAreaSize2ContentViewport() //<1>

  //Reset to default on resize - zooming and panning not supported
  meisterChartBuilder.configure {
    chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize //<2>
  }

  //On reset use insets
  meisterChartBuilder.zoomAndTranslationDefaults {
    FittingWithMargin(Insets(10.0, 70.0, 25.0, 25.0)) //<3>
  }

  //end::barChartContentAreaSizeBinding[]
}


/**
 * Shows how the value axis can be placed on the edge of the content area
 */
fun valueAxisOnContentAreaBounds() {
  val meisterChartBuilder: MeisterChartBuilder = createBuilder()

  // tag::valueAxisOnContentAreaBounds[]

  //Content area has the same size as the window
  meisterChartBuilder.contentAreaSizingStrategy = BindContentAreaSize2ContentViewport() //<1>

  val passpartoutMargin = Insets(20.0, 10.0, 80.0, 120.0)
  meisterChartBuilder.zoomAndTranslationDefaults {
    FittingWithMargin(passpartoutMargin) //<2>
  }

  meisterChartBuilder.configure {
    //layers.add(...) //add the content layer
    //Clip the content area layer
    val contentLayer = EmptyLayer //the real content layer is initialized

    layers.addLayer(contentLayer.clipped(passpartoutMargin)) //<3>

    layers.addLayer(ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { ValueRange.linear(0.0, 123.0) })) {
      titleProvider = { _, _ -> "The Value Axis [m²/h]" }
      side = Side.Left  //Other side work exactly the same
      tickOrientation = Vicinity.Outside //<4>
      size = passpartoutMargin.left - margin.left //<5>
      //size = passpartoutMargin.bottom - margin.left //for horizontal
    })

  }
  //end::valueAxisOnContentAreaBounds[]
}

