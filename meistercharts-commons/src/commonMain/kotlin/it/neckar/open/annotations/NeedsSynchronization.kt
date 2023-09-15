package it.neckar.open.annotations

/**
 * Indicates that the annotated element must be synchronized when using in a multithreaded environment.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class NeedsSynchronization
