package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.events.gesture.MouseMovementSupport
import it.neckar.open.observable.ObservableObject
import org.junit.jupiter.api.Test

internal class MouseMovementSupportTest {
  @Test
  internal fun testMousePositionMovementsStuff() {
    val mousePositionObservable = ObservableObject<@Window Coordinates?>(null)
    val mouseMovementSupport = MouseMovementSupport(mousePositionObservable)

    val buttonBoundsObservable = ObservableObject(Rectangle.zero)

    var over = false
    var calledCount = 0
    mouseMovementSupport.mouseOver(buttonBoundsObservable) {
      over = it
      calledCount++
    }

    //is called once initially
    assertThat(calledCount).isEqualTo(1)

    assertThat(over).isFalse()

    mousePositionObservable.value = Coordinates(7.0, 10.0)
    assertThat(over).isFalse()
    assertThat(calledCount).isEqualTo(1)

    buttonBoundsObservable.value = Rectangle(Coordinates.origin, Size(10.0, 20.0))
    assertThat(over).isTrue()
    assertThat(calledCount).isEqualTo(2)


    //set mouse position off again
    mousePositionObservable.value = Coordinates(11.0, 10.0)
    assertThat(over).isFalse()
    assertThat(calledCount).isEqualTo(3)
    mousePositionObservable.value = null
    assertThat(over).isFalse()
    assertThat(calledCount).isEqualTo(3)

    mousePositionObservable.value = Coordinates(7.0, 10.0)
    assertThat(over).isTrue()
    assertThat(calledCount).isEqualTo(4)


    //Ensure no multiple events
    assertThat(calledCount).isEqualTo(4)
    mousePositionObservable.value = Coordinates(7.0, 10.0)
    assertThat(over).isTrue()
    mousePositionObservable.value = Coordinates(7.0, 9.0)
    assertThat(over).isTrue()
    mousePositionObservable.value = Coordinates(7.0, 8.0)
    assertThat(over).isTrue()

    assertThat(calledCount).isEqualTo(4)

    buttonBoundsObservable.value = Rectangle(Coordinates.origin, Size(10.0, 20.0))
    assertThat(calledCount).isEqualTo(4)
  }
}
