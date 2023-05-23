package it.neckar.open.unit.time

import it.neckar.open.unit.Name
import it.neckar.open.unit.Symbol
import it.neckar.open.unit.Unit
import it.neckar.open.unit.other.Relative
import it.neckar.open.unit.prefix.milli
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.s

/**
 * Represents a *relative* time
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
@Time
@Symbol(ms.SYMBOL)
@Name("relative milliseconds")
@milli(s::class)
@Relative
annotation class RelativeMillis {
  companion object {
    const val SYMBOL: String = "ms"
  }
}

