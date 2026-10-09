package it.neckar.gradle

import it.neckar.projects.RepositoryPath

/**
 * Where a project is registered: the registry file and the expression a build script writes for its
 * entry, e.g. `Projects.open.commons.kotlinLang`. The registry answers it; an error message names both.
 */
data class ProjectRegistration(
  val file: RepositoryPath,
  val accessor: String,
)
