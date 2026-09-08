package it.neckar.projects

/**
 * A Kotlin compilation target a multiplatform module is registered for.
 *
 * The registered set is the single source of truth for a module's targets: build-logic declares
 * exactly these Kotlin targets and derives the detekt source list, the toolchain, JUnit and Kover
 * from them. A module build script never declares a target itself — `verifyProjects` fails when it
 * does, and when a `src/<prefix>Main` directory exists without a matching entry in the set.
 */
enum class KotlinTarget(
  /**
   * Prefix of the target's source set directories (`src/${sourceSetPrefix}Main/kotlin`).
   * Spelled out rather than derived from the constant name — decapitalizing `WasmJs` would
   * guess `wasmJs` correctly only by accident.
   */
  val sourceSetPrefix: String,
) {
  Jvm("jvm"),
  Js("js"),
  WasmJs("wasmJs"),
  LinuxX64("linuxX64"),
  ;

  val mainSourceSetName: String
    get() = "${sourceSetPrefix}Main"
}
