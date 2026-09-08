package it.neckar.gradle

/**
 * Matches the trailing timezone designator of a strict ISO-8601 timestamp — either a numeric
 * offset (`+02:00`, `-05:00`) or the UTC marker `Z`.
 */
private val TimezoneDesignator = Regex("""([+-]\d{2}:\d{2}|Z)$""")

/**
 * Characters a semver build-metadata identifier may consist of.
 */
private val ValidBuildMetadata = Regex("[0-9A-Za-z-]+")

/**
 * Converts a strict ISO-8601 timestamp into a semver build-metadata identifier.
 *
 * Semver allows only `[0-9A-Za-z-]` in each dot-separated identifier after the `+`. A strict
 * ISO-8601 timestamp violates that twice: with the colons in the time and with the timezone
 * designator, whose `+` would even start a second build-metadata section. Both are removed:
 *
 * ```
 * 2026-07-17T19:17:00+02:00 -> 2026-07-17T19-17-00
 * 2026-07-17T19:17:00Z      -> 2026-07-17T19-17-00
 * ```
 *
 * Throws if the result is still not a valid identifier, so an unexpected timestamp format fails
 * the build instead of producing a manifest that the consuming tool rejects at deploy time.
 */
fun semverBuildMetadataFromIsoTimestamp(
  /**
   * A strict ISO-8601 timestamp, e.g. git's `%cI` committer date.
   */
  isoTimestamp: String,
): String {
  val buildMetadata = isoTimestamp
    .replace(TimezoneDesignator, "")
    .replace(":", "-")

  require(buildMetadata.matches(ValidBuildMetadata)) {
    "Cannot convert <$isoTimestamp> to semver build metadata: <$buildMetadata> is not a valid identifier"
  }

  return buildMetadata
}
