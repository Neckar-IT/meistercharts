package com.meistercharts.fx.binding

import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue

/**
 * Contains extension methods that allow binding of observable objects to JavaFX properties
 */

/**
 * The key that is used to store the observable value this object is bound to
 */
const val KEY_FX_BOUND_TO: String = "fxBoundTo"
/**
 * The key that is used to store the JavaFX *object* property that has been created for this observable object
 */
const val KEY_FX_OBJECT_PROPERTY: String = "fxObjectProperty"
/**
 * The key that is used to store the JavaFX *double* property that has been created for this observable object
 */
const val KEY_FX_DOUBLE_PROPERTY: String = "fxDoubleProperty"

/**
 * Binds this object to the given binding
 */
fun <T> ObservableObject<T>.bind(observableValue: ObservableValue<T>) {
  observableValue.addListener { _, _, newValue ->
    value = newValue
  }
  value = observableValue.value

  //Register
  addDependentObject(KEY_FX_BOUND_TO, observableValue)
}

/**
 * Converts an observable object to a property
 */
fun ObservableObject<Double>.toJavaFx(): DoubleProperty {
  getDependentObject(KEY_FX_DOUBLE_PROPERTY)?.let {
    @Suppress("UNCHECKED_CAST")
    return it as DoubleProperty
  }

  val fxProperty = SimpleDoubleProperty(value)
  consume(false) { newValue -> fxProperty.value = newValue }
  fxProperty.addListener { _, _, newValue ->
    value = newValue.toDouble()
  }

  addDependentObject(KEY_FX_DOUBLE_PROPERTY, fxProperty)
  return fxProperty
}

/**
 * Returns a *READ ONLY* property
 */
fun ReadOnlyObservableObject<Double>.toJavaFx(): ReadOnlyDoubleProperty {
  getDependentObject(KEY_FX_DOUBLE_PROPERTY)?.let {
    @Suppress("UNCHECKED_CAST")
    return it as DoubleProperty
  }

  val fxProperty = SimpleDoubleProperty(value)
  consume(false) { newValue -> fxProperty.value = newValue }

  addDependentObject(KEY_FX_DOUBLE_PROPERTY, fxProperty)
  return fxProperty
}

fun <T> ObservableObject<T>.toJavaFx(): ObjectProperty<T> {
  getDependentObject(KEY_FX_OBJECT_PROPERTY)?.let {
    @Suppress("UNCHECKED_CAST")
    return it as ObjectProperty<T>
  }

  val fxProperty = SimpleObjectProperty(value)
  consume { newValue -> fxProperty.value = newValue }
  fxProperty.addListener { _, _, newValue ->
    value = newValue
  }

  addDependentObject(KEY_FX_OBJECT_PROPERTY, fxProperty)
  return fxProperty
}

fun <T> ReadOnlyObservableObject<T>.toJavaFx(): ReadOnlyObjectProperty<T> {
  getDependentObject(KEY_FX_OBJECT_PROPERTY)?.let {
    @Suppress("UNCHECKED_CAST")
    return it as ObjectProperty<T>
  }

  val fxProperty = SimpleObjectProperty(value)
  consume { newValue -> fxProperty.value = newValue }

  addDependentObject(KEY_FX_OBJECT_PROPERTY, fxProperty)
  return fxProperty
}

/**
 * Binds this observable object to a JavaFX property
 */
fun <T> ObservableObject<in T>.bindToJavaFx(fxProperty: ObservableValue<out T>) {
  fxProperty.addListener { _, _, newValue ->
    value = newValue
  }

  value = fxProperty.value
}

fun ObservableDouble.bindToJavaFx(fxProperty: ObservableDoubleValue) {
  fxProperty.addListener { _, _, newValue ->
    value = newValue.toDouble()
  }

  value = fxProperty.value.toDouble()
}

fun ObservableBoolean.bindToJavaFx(fxProperty: ObservableBooleanValue) {
  fxProperty.addListener { _, _, newValue ->
    value = newValue
  }

  value = fxProperty.value
}
