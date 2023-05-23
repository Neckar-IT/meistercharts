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

import it.neckar.open.unit.Unit
import it.neckar.open.unit.quantity.DotDensity
import it.neckar.open.unit.Definition
import it.neckar.open.unit.DerivedUnit
import it.neckar.open.unit.Name
import it.neckar.open.unit.Symbol
import it.neckar.open.unit.si.cm

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
@Suppress("ClassName")
@Unit
@DotDensity
@Name("Dots per Inch")
@Symbol(dpi.SYMBOL)
@Definition("the number of individual dots that can be placed in a line within the span of 1 inch (2.54 cm)")
@DerivedUnit(cm::class)
annotation class dpi {
  companion object {
    const val SYMBOL: String = "dpi"
  }

}
