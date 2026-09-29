package it.neckar.gradle

import org.gradle.api.tasks.AbstractCopyTask

/**
 * Gives every MongoDB and PostgreSQL compose service an open-file limit of [SoftOpenFileLimit], by
 * appending a `ulimits:` block below its `image: ${docker-image::mongo}` or
 * `image: ${docker-image::postgres}` line at materialization:
 * ```yaml
 *   mongodb:
 *     image: ${docker-image::mongo}
 *     # ==== open-file limit (generated — see ComposeDatabaseFileLimits.kt) ====
 *     ulimits:
 *       nofile:
 *         soft: 64000
 *         hard: 524288
 * ```
 *
 * Docker starts a container with a soft limit of 1024 open files. WiredTiger keeps one file per
 * collection and per index open, and every migration backup adds a database of its own: `mongod` on
 * dev.elektromeister.neckar.it held 1024 descriptors, refused connections with `Too many open files`
 * and left the deploy hanging.
 *
 * The limit arrives through the compose file rather than through the daemon default in
 * `/etc/docker/daemon.json`, for the reason [applyComposeLoggingLimits] gives: Docker applies
 * `ulimits` only when it creates a container, and a changed service config is what makes
 * `docker compose up` recreate it on the next deploy.
 *
 * The image is the anchor, so the filter has to run before [filterExternalDockerImages] resolves the
 * placeholder. A database image written without the placeholder fails the build, because it would
 * silently bypass the anchor; a hand-written service-level `ulimits:` fails it as a duplicate key and
 * a second value for the limit.
 */
fun AbstractCopyTask.applyComposeDatabaseFileLimits() {
  // Fingerprint the generated block as task input — a limit change must re-materialize consumers.
  inputs.property("composeDatabaseFileLimitsBlock", expandComposeDatabaseFileLimits(FingerprintAnchor))

  // Filters the whole spec like applyComposeLoggingLimits: `image:` occurs outside compose files only
  // in files that are not materialized (expected-containers.yaml sits next to the deployment tree).
  filter { line -> expandComposeDatabaseFileLimits(line) }
}

/**
 * Appends the `ulimits:` block to [line] if it is the `image:` of a MongoDB or PostgreSQL service;
 * returns it unchanged otherwise. Rejects a hand-written service-level `ulimits:` and a database image
 * without the `${docker-image::…}` placeholder — see [applyComposeDatabaseFileLimits].
 */
internal fun expandComposeDatabaseFileLimits(line: String): String {
  require(HandWrittenUlimitsRegex.matchEntire(line) == null) {
    "Hand-written `ulimits:` block in a compose file: [$line]. The open-file limit of database services is " +
      "generated from ComposeDatabaseFileLimits.kt — delete the block."
  }
  require(LiteralDatabaseImageRegex.matchEntire(line) == null) {
    $$"Database image without placeholder in a compose file: [$$line]. Reference it as `${docker-image::mongo}` or " +
      $$"`${docker-image::postgres}`, which ComposeDatabaseFileLimits.kt reads to add the open-file limit."
  }

  val match = DatabaseImagePlaceholderRegex.matchEntire(line) ?: return line
  val indent = match.groupValues[1]

  return listOf(
    line,
    "$indent# ==== open-file limit (generated — see ComposeDatabaseFileLimits.kt) ====",
    "${indent}ulimits:",
    "$indent  nofile:",
    "$indent    soft: $SoftOpenFileLimit",
    "$indent    hard: $HardOpenFileLimit",
  ).joinToString("\n")
}

/**
 * Open files a database container may hold. WiredTiger needs one per collection and index, backups
 * included; 64000 is the value MongoDB's production notes recommend.
 */
private const val SoftOpenFileLimit: Int = 64000

/** Ceiling up to which the database process may raise its own limit; Docker's default hard limit. */
private const val HardOpenFileLimit: Int = 524288

/** Canonical anchor line used to fingerprint the generated block as a task input property. */
private const val FingerprintAnchor: String = $$"    image: ${docker-image::mongo}"

private val DatabaseImagePlaceholderRegex = Regex($$"""^(\s*)image:\s*(["']?)\$\{docker-image::(?:mongo|postgres)}\2\s*$""")

/** A `mongo` or `postgres` image written out, with or without registry and namespace. */
private val LiteralDatabaseImageRegex = Regex("""^\s*image:\s*["']?(?:\S+/)?(?:mongo|postgres)[:@].*$""")

/** Service-level `ulimits:` key, i.e. indented but not nested deeper than a service's own keys. */
private val HandWrittenUlimitsRegex = Regex("""^\s{1,6}ulimits:\s*$""")
