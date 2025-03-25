package it.neckar.open.annotations

/**
 * An annotation to mark a value class to be exported to TypeScript
 * As value classes cannot be annotated with @JsExport, this annotation is used to export them to TypeScript.
 * Technically, other classes would also be collected with this annotation, but this is not recommended.
 *
 * Can be used in JVM projects where @JsExport is not available
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY) //Is required to allow KSP plugin to find the annotation when calling `KSAnnotated.annotations`
annotation class TsExport() {

  companion object {
    /**
     * The name of the JsExport annotation.
     * The JsExport annotation is only available in JS projects.
     */
    const val JsExportAnnotationMame: String = "JsExport"

    /**
     * The name of the `JsExport.Ignore` annotation.
     */
    const val JsExportIgnoredAnnotationMame: String = "Ignore"
  }
}
