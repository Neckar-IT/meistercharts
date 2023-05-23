package it.neckar.open.annotations

/**
 * Marks variables / methods that are only valid within the context of a paint method
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class PaintContext
