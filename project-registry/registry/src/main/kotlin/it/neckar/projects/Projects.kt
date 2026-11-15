package it.neckar.projects

/**
 * The registry of a build rooted at `internal/open/meistercharts`: `Projects.meistercharts.meisterchartsCore` is
 * `:meistercharts-core`.
 */
object Projects : ProjectRoot() {
  val meistercharts: MeisterchartsProjects = MeisterchartsProjects(this, GradleProjectPath.Root)
}
