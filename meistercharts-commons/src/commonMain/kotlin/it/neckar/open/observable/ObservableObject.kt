/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.observable

import it.neckar.open.dispose.Disposable
import kotlin.reflect.KProperty

/**
 * An observable object that contains a value and can be observed
 */
open class ObservableObject<T>(initValue: T) : DefaultObservable<T>(), DisposableReadOnlyObservableObject<T> {
  /**
   * The current value
   */
  override var value: T = initValue
    set(value) {
      if (calledFromBind.not()) {
        requireNotBound()
      }

      val oldValue = field
      field = value
      notifyListenersIfChanged(oldValue, value)
    }

  /**
   * Sets the value - used for delegation to a var:
   * ```
   * val nameProperty = ObservableObject("foo")
   * var name by nameProperty
   * ```
   */
  operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
    this.value = value
  }

  /**
   * Required for compatibility to JavaFX properties
   */
  fun get(): T {
    return value
  }

  /**
   * Binds this [ObservableObject] to [other] and vice versa.
   *
   * Copies the value from [other] to this initially.
   *
   * Both upstream subscriptions are registered on this observable, so calling [dispose] on this
   * instance releases both listeners.
   *
   * For a unidirectional binding see [bind]
   */
  fun bindBidirectional(other: ObservableObject<T>) {
    //Copy value from this to other
    addUpstreamSubscription(consume { newValue -> other.value = newValue })
    addUpstreamSubscription(other.consumeImmediately { newValue -> this.value = newValue })
  }

  fun <R> bindBidirectional(
    other: ObservableObject<R>,
    converterForward: (newValueToConvert: T, oldConvertedValue: R) -> R,
    converterBack: (newValueToConvert: R, oldConvertedValue: T) -> T,
  ) {
    return bindBidirectionalMapped(other, converterForward, converterBack)
  }

  /**
   * Binds two objects bidirectional - using converters.
   *
   * Assigns the value of the other observable to this initially.
   *
   * Both upstream subscriptions are registered on this observable, so calling [dispose] on this
   * instance releases both listeners.
   *
   * ATTENTION: The converter must work bidirectional - they must return objects that are equal to each other
   * @param R the other type
   */
  fun <R> bindBidirectionalMapped(
    other: ObservableObject<R>,
    converterForward: (newValueToConvert: T, oldConvertedValue: R) -> R,
    converterBack: (newValueToConvert: R, oldConvertedValue: T) -> T,
  ) {
    //Copy value from this to other
    var updating = false

    addUpstreamSubscription(consume { newValue ->
      if (updating.not()) {
        updating = true
        try {
          other.value = converterForward(newValue, other.value)
        } finally {
          updating = false
        }
      }
    })
    addUpstreamSubscription(other.consumeImmediately { newValue ->
      if (updating.not()) {
        updating = true
        try {
          this.value = converterBack(newValue, this.value)
        } finally {
          updating = false
        }
      }
    })
  }

  /**
   * Is set to true if the value is set from a bound value
   */
  private var calledFromBind: Boolean = false

  /**
   * Binds this [ObservableObject] to [other].
   *
   * Copies the value from [other] to this initially.
   *
   * The upstream subscription is registered on this observable, so calling [dispose] on this
   * instance releases the listener on [other].
   *
   * For a bidirectional binding see [bindBidirectional]
   */
  fun bind(other: ReadOnlyObservableObject<T>) {
    requireNotBound()
    addUpstreamSubscription(other.consumeImmediately { newValue ->
      updateFromBinding(newValue)
    })
    isBound = true
  }

  /**
   * Is set to true if this observable is bound to another observable (*not* bidirectional)
   */
  var isBound: Boolean = false
    private set

  /**
   * Calls the given function with the new value - from a binding!
   */
  fun updateFromBinding(newValue: T) {
    calledFromBind = true
    try {
      this.value = newValue
    } finally {
      calledFromBind = false
    }
  }

  /**
   * Marks this observable as bound (*not* bidirectional)
   */
  fun markAsBound() {
    requireNotBound()
    isBound = true
  }

  private fun requireNotBound() {
    check(isBound.not()) {
      "This observable is bound to another observable"
    }
  }

  override fun toString(): String {
    return "ObservableObject [value: $value]"
  }

  /**
   * Gets the values and applies the new value returned by the lambda
   */
  inline fun getAndSet(function: (oldValue: T) -> T) {
    value = function(value)
  }

  /**
   * Sets the value if it is different from the current value.
   * Calls the callback
   */
  fun setIfDifferent(newValue: T, onChange: () -> Unit) {
    if (value != newValue) {
      value = newValue
      onChange()
    }
  }
}

/**
 * Connects multiple observables using and
 */
fun List<ReadOnlyObservableObject<Boolean>>.and(): ReadOnlyObservableObject<Boolean> {
  require(isNotEmpty()) {
    "and must not be called on empty list"
  }
  return reduce {
    it.reduce { bool1, bool2 ->
      bool1 && bool2
    }
  }
}

/**
 * Connects multiple observables using or
 */
fun List<ReadOnlyObservableObject<Boolean>>.or(): ReadOnlyObservableObject<Boolean> {
  require(isNotEmpty()) {
    "or must not be called on empty list"
  }
  return reduce {
    it.reduce { bool1, bool2 ->
      bool1 || bool2
    }
  }
}

/**
 * Clears the value
 */
fun <T> ObservableObject<T?>.clear() {
  this.value = null
}
