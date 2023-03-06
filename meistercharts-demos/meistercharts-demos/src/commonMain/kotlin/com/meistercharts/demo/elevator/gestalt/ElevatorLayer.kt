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
package com.meistercharts.demo.elevator.gestalt

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.ContentArea
import it.neckar.open.unit.number.Positive
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.join
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.open.unit.other.px

class ElevatorLayer(
  val model: ElevatorModel,
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  val roofPaintable: RoofPaintable = RoofPaintable(model::numberOfElevators, model::numberOfWaitingRooms)
  val floorPaintable: FloorPaintable = FloorPaintable(model::numberOfElevators, model::numberOfWaitingRooms)
  val linesPaintable: LinesPaintable = LinesPaintable(model::numberOfElevators, model::numberOfWaitingRooms)
  val elevatorPaintable: ElevatorPaintable = ElevatorPaintable(model::numberOfElevators, model::numberOfWaitingRooms)
  val elevatorCoverPaintable: ElevatorCoverPaintable = ElevatorCoverPaintable(model::numberOfElevators, model::numberOfWaitingRooms)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    val numberOfFloors = model.floorRange.numberOfFloors

    //move to origin of @ContentArea
    gc.translate(chartCalculator.contentAreaRelative2windowX(0.0), chartCalculator.contentAreaRelative2windowY(0.0))

    //Paint the stories
    numberOfFloors.fastFor { storyIndex ->
      @Window val floorBottom = chartCalculator.domainRelative2zoomedY(model.floorRange.floorBottom2DomainRelative(storyIndex.toDouble()))

      gc.saved {
        gc.translate(0.0, floorBottom)
        floorPaintable.paint(paintingContext, 0.0, 0.0)
      }
    }

    //Paint the elevator
    gc.saved {
      @Window val elevatorBottomLocationWindow = chartCalculator.domainRelative2zoomedY(model.floorRange.floorBottom2DomainRelative(model.elevatorLocation))
      gc.translate(0.0, elevatorBottomLocationWindow)

      elevatorPaintable.paint(paintingContext, 0.0, 0.0)
    }

    //Paint the elevator covers
    numberOfFloors.fastFor { storyIndex ->
      @Window val floorBottom = chartCalculator.domainRelative2zoomedY(model.floorRange.floorBottom2DomainRelative(storyIndex.toDouble()))

      gc.saved {
        gc.translate(0.0, floorBottom)
        elevatorCoverPaintable.paint(paintingContext, 0.0, 0.0)
      }
    }

    //Paint the roof
    gc.saved {
      val roofBottom = chartCalculator.domainRelative2zoomedY(model.floorRange.floorBottom2DomainRelative(numberOfFloors.toDouble()))
      gc.translate(0.0, roofBottom)
      roofPaintable.paint(paintingContext, 0.0, 0.0)
    }

    //Paint the lines for the floors
    numberOfFloors.fastFor { storyIndex ->
      @Window val floorBottom = chartCalculator.domainRelative2zoomedY(model.floorRange.floorBottom2DomainRelative(storyIndex.toDouble()))

      gc.saved {
        gc.translate(0.0, floorBottom)
        linesPaintable.paint(paintingContext, 0.0, 0.0)
      }
    }

    //Lines for the roof
    gc.saved {
      val roofBottom = chartCalculator.domainRelative2zoomedY(model.floorRange.floorBottom2DomainRelative(numberOfFloors.toDouble()))
      gc.translate(0.0, roofBottom)
      linesPaintable.paint(paintingContext, 0.0, 0.0)
    }
  }
}


abstract class BaseFloorPaintable(
  val numberOfElevatorsProvider: () -> @Positive Int,
  val numberOfWaitingRoomsProvider: () -> @Positive Int,
) : Paintable {
  abstract val left: LocalResourcePaintable?
  abstract val elevatorShaft: LocalResourcePaintable?
  abstract val interspace: LocalResourcePaintable?
  abstract val waitingRoom: LocalResourcePaintable?
  abstract val right: LocalResourcePaintable?

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    //TODO
    return Rectangle.zero
  }

  override fun paint(paintingContext: LayerPaintingContext, x: @Zoomed Double, y: @Zoomed Double) {
    val calculator = paintingContext.chartCalculator

    with(calculator) {

      left?.paintScaled(paintingContext, originLeft)

      val numberOfElevators = numberOfElevatorsProvider()
      numberOfElevators.join({
        interspace?.paintScaled(paintingContext, getRightSideOfElevator(it).toZoomed(calculator))
      }) {
        elevatorShaft?.paintScaled(paintingContext, getOriginElevator(it).toZoomed(calculator))
      }

      val numberOfWaitingRooms = numberOfWaitingRoomsProvider()
      numberOfWaitingRooms.fastFor {
        waitingRoom?.paintScaled(paintingContext, getOriginWaitingRoom(numberOfElevators, it).toZoomed(calculator))
      }

      right?.paintScaled(paintingContext, getOriginRight(numberOfElevators, numberOfWaitingRooms).toZoomed(calculator))
    }
  }

  val originLeft: @ContentArea Double = 0.0

  fun getOriginElevator(elevatorIndex: Int): @ContentArea Double {
    return (ElevatorResources.leftWidth + elevatorIndex * (ElevatorResources.elevatorShaftWidth + ElevatorResources.interspaceWidth)).toDouble()
  }

  fun getRightSideOfElevator(elevatorIndex: Int): @ContentArea Double {
    return (ElevatorResources.leftWidth + (elevatorIndex + 1) * ElevatorResources.elevatorShaftWidth + elevatorIndex * ElevatorResources.interspaceWidth).toDouble()
  }

  fun getOriginWaitingRoom(numberOfElevators: Int, waitingRoomIndex: Int): @ContentArea Double {
    return getRightSideOfElevator(numberOfElevators - 1) + ElevatorResources.waitingRoomWidth * waitingRoomIndex
  }

  fun getOriginRight(numberOfElevators: Int, numberOfWaitingRooms: Int): @ContentArea Double {
    return getOriginWaitingRoom(numberOfElevators, numberOfWaitingRooms)
  }

  fun getRightSide(numberOfElevators: Int, numberOfWaitingRooms: Int): @ContentArea Double {
    return getOriginWaitingRoom(numberOfElevators, numberOfWaitingRooms) + ElevatorResources.rightWidth
  }

  /**
   * Paints the resource
   */
  private fun LocalResourcePaintable.paintScaled(paintingContext: LayerPaintingContext, currentX: @Zoomed Double) {
    paintingContext.gc.saved {
      it.translate(currentX, 0.0)
      it.scale(paintingContext.chartState.zoomX, paintingContext.chartState.zoomY)
      paint(paintingContext, 0.0, 0.0)
    }
  }

  protected fun @ContentArea Double.toZoomed(chartCalculator: ChartCalculator): @Zoomed Double {
    return chartCalculator.contentArea2zoomedX(this)
  }
}

class ElevatorPaintable(
  numberOfElevatorsProvider: () -> @Positive Int,
  numberOfWaitingRoomsProvider: () -> @Positive Int,
) : BaseFloorPaintable(numberOfElevatorsProvider, numberOfWaitingRoomsProvider) {

  override val left: LocalResourcePaintable? = null
  override val elevatorShaft: LocalResourcePaintable? = ElevatorResources.elevator
  override val interspace: LocalResourcePaintable? = null
  override val waitingRoom: LocalResourcePaintable? = null
  override val right: LocalResourcePaintable? = null
}

/**
 * Paints the elevator cover
 */
class ElevatorCoverPaintable(
  numberOfElevatorsProvider: () -> @Positive Int,
  numberOfWaitingRoomsProvider: () -> @Positive Int,
) : BaseFloorPaintable(numberOfElevatorsProvider, numberOfWaitingRoomsProvider) {

  override val left: LocalResourcePaintable? = null
  override val elevatorShaft: LocalResourcePaintable? = ElevatorResources.elevatorCover
  override val interspace: LocalResourcePaintable? = null
  override val waitingRoom: LocalResourcePaintable? = null
  override val right: LocalResourcePaintable? = null
}

class LinesPaintable(
  numberOfElevatorsProvider: () -> @Positive Int,
  numberOfWaitingRoomsProvider: () -> @Positive Int,
) : BaseFloorPaintable(numberOfElevatorsProvider, numberOfWaitingRoomsProvider) {

  override val left: LocalResourcePaintable? = null
  override val elevatorShaft: LocalResourcePaintable? = null
  override val interspace: LocalResourcePaintable? = null
  override val waitingRoom: LocalResourcePaintable? = null
  override val right: LocalResourcePaintable? = null


  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    val numberOfElevators = numberOfElevatorsProvider()
    val numberOfWaitingRooms = numberOfWaitingRoomsProvider()

    val chartCalculator = paintingContext.chartCalculator

    @ContentArea val originRight = getOriginRight(numberOfElevators, numberOfWaitingRooms).toZoomed(chartCalculator)
    @ContentArea val rightSide = getRightSide(numberOfElevators, numberOfWaitingRooms).toZoomed(chartCalculator)

    //Paint the black lines
    gc.stroke(Color.black)

    //the horizontal line
    gc.strokeLine(0.0, 0.0, originRight, 0.0)
    //the line to the back right
    gc.strokeLine(originRight, 0.0, rightSide, -ElevatorResources.rightWidth.toDouble().toZoomed(chartCalculator))
  }
}

/**
 * Paints a single floor - alignment point is bottom left of the floor
 */
class FloorPaintable(
  numberOfElevatorsProvider: () -> @Positive Int,
  numberOfWaitingRoomsProvider: () -> @Positive Int,
) : BaseFloorPaintable(numberOfElevatorsProvider, numberOfWaitingRoomsProvider) {
  override val left: LocalResourcePaintable = ElevatorResources.floorLeft
  override val elevatorShaft: LocalResourcePaintable = ElevatorResources.floorElevatorShaft
  override val interspace: LocalResourcePaintable = ElevatorResources.floorInterspace
  override val waitingRoom: LocalResourcePaintable = ElevatorResources.floorWaitingRoom
  override val right: LocalResourcePaintable = ElevatorResources.floorRight
}

class RoofPaintable(
  numberOfElevatorsProvider: () -> @Positive Int,
  numberOfWaitingRoomsProvider: () -> @Positive Int,
) : BaseFloorPaintable(numberOfElevatorsProvider, numberOfWaitingRoomsProvider) {
  override val left: LocalResourcePaintable = ElevatorResources.roofLeft
  override val elevatorShaft: LocalResourcePaintable = ElevatorResources.roofElevatorShaft
  override val interspace: LocalResourcePaintable = ElevatorResources.roofInterspace
  override val waitingRoom: LocalResourcePaintable = ElevatorResources.roofWaitingRoom
  override val right: LocalResourcePaintable = ElevatorResources.roofRight
}

object ElevatorResources {
  const val leftWidth: @Zoomed Int = 56
  const val elevatorShaftWidth: @Zoomed Int = 112
  const val interspaceWidth: @Zoomed Int = 40
  const val waitingRoomWidth: @Zoomed Int = 150
  const val rightWidth: @Zoomed Int = 56

  val buttonSize: Size = Size(260.0 * 0.25, 208.0 * 0.25)

  /**
   * The additional height of the elevator - necessary because the elevator is not at the front of the building but a little bit behind.
   */
  const val elevatorAdditionalOffset: Int = -12
  val backgroundImage: LocalResourcePaintable = elevatorResource("elevator/background-only-hills.png", 7676, 4324)

  //Alignment point is bottom left of the story

  val elevator: LocalResourcePaintable = elevatorResource("elevator/elevator.png", elevatorShaftWidth, 212, -212 + elevatorAdditionalOffset)

  //Floor related paintables.
  val floorLeft: LocalResourcePaintable = elevatorResource("elevator/floor-left.png", leftWidth, 200)
  val floorLeftActive: LocalResourcePaintable = elevatorResource("elevator/floor-left.png", leftWidth, 200)

  val floorElevatorShaft: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-shaft.png", elevatorShaftWidth, 212)
  val floorElevatorCover: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-cover.png", elevatorShaftWidth, 212)
  val floorElevatorCoverActive: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-cover-active.png", elevatorShaftWidth, 212)

  val floorInterspace: LocalResourcePaintable = elevatorResource("elevator/floor-interspace.png", interspaceWidth, 200)
  val floorInterspaceActive: LocalResourcePaintable = elevatorResource("elevator/floor-interspace-active.png", interspaceWidth, 200)

  val floorWaitingRoom: LocalResourcePaintable = elevatorResource("elevator/floor-waiting-room.png", waitingRoomWidth, 200)
  val floorWaitingRoomActive: LocalResourcePaintable = elevatorResource("elevator/floor-waiting-room.png", waitingRoomWidth, 200)

  val floorRight: LocalResourcePaintable = elevatorResource("elevator/floor-right.png", rightWidth, 256)
  val floorRightActive: LocalResourcePaintable = elevatorResource("elevator/floor-right-active.png", rightWidth, 256)

  //Elevator cover
  val elevatorCover: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-cover.png", elevatorShaftWidth, 212)
  val elevatorCoverActive: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-cover-active.png", elevatorShaftWidth, 212)

  //Roof resources
  val roofLeft: LocalResourcePaintable = elevatorResource("elevator/roof-left.png", leftWidth, 56)
  val roofElevatorShaft: LocalResourcePaintable = elevatorResource("elevator/roof-elevator-shaft.png", elevatorShaftWidth, 56)
  val roofInterspace: LocalResourcePaintable = elevatorResource("elevator/roof-interspace.png", interspaceWidth, 56)
  val roofWaitingRoom: LocalResourcePaintable = elevatorResource("elevator/roof-waiting-room.png", waitingRoomWidth, 56)
  val roofRight: LocalResourcePaintable = elevatorResource("elevator/roof-right.png", rightWidth, 56)

  //Buttons
  val floorElevatorButton: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-button.png", buttonSize)
  val floorElevatorButtonActive: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-button-active.png", buttonSize)
  val floorElevatorButtonHover: LocalResourcePaintable = elevatorResource("elevator/floor-elevator-button-hover.png", buttonSize)
}

/**
 * Returns the resource
 * ATTENTION: The size is halfed!
 */
private fun elevatorResource(path: String, width: @px Int, height: @px Int, alignmentPointY: Int = -height): LocalResourcePaintable {
  return elevatorResource(path, Size(width, height), alignmentPointY.toDouble())
}

private fun elevatorResource(path: String, size: @px Size, alignmentPointY: Double = -size.height): LocalResourcePaintable {
  return LocalResourcePaintable(path, size, Coordinates.of(0.0, alignmentPointY))
}

