package it.neckar.open.annotations

/**
 * Documents a former name for a class, property, function, etc.
 *
 * This is only for documentation purposes.
 * Might later be used for deserialization compatibility.
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
annotation class FormerName(val name: String)
