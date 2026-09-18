package it.neckar.projects

/**
 * The property name a registry gives the project in [directory]: `kotlin-lang` → `kotlinLang`,
 * `auto-merge.neckar.it` → `autoMergeNeckarIt`.
 */
fun accessorName(directory: String): String {
  val words: List<String> = directory.split('-', '.')
  return words.first() + words.drop(1).joinToString("") { word -> word.replaceFirstChar { it.uppercaseChar() } }
}

/**
 * The registrations below this project whose property name differs from [accessorName] of their
 * directory, or that no property exposes. The root's own subprojects may stay unnamed, and a group at
 * the path of its parent (`Projects.open` in a build rooted at `internal/open`) has no directory to be named after.
 */
fun ConfiguredProject.accessorNameViolations(): List<String> {
  val properties: Map<String, ConfiguredProject> = javaClass.methods
    .filter { it.parameterCount == 0 && it.name.startsWith("get") && ConfiguredProject::class.java.isAssignableFrom(it.returnType) }
    .associate { getter -> getter.name.removePrefix("get").replaceFirstChar { it.lowercaseChar() } to getter.invoke(this) as ConfiguredProject }

  val misnamed: List<String> = properties.filterValues { it.relativePath != GradleProjectPath.Root }.mapNotNull { (propertyName, project) ->
    val expectedName: String = accessorName(project.relativePath.path.substringAfterLast(':'))
    when (propertyName) {
      expectedName -> null
      else -> "${project.path}: property '$propertyName' must be named '$expectedName'"
    }
  }
  val unnamed: List<String> = subprojects
    .filter { subproject -> properties.values.none { it === subproject } }
    .filter { this !is ProjectRoot }
    .map { "${it.path}: no property of ${javaClass.simpleName} exposes it" }

  return misnamed + unnamed + subprojects.flatMap { it.accessorNameViolations() }
}
