package it.neckar.open.annotations

/**
 * Annotation to mark an enum to be exported as an object in JavaScript.
 *
 * This is useful for exporting enums to JavaScript that also contain values
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME) //Is required to allow KSP plugin to find the annotation when calling `KSAnnotated.annotations`
annotation class ExportAsObject() {

  companion object {
    /**
     * The name of the JsExport annotation.
     * The JsExport annotation is only available in JS projects.
     */
    const val AnnotationMame: String = "ExportAsObject"
  }
}
