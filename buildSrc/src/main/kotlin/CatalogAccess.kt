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
 * Looks up the configured Node.js or pnpm version.
 *
 * Sources of truth after the npm.versions.toml removal (#1635):
 * - "node" → reads `.nvmrc` (strips the leading `v`)
 * - "pnpm" → reads `engines.pnpm` from the root `package.json`
 *
 * Any other alias throws — Renovate's native `npm` manager updates application
 * dependencies directly in the relevant `package.json` files, so build-time
 * lookup of arbitrary npm versions is no longer supported.
 */
fun Project.npmVersion(alias: String): String = when (alias) {
  "node" -> readNvmrcNodeVersion(rootProject.file(".nvmrc"))
  "pnpm" -> readPnpmEngineVersion(rootProject.file("package.json"))
  else -> throw IllegalArgumentException(
    "npmVersion('$alias') is no longer supported. Only 'node' and 'pnpm' remain after the npm.versions.toml removal (#1635). " +
      "Read application dependencies from the relevant package.json directly."
  )
}

private val NodeVersionRegex = Regex("""^v?(\d+\.\d+\.\d+)$""")

private fun readNvmrcNodeVersion(nvmrcFile: File): String {
  val content = nvmrcFile.readText().trim()
  val match = NodeVersionRegex.find(content)
    ?: throw IllegalStateException("Cannot parse Node.js version from .nvmrc content <$content>")
  return match.groupValues[1]
}

private val PnpmEngineRegex = Regex(""""pnpm"\s*:\s*"([^"]+)"""")

private fun readPnpmEngineVersion(packageJsonFile: File): String {
  val content = packageJsonFile.readText()
  val match = PnpmEngineRegex.find(content)
    ?: throw IllegalStateException("Cannot find engines.pnpm version in ${packageJsonFile.absolutePath}")
  return match.groupValues[1]
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
