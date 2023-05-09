package it.neckar.open.unit.si

import it.neckar.open.unit.Name
import it.neckar.open.unit.Symbol
import it.neckar.open.unit.Unit
import it.neckar.open.unit.prefix.deci
import it.neckar.open.unit.quantity.Speed

/**
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
@Suppress("ClassName")
@Unit
@Speed
@Name("deciliter per minute")
@Symbol("0.1 l/min")
@deci(L_min::class)
annotation class dl_min


