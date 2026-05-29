package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider

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
 * Single source of truth: the `engines` block of the root `package.json`.
 * - "node" → reads `engines.node`
 * - "pnpm" → reads `engines.pnpm`
 *
 * Gradle binds its Node toolchain to `engines.node` — the exact version pnpm
 * enforces via `engineStrict` — so the downloaded Node always satisfies pnpm's
 * engine check and `:pnpmInstall` cannot fail on node/pnpm engine drift.
 *
 * `.nvmrc` is a generated mirror of `engines.node` (task `generateNvmrc`,
 * drift-checked by the root `verifyNoGeneratorDrift` task), kept only for tools
 * that read `.nvmrc` directly: nvm, the Ansible nvm role, and
 * `docker/_versions.sh`. See #1914.
 *
 * Any other alias throws — Renovate's native `npm` manager updates application
 * dependencies directly in the relevant `package.json` files, so build-time
 * lookup of arbitrary npm versions is no longer supported.
 */
fun Project.npmVersion(alias: String): String {
  val packageJsonContent = rootProject.file("package.json").readText()
  return when (alias) {
    "node" -> parseNodeEngineVersion(packageJsonContent)
    "pnpm" -> parsePnpmEngineVersion(packageJsonContent)
    else -> throw IllegalArgumentException(
      "npmVersion('$alias') is no longer supported. Only 'node' and 'pnpm' remain after the npm.versions.toml removal (#1635). " +
        "Read application dependencies from the relevant package.json directly."
    )
  }
}

private val NodeEngineRegex = Regex(""""node"\s*:\s*"([^"]+)"""")

/** Parses `engines.node` from root `package.json` content. */
fun parseNodeEngineVersion(packageJsonContent: String): String {
  val match = NodeEngineRegex.find(packageJsonContent)
    ?: throw IllegalStateException("Cannot find engines.node version in package.json")
  return match.groupValues[1]
}

private val PnpmEngineRegex = Regex(""""pnpm"\s*:\s*"([^"]+)"""")

/** Parses `engines.pnpm` from root `package.json` content. */
fun parsePnpmEngineVersion(packageJsonContent: String): String {
  val match = PnpmEngineRegex.find(packageJsonContent)
    ?: throw IllegalStateException("Cannot find engines.pnpm version in package.json")
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
