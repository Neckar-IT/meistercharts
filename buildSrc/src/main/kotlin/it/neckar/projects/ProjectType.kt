package it.neckar.projects

/**
 * The toolchain and structure of a configured project; [ProjectRole] says what it does in the system.
 * Sealed so the dev-server strategy for JS projects can be expressed as a subtype of [Pnpm] rather than
 * a parallel flag (see `it.neckar.gradle.localdev`).
 */
sealed interface ProjectType {
  /**
   * JVM project - with the latest LTS JDK version
   */
  data object KotlinJvm : ProjectType

  /**
   * Projects that contain a KSP processor
   */
  data object KspProcessor : ProjectType

  /**
   * Multiplatform project. Which platforms it actually builds for is the registered
   * [ConfiguredProject.targets] set, not a property of the type — a module compiling only to
   * `linuxX64` and one compiling to `jvm` + `js` + `wasmJs` are both of this type.
   */
  data object KotlinMultiplatform : ProjectType

  /**
   * Python project
   */
  data object Python : ProjectType

  /**
   * IntelliJ IDEA plugin
   */
  data object IdeaPlugin : ProjectType

  /**
   * Represents a project parent (not the Gradle root).
   * A project parent is used to merge things related to one project (e.g. deployment or kover reports)
   */
  data object ProjectParent : ProjectType

  /**
   * Intermediate project - does not have any configuration
   */
  data object Intermediate : ProjectType

  /**
   * Another project - does not have any (common) configuration
   */
  data object Other : ProjectType

  /**
   * JS project built with pnpm. The subtype is the local-dev server strategy; [Library] has none.
   */
  sealed interface Pnpm : ProjectType {
    /** Generic pnpm package (config or library) — not a runnable web app, no dev server. */
    data object Library : Pnpm

    /** Web app with a Vite dev server (`VITE_PORT=<port> pnpm exec vite`). */
    data object Vite : Pnpm

    /** Web app with an Astro dev server (`pnpm exec astro dev --port <port> --host`). */
    data object Astro : Pnpm
  }

  companion object {
    /**
     * All concrete project types — replaces the former enum `entries`.
     */
    val all: List<ProjectType> = listOf(
      KotlinJvm,
      KspProcessor,
      KotlinMultiplatform,
      Python,
      IdeaPlugin,
      ProjectParent,
      Intermediate,
      Other,
      Pnpm.Library,
      Pnpm.Vite,
      Pnpm.Astro,
    )
  }
}
