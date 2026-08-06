package it.neckar.gradle

import org.gradle.api.tasks.AbstractCopyTask

/**
 * Gives every compose service a bounded container log at materialization, by appending a `logging:`
 * block below its `container_name:` — single source of truth for the limit, compose analog of
 * [expandOidcMiddlewareLabels]:
 * ```yaml
 *   otel-agent:
 *     image: otel/opentelemetry-collector-contrib:0.157.0
 *     container_name: otel-agent
 *     logging:
 *       driver: "json-file"
 *       options:
 *         max-size: "100m"
 *         max-file: "3"
 * ```
 *
 * Docker's json-file driver writes without any limit unless one is given, so a container that loops
 * on an error fills the disk: an orphaned node-exporter on dev.elektromeister.neckar.it logged
 * `broken pipe` for two months into a single 12 GB file (#2572), and traefik on auth-host.neckar.it
 * wrote 1.1 GB in nineteen hours while the disk went to 100% (#2864).
 *
 * The limit has to arrive through the compose file rather than through the daemon default in
 * `/etc/docker/daemon.json`, because Docker resolves it once, when the container is CREATED, and
 * then keeps it for the container's lifetime. A container created before the daemon default existed
 * keeps writing unbounded across every restart — measured in docker:dind: with the daemon set to
 * `max-size=1m`, a container created beforehand and merely restarted grew a single 120 MB file,
 * while a container created afterwards rotated at 1 MB. Stating the limit here makes the service
 * config differ, which is exactly what makes `docker compose up` recreate the container — so the
 * next ordinary deploy fixes the containers that are running unbounded today, with no separate
 * migration step and no dockerd restart.
 *
 * `container_name:` is the anchor because every service carries one (enforced for the Loki labels by
 * `VerifyComposeContainerNamesTask`, #2381) and carries exactly one, which makes this a stateless
 * per-line rewrite. A hand-written `logging:` block fails the build instead of being merged with:
 * two blocks in one service is a duplicate YAML key, and a second value for the limit is precisely
 * the drift this expansion exists to prevent.
 */
fun AbstractCopyTask.applyComposeLoggingLimits() {
  // Fingerprint the generated block as task input — a limit change must re-materialize consumers.
  inputs.property("composeLoggingBlock", expandComposeLoggingLimits(FingerprintAnchor))

  // Filters the whole spec, like the OIDC expansion, rather than a `filesMatching` child spec:
  // every materialized file passes one filter chain, so a host whose compose lives somewhere the
  // pattern did not anticipate cannot end up without a limit.
  //
  // Safe over every file: `container_name:` occurs in compose files only (verified across internal/
  // and external/), and no file carries a service-level `logging:` — the reject below is what keeps
  // it that way. Registered at configuration time; the generated block holds no `${…}`, so its
  // order against the secrets filter is free.
  filter { line -> expandComposeLoggingLimits(line) }
}

/**
 * Appends the `logging:` block to [line] if it is a `container_name:`; returns it unchanged
 * otherwise. Rejects a hand-written service-level `logging:` block — see [applyComposeLoggingLimits].
 */
internal fun expandComposeLoggingLimits(line: String): String {
  require(HandWrittenLoggingRegex.matchEntire(line) == null) {
    "Hand-written `logging:` block in a compose file: [$line]. The container log limit is generated " +
      "for every service from ComposeLoggingLimits.kt — delete the block."
  }

  val match = ContainerNameRegex.matchEntire(line) ?: return line
  val indent = match.groupValues[1]

  return listOf(
    line,
    "$indent# ==== container log limit (generated — see ComposeLoggingLimits.kt) ====",
    "${indent}logging:",
    """$indent  driver: "json-file"""",
    "$indent  options:",
    """$indent    max-size: "$MaxLogFileSize"""",
    """$indent    max-file: "$MaxLogFileCount"""",
  ).joinToString("\n")
}

/**
 * Size at which the json-file driver rotates a container's log, enforced as the log is written.
 * With [MaxLogFileCount] this caps each container at 300 MB.
 */
private const val MaxLogFileSize: String = "100m"

/** Generations the json-file driver keeps, the current one included. */
private const val MaxLogFileCount: String = "3"

/** Canonical anchor line used to fingerprint the generated block as a task input property. */
private const val FingerprintAnchor: String = "    container_name: fingerprint"

private val ContainerNameRegex = Regex("""^(\s*)container_name:\s*\S.*$""")

/** Service-level `logging:` key, i.e. indented but not nested deeper than a service's own keys. */
private val HandWrittenLoggingRegex = Regex("""^\s{1,6}logging:\s*$""")
