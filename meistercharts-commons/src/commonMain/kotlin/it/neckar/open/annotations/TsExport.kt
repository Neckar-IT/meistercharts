package it.neckar.open.annotations

/**
 * An annotation to mark a value class to be exported to Typescript
 * As value classes cannot be annotated with @JsExport, this annotation is used to export them to Typescript
 * Technically other classes would also be collected with this annotation, but this is not recommended
 *
 * Can be used in JVM projects were @JsExport is not available
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class TsExport(val value: String = "")
