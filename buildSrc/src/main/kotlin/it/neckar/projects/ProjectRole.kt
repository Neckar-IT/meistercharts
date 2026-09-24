package it.neckar.projects

/**
 * What a configured project does in the system. The project configuration applies the plugin of the
 * role, so no build script and no repository check derives the role from file patterns.
 * [ProjectType] carries toolchain and structure independently of it.
 */
enum class ProjectRole {
  /** Code, configuration or a document other projects or readers consume; never deployed. */
  Library,

  /** A server process that answers `/api/<prefix>/health/live` with `{"state":"live"}`. */
  Service,

  /**
   * Static output a web server delivers. The configuration applies `frontendProject`: the output carries
   * `health/live`, and every nginx `server` block that does more than redirect serves `/health/live`.
   */
  Frontend,

  /** An executable run by hand or by a script, packaged as a fat JAR or native binary. */
  Tool,

  /** Deployment scripts, host provisioning or an image without code of its own. */
  Deployment,

  /** Tests that run against other projects and ship nothing. */
  TestSuite,

  /** Groups subprojects: every [ProjectType.Intermediate] and [ProjectType.ProjectParent]. */
  Container,
}
