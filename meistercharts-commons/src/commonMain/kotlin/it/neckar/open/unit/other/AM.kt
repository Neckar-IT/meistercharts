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
import it.neckar.open.unit.Name
import it.neckar.open.unit.Symbol
import it.neckar.open.unit.Unit
import it.neckar.open.unit.quantity.Length

/**
 * Constants:
 * AM0: air mass zero
 * AM1: zenith at sea level
 * AM1.5: 48°
 *
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
@Unit
@Length
@Name("airmass")
@Symbol(AM.SYMBOL)
@Definition("optical path length through Earth's atmosphere", "relative to path length to that at the zenith at sea level")
annotation class AM {
  companion object {
    const val SYMBOL: String = "AM"
  }

}
