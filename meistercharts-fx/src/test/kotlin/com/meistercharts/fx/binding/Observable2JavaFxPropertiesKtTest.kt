package com.meistercharts.fx.binding

import assertk.*
import assertk.assertions.*
import it.neckar.open.observable.ObservableInt
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.toNumber
import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Test

/**
 */
class Observable2JavaFxPropertiesKtTest {
  @Test
  internal fun testToNumber() {
    val o = ObservableInt(17)

    val number = o.toNumber()
    assertThat(number.value.toDouble()).isEqualTo(17.0)

    o.value = 27
    assertThat(number.value.toDouble()).isEqualTo(27.0)

    number.value = 12.2
    assertThat(number.value.toDouble()).isEqualTo(12.0)
    assertThat(o.value).isEqualTo(12)
  }

  @Test
  internal fun testPropRepeat() {
    val observableObject = ObservableObject("a")
    assertThat(observableObject.toJavaFx()).isSameAs(observableObject.toJavaFx())
  }

  @Test
  internal fun testBindings() {
    val observableObject = ObservableObject("a")
    val fxProperty = SimpleStringProperty("asdf")

    assertThat(observableObject.value).isEqualTo("a")
    observableObject.bind(fxProperty)
    assertThat(observableObject.value).isEqualTo("asdf")

    fxProperty.set("foo")
    assertThat(observableObject.value).isEqualTo("foo")
  }

  @Test
  internal fun testToFx() {
    val observableObject = ObservableObject("a")
    val fxProperty = observableObject.toJavaFx()


    assertThat(observableObject.value).isEqualTo("a")
    assertThat(fxProperty.value).isEqualTo("a")

    fxProperty.set("foo")
    assertThat(observableObject.value).isEqualTo("foo")
    assertThat(fxProperty.value).isEqualTo("foo")

    observableObject.value = "bla"
    assertThat(observableObject.value).isEqualTo("bla")
    assertThat(fxProperty.value).isEqualTo("bla")
  }

  @Test
  internal fun testBidirectionalDirection() {
    //Check with JavaFX
    val fxProp0 = SimpleStringProperty("foo")
    val fxProp1 = SimpleStringProperty("bar")

    fxProp0.bindBidirectional(fxProp1)

    assertThat(fxProp0.value).isEqualTo("bar")
    assertThat(fxProp1.value).isEqualTo("bar")

    //Same with ObservableObject
    val observableObject0 = ObservableObject("foo")
    val observableObject1 = ObservableObject("bar")

    observableObject0.bindBidirectional(observableObject1)
    assertThat(observableObject0.value).isEqualTo("bar")
    assertThat(observableObject1.value).isEqualTo("bar")
  }
}
