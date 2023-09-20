package it.neckar.open.annotations

/**
 * An annotation that indicates that the annotated element is intended for use only in tests,
 * and should not be used in production code.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class TestOnly(val value: String = "")
