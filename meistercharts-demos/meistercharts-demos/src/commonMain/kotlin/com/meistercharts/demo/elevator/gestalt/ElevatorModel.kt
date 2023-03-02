package com.meistercharts.demo.elevator.gestalt

import com.meistercharts.annotations.Domain
import it.neckar.open.observable.ObservableInt
import it.neckar.open.observable.ObservableObject
import kotlin.math.roundToInt

/**
 * Marks story values
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.LOCAL_VARIABLE)
@Domain
annotation class Floor {

}

/**
 * Model for the elevator
 */
class ElevatorModel {
  var numberOfElevators: Int = 1
  var numberOfWaitingRooms: Int = 3


  val floorRange: FloorRange = FloorRange(5)

  /**
   * Returns the number of the top most floor
   */
  val topMostFloor: @Floor Int
    get() = floorRange.numberOfFloors - 1

  /**
   * The current location of the elevator (in floors)
   */
  var elevatorLocation: @Floor Double = 0.0

  /**
   * The current target for the elevator
   */
  val elevatorTargetProperty: @Floor ObservableInt = ObservableInt(0).also {
    it.consume {
      movementDirection = if ((elevatorTarget - elevatorLocation) > 0) MovementDirection.Upwards else MovementDirection.Downwards
    }
  }
  var elevatorTarget: @Floor Int by elevatorTargetProperty

  var movementDirection: MovementDirection = MovementDirection.Upwards

  /**
   * Contains the floor buttons that have requested the elevator
   */
  val requestedFloorsProperty: ObservableObject<Set<@Floor Int>> = ObservableObject(emptySet<Int>())
  var requestedFloors: Set<@Floor Int> by requestedFloorsProperty

  fun addRequestedFloor(requestedFloor: Int) {
    requestedFloors = requestedFloors.toMutableSet().also { it.add(requestedFloor) }
  }

  fun removeRequestedFloor(requestedFloor: Int) {
    requestedFloors = requestedFloors.toMutableSet().also { it.remove(requestedFloor) }
  }

  /**
   * Returns the next scheduled floor
   */
  val nextScheduledFloor: @Floor Int?
    get() {
      if (requestedFloors.isEmpty()) {
        return null
      }

      return when (movementDirection) {
        MovementDirection.Upwards -> {
          findRequestedUpwards() ?: findRequestedDownwards()
        }
        MovementDirection.Downwards -> {
          findRequestedDownwards() ?: findRequestedUpwards()
        }
      }
    }

  private fun findRequestedUpwards(): Int? {
    for (possibleNext in (elevatorLocation.roundToInt() + 1)..topMostFloor) {
      if (requestedFloors.contains(possibleNext)) {
        return possibleNext
      }
    }

    return null
  }

  private fun findRequestedDownwards(): Int? {
    for (possibleNext in (elevatorLocation.roundToInt() - 1).downTo(0)) {
      if (requestedFloors.contains(possibleNext)) {
        return possibleNext
      }
    }

    return null
  }
}

/**
 * The movement direction of the elevator
 */
enum class MovementDirection {
  Upwards,
  Downwards
}
