package it.neckar.open.annotations

/**
 * Marks a declaration for export into the generated TypeScript definitions of the Kotlin/JS target.
 *
 * On JS this aliases `kotlin.js.JsExport`, so the TypeScript-generation pipeline is unchanged. On
 * Wasm and JVM it is a no-op — `kotlin.js.JsExport` is invalid on `interface`s in Kotlin/Wasm, so it
 * cannot live directly in `commonMain` of a module that also targets `wasmJs`.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
expect annotation class JsExportForTs()

/**
 * Sets the name a declaration is exported under in the generated TypeScript definitions.
 *
 * On JS this aliases `kotlin.js.JsName`; on Wasm and JVM it is a no-op. See [JsExportForTs].
 */
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.CONSTRUCTOR,
)
@Retention(AnnotationRetention.BINARY)
expect annotation class JsNameForTs(val name: String)
