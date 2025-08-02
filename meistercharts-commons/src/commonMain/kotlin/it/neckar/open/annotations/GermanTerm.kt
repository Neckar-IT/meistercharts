package it.neckar.open.annotations

/**
 * Documents the German technical term for a class or property.
 */
@Repeatable
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.TYPE,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class GermanTerm(val value: String)
