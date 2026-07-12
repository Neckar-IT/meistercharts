package it.neckar.open.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
actual annotation class JsExportForTs actual constructor()

@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.CONSTRUCTOR,
)
@Retention(AnnotationRetention.BINARY)
actual annotation class JsNameForTs actual constructor(actual val name: String)
