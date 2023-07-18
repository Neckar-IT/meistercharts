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
package com.meistercharts.provider

import it.neckar.open.annotations.CreatesObjects
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.kotlin.lang.DoubleMapFunction
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.mapped


/**
 * Formats the values provided by the doubles provider
 */
@CreatesObjects
fun DoublesProvider.formatted(valueFormat: () -> CachedNumberFormat): SizedProvider<String> {
  return mapped(DoubleMapFunction { value -> valueFormat().format(value) })
}
