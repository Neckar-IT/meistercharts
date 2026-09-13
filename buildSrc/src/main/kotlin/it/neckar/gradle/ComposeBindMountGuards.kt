package it.neckar.gradle

import org.gradle.api.tasks.AbstractCopyTask

/**
 * Gives every bind mount a deployment writes relative to its compose file the long syntax with
 * `create_host_path: false`, so Docker refuses the container instead of inventing the source.
 *
 * Without the guard Docker creates an empty directory at a missing source and starts the container
 * against it, and `sh` handed a directory exits 0 without output — a silent `restart: unless-stopped`
 * loop. With it the daemon answers `bind source path does not exist: <path>`.
 *
 * Relative sources only. [DockerComposeVolumeRewriter], whose translation this reuses, guards
 * absolute ones too; a deployment also mounts host paths like `/dev` and
 * `/var/lib/docker/containers`, and a checkout cannot say whether each exists on every host.
 */
fun AbstractCopyTask.applyComposeBindMountGuards() {
  inputs.property("composeBindMountGuard", FingerprintAnchors.joinToString("\n", transform = ::guardRelativeBindMount))

  // The whole spec, like the logging limits: a compose file no pattern anticipated stays guarded.
  filter { line -> guardRelativeBindMount(line) }
}

/**
 * Rewrites [line] into the long bind syntax if it is a relative short-syntax mount; returns it
 * unchanged otherwise. Refuses a hand-written long-syntax mount and a mode the shared translation
 * would drop — either would ship a mount with `create_host_path` at its permissive default.
 *
 * Line-based, because a copy task filters lines: a relative mount outside a `volumes:` block is
 * rewritten too, and no compose file of this repository writes one.
 */
internal fun guardRelativeBindMount(line: String): String {
  require(HandWrittenLongSyntaxRegex.matchEntire(line) == null) {
    "Hand-written long-syntax bind mount in a compose file: [$line]. Write it as `- <source>:<target>`; " +
      "the rewrite adds create_host_path: false, which a hand-written block carries only as long as " +
      "someone remembers it — and VerifyComposeBindMountsTask parses the short syntax alone."
  }

  val mount: RelativeBindMount = parseRelativeBindMount(line) ?: return line

  val untranslated: List<String> = DockerComposeVolumeRewriter.untranslatedModes(mount.modes)
  require(untranslated.isEmpty()) {
    "Relative bind mount whose mode ${untranslated.joinToString()} the long syntax would drop, in " +
      "[$line]. It spells a mode out per key and takes one per family, so name one mode per family " +
      "and add a missing translation in DockerComposeVolumeRewriter.kt."
  }

  return DockerComposeVolumeRewriter.longSyntaxBlock(
    indent = mount.indent,
    source = mount.source.value,
    target = mount.target,
    modes = mount.modes,
  )
}

data class RelativeBindMount(
  val indent: String,
  val source: BindMountSource,
  val target: String,
  /** The modes the entry names, `ro` or `ro,rslave`, empty when it names none. */
  val modes: List<String>,
)

/**
 * Parses [line] as a volume entry whose source is relative — `- ./x:/y`, `- ../x:/y:ro`, quoted or
 * bare, with or without a trailing comment — or returns `null` when it is not one. An absolute
 * source and a named volume name nothing the deploy tree carries.
 */
fun parseRelativeBindMount(line: String): RelativeBindMount? {
  val match = RelativeShortSyntaxMountRegex.matchEntire(line) ?: return null
  val (indent, _, source, target, modes) = match.destructured

  return RelativeBindMount(
    indent = indent,
    source = BindMountSource(source),
    target = target,
    modes = modes.split(",").filter { it.isNotEmpty() },
  )
}

/** One anchor per branch of the translation, so a change to any of its keys re-materializes. */
private val FingerprintAnchors: List<String> = listOf(
  "      - ./fingerprint.yml:/fingerprint.yml",
  "      - ./fingerprint.yml:/fingerprint.yml:ro",
  "      - ./fingerprint.yml:/fingerprint.yml:rw",
  "      - ./fingerprint.yml:/fingerprint.yml:ro,rslave",
  "      - ./fingerprint.yml:/fingerprint.yml:Z",
)

/** The quote is back-referenced, so a stray one on a single side does not parse as a mount. */
private val RelativeShortSyntaxMountRegex =
  Regex("""^(\s*)-\s+(["']?)(\.{1,2}/[^:"'\s]+):(/[^:"'\s]*)(?::([A-Za-z,]+))?\2\s*(?:#.*)?$""")

/** Bounded in indentation like the logging limits' reject: the filter reads every materialized file. */
private val HandWrittenLongSyntaxRegex = Regex("""^\s{1,8}-\s+type:\s*bind\s*$""")
