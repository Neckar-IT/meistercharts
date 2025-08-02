package it.neckar.open.annotations

/**
 * Describes where and how this class or property is used in the UI.
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
annotation class UiUsage(val description: String)
