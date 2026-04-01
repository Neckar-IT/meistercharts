import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import java.io.File

/**
 * Provides access to the version catalog from buildSrc Kotlin source code.
 *
 * In `.gradle.kts` scripts, use `libs.*` accessors directly.
 * In buildSrc Kotlin source, use `project.lib("alias")` instead.
 *
 * IMPORTANT: Do not name this `libs` — it would shadow the generated
 * type-safe accessors in `.gradle.kts` scripts.
 */
val Project.versionCatalog: VersionCatalog
  get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

/**
 * Looks up a library from the version catalog by its alias.
 * The alias uses the TOML notation with dashes (e.g., "ktor-client-core").
 */
fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> {
  return versionCatalog.findLibrary(alias).orElseThrow {
    IllegalArgumentException("Library alias '$alias' not found in version catalog")
  }
}

/**
 * Looks up a version from `gradle/npm.versions.toml` by its alias.
 *
 * Parses the TOML file directly at execution time, so newly added entries
 * are immediately visible — even within the same Gradle invocation.
 */
fun Project.npmVersion(alias: String): String {
  val tomlFile = rootProject.file("gradle/npm.versions.toml")
  val versions = parseTomlVersions(tomlFile)
  return versions[alias]
    ?: throw IllegalArgumentException("NPM version alias '$alias' not found in gradle/npm.versions.toml")
}

/**
 * Special alias mappings for npm packages whose normalized name would conflict
 * with Java reserved words or Gradle accessor constraints.
 */
private val npmAliasOverrides = mapOf(
  "class-variance-authority" to "cva",  // "class" conflicts with Object.getClass()
  "@11ty/eleventy" to "eleventy",       // starts with digit
)

/**
 * Normalizes an npm package name to a TOML alias.
 *
 * Conversion rules:
 * - Strip leading `@`
 * - Replace `/` with `-`
 * - Replace `.` with `-`
 * - Strip leading digits (TOML aliases must start with [a-z])
 *
 * Examples:
 * - `@types/react` → `types-react`
 * - `@tanstack/react-query` → `tanstack-react-query`
 * - `lodash.debounce` → `lodash-debounce`
 * - `@11ty/eleventy` → `eleventy`
 * - `react` → `react`
 */
fun normalizeNpmNameToAlias(npmPackageName: String): String {
  npmAliasOverrides[npmPackageName]?.let { return it }

  val raw = npmPackageName
    .removePrefix("@")
    .replace('/', '-')
    .replace('.', '-')

  // Strip leading segment if it starts with a digit (Gradle requires [a-z] start)
  // e.g., "11ty-eleventy" → "eleventy"
  return when {
    raw.first().isDigit() -> raw.substringAfter('-')
    else -> raw
  }
}

/**
 * Looks up a version from `gradle/docker.versions.toml` by its alias.
 *
 * Parses the TOML file directly at execution time, so newly added entries
 * are immediately visible — even within the same Gradle invocation.
 */
fun Project.dockerVersion(alias: String): String {
  val tomlFile = rootProject.file("gradle/docker.versions.toml")
  val versions = parseTomlVersions(tomlFile)
  return versions[alias]
    ?: throw IllegalArgumentException("Docker version alias '$alias' not found in gradle/docker.versions.toml")
}

/**
 * Normalizes a Docker registry/repository path to a TOML alias.
 *
 * Conversion rules:
 * - Strip registry prefix (`docker.io/`, `ghcr.io/`, `quay.io/`)
 * - Replace `/` with `-`
 *
 * When adding a new registry, update the prefix list here.
 *
 * Examples:
 * - `docker.io/grafana/grafana-oss` → `grafana-grafana-oss`
 * - `docker.io/mongo` → `mongo`
 * - `ghcr.io/umami-software/umami` → `umami-software-umami`
 * - `quay.io/keycloak/keycloak` → `keycloak-keycloak`
 */
fun normalizeDockerImageToAlias(registryRepository: String): String {
  val repository = registryRepository
    .removePrefix("docker.io/")
    .removePrefix("ghcr.io/")
    .removePrefix("quay.io/")
  return repository.replace('/', '-')
}

/**
 * Parses the `[versions]` section of a TOML file into a map of alias to version string.
 *
 * Used by [npmVersion] and [dockerVersion] to read versions directly from the TOML file
 * at execution time, avoiding the Gradle version catalog (which is frozen at configuration time).
 */
fun parseTomlVersions(tomlFile: File): Map<String, String> {
  val result = mutableMapOf<String, String>()
  var inVersions = false

  for (line in tomlFile.readLines()) {
    val trimmed = line.trim()
    if (trimmed == "[versions]") {
      inVersions = true
      continue
    }
    if (trimmed.startsWith("[") && inVersions) break
    if (inVersions.not() || trimmed.isEmpty() || trimmed.startsWith("#")) continue

    val match = Regex("""^([\w.-]+)\s*=\s*"([^"]+)"""").find(trimmed)
    if (match != null) {
      result[match.groupValues[1]] = match.groupValues[2]
    }
  }

  return result
}
