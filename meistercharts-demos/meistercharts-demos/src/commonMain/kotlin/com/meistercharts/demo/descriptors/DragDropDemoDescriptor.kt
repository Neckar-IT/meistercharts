package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableSizeSeparate
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import com.meistercharts.model.within
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.events.gesture.CanvasDragSupport
import com.meistercharts.events.gesture.connectedMouseEventHandler
import com.meistercharts.events.gesture.connectedTouchEventHandler
import it.neckar.open.unit.si.ms

class DragDropDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drag'n'Drop"

  //language=HTML
  override val description: String = """<h3>Demo for Drag'n'Drop</h3>
    |The blue box can be dragged around. While dragging the box changes its color to red.
    |
    |<h4>"Other" mouse events</h4>
    |Mouse events that are not consumed by the dragging, are painted on the canvas.
    |
  """.trimMargin()

  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          //Visualizes all mouse events that "come" through
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
              val maxEventsCount = 15

              override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onClick(event, chartSupport)
              }

              override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onDown(event, chartSupport)
              }

              override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onUp(event, chartSupport)
              }

              override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onDoubleClick(event, chartSupport)
              }

              override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onMove(event, chartSupport)
              }

              override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onDrag(event, chartSupport)
              }

              override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
                lastEvents.add(event)
                lastEvents.deleteFromStartUntilMaxSize(maxEventsCount)
                markAsDirty()
                return super.onWheel(event, chartSupport)
              }
            }


            var lastEvents: MutableList<MouseEvent> = mutableListOf()

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              if (lastEvents.isNotEmpty()) {
                gc.translateToCenter()
                gc.fillText("Other mouse events:", 0.0, 0.0, Direction.BottomLeft, 10.0, 10.0)

                lastEvents.fastForEach {
                  gc.translate(0.0, 20.0)
                  gc.fillText(it.toString(), 0.0, 0.0, Direction.BottomLeft, 10.0, 10.0)
                }
              }
            }
          })

          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val canvasDragSupport = CanvasDragSupport().also {
              it.handle(object : CanvasDragSupport.Handler {
                override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
                  return location.within(rectangleLocation, size)
                }

                override fun onDrag(source: CanvasDragSupport, @Window location: Coordinates, @Zoomed distance: Distance, @ms deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
                  rectangleLocation = rectangleLocation.plus(distance)
                  markAsDirty()
                  return EventConsumption.Consumed
                }

                override fun onFinish(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): EventConsumption {
                  markAsDirty()
                  return super.onFinish(source, location, chartSupport)
                }
              })
            }

            override val mouseEventHandler: CanvasMouseEventHandler = canvasDragSupport.connectedMouseEventHandler()
            override val touchEventHandler: CanvasTouchEventHandler = canvasDragSupport.connectedTouchEventHandler(1)

            var rectangleLocation: @Window Coordinates = Coordinates.origin

            var size: @Zoomed Size = Size(150.0, 150.0)

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              if (canvasDragSupport.dragging) {
                gc.fill(Color.red)
              } else {
                gc.fill(Color.blue)
              }

              gc.fillRect(rectangleLocation, size)
            }
          }

          layers.addLayer(
            myLayer
          )

          configurableSizeSeparate("Size", myLayer::size) {
            min = -200.0
            max = 200.0
          }
        }
      }
    }
  }
}

