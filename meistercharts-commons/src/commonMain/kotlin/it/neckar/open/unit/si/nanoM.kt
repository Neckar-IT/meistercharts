package it.neckar.open.unit.si


import it.neckar.open.unit.Name
import it.neckar.open.unit.Symbol
import it.neckar.open.unit.Unit
import it.neckar.open.unit.prefix.micro
import it.neckar.open.unit.prefix.nano
import it.neckar.open.unit.quantity.Length

/**
 * Nano meter
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
@Unit
@Length
@Symbol(nanoM.SYMBOL)
@Name("nanometer")
@nano(m::class)
annotation class nanoM {
  companion object {
    const val SYMBOL: String = "nm"
  }
}
