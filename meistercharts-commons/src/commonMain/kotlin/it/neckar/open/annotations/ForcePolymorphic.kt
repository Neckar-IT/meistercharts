package it.neckar.open.annotations

/**
 * Used when serializing a subclass of a sealed interface as the sealed interface itself.
 * This forces the serializer to always include type information (even if there is only one possible implementation).
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
  AnnotationTarget.TYPE,
)
@MustBeDocumented
annotation class ForcePolymorphic
