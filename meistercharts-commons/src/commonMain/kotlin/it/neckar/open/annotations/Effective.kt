package it.neckar.open.annotations

/**
 * Represents an effective value, e.g., an effective date or price. Or maybe a processed URL.
 *
 * The value should not be used directly - but is for documentation purposes onlyi
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
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Effective(val value: String)
