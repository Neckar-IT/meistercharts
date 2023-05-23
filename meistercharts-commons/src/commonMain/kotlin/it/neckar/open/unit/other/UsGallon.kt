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

import it.neckar.open.unit.Definition
import it.neckar.open.unit.DerivedUnit
import it.neckar.open.unit.Name
import it.neckar.open.unit.Symbol
import it.neckar.open.unit.Unit
import it.neckar.open.unit.quantity.Length
import it.neckar.open.unit.si.L

/**
 */
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
@Suppress("ClassName")
@Unit
@Length
@Name("US gallon")
@Symbol(UsGallon.SYMBOL)
@DerivedUnit(L::class)
@Definition("231 cubic inches", "3.785 411 784 litres")
annotation class UsGallon {
  companion object {
    /**
     * The ratio used to convert litres to US gallons
     *
     *  * UsGallon == Liter / US_GALLON_LITRE_RATIO
     *  * Lister = UsGallon * US_GALLON_LITRE_RATIO
     */
    const val US_GALLON_LITRE_RATIO: Double = 3.785_411_784

    /**
     * The "real" symbol
     */
    const val SYMBOL: String = "US gal"
  }
}
