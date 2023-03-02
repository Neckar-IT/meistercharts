package com.meistercharts.fx.binding

import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.SizedProvider
import javafx.beans.binding.ObjectBinding

/**
 *
 */

/**
 * Returns the current value of this object binding as doubles provider
 */
fun ObjectBinding<DoublesProvider>.asDoublesProvider(): DoublesProvider {
  return object : DoublesProvider {
    override fun size(): Int = value.size()

    override fun valueAt(index: Int): Double {
      return value.valueAt(index)
    }
  }
}

/**
 * Returns the current value of this object binding as sized provider
 */
fun <T> ObjectBinding<SizedProvider<T>>.asSizedProvider(): SizedProvider<T> {
  return object : SizedProvider<T> {
    override fun size(): Int = value.size()

    override fun valueAt(index: Int): T {
      return value.valueAt(index)
    }
  }
}

