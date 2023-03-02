package com.meistercharts.algorithms

import it.neckar.open.annotations.CreatesObjects
import it.neckar.open.kotlin.lang.DoubleMapFunction
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.mapped
import it.neckar.open.formatting.CachedNumberFormat


/**
 * Formats the values provided by the doubles provider
 */
@CreatesObjects
fun DoublesProvider.formatted(valueFormat: () -> CachedNumberFormat): SizedProvider<String> {
  return mapped(DoubleMapFunction { value -> valueFormat().format(value) })
}
