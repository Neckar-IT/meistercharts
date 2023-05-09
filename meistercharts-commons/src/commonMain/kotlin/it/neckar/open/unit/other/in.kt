/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.neckar.open.unit.other

import it.neckar.open.unit.AlternativeSymbols
import it.neckar.open.unit.Definition
import it.neckar.open.unit.DerivedUnit
import it.neckar.open.unit.Name
import it.neckar.open.unit.Symbol
import it.neckar.open.unit.Unit
import it.neckar.open.unit.quantity.Length
import it.neckar.open.unit.si.m

/**
 * Inch
 *
 */
@Suppress("ClassName")
@Retention(AnnotationRetention.SOURCE)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.ANNOTATION_CLASS,
  AnnotationTarget.TYPE_PARAMETER,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD,
  AnnotationTarget.LOCAL_VARIABLE,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.CONSTRUCTOR,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.TYPE,
  AnnotationTarget.EXPRESSION,
  AnnotationTarget.FILE,
  AnnotationTarget.TYPEALIAS
)
@MustBeDocumented
@Length
@Name("inch")
@Symbol(`in`.SYMBOL)
@AlternativeSymbols(`in`.ALTERNATIVE_SYMBOL, `in`.SYMBOL_SAFE)
@Unit
@DerivedUnit(m::class)
@Definition("0.0254 m", "12 inches = 1 foot")
annotation class `in` {
  companion object {
    /**
     * 1 ft = FEET_RATIO in
     */
    const val FEET_RATIO: Int = 12

    /**
     * Conversion to mm: n * MM_RATIO
     */
    const val MM_RATIO: Double = 25.4


    const val SYMBOL: String = "″"
    /**
     * This is a "safe" symbol - that is not correct!
     */

    const val SYMBOL_SAFE: String = "\""

    const val ALTERNATIVE_SYMBOL: String = "in"
  }
}
