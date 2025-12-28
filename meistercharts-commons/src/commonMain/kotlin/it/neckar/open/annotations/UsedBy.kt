package it.neckar.open.annotations

/**
 * Documents which clients/projects use this endpoint, function, or class.
 *
 * This annotation helps track dependencies and usage patterns across projects.
 *
 * @param consumer A descriptive identifier of the consumer (e.g., "mea.neckar.it", "AutoMergeClient")
 * @param explanation Explains how/why the consumer uses this endpoint
 */
@Repeatable
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class UsedBy(
  val consumer: String,
  val explanation: String = "",
)
