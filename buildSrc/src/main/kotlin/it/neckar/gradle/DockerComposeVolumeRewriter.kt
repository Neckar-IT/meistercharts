package it.neckar.gradle

/**
 * Rewrites short-syntax bind-mount entries in a Docker Compose YAML string to the
 * long syntax with `create_host_path: false`.
 *
 * **Motivation.** Docker's default Bind-Mount behaviour is to silently create the
 * source path as an empty root-owned directory when it does not exist. That auto-
 * creation produces "ghost" directories that are hard to clean up (root-owned,
 * not tracked by git) and that can later collide with real files placed at the
 * same path. The long-syntax form supports `bind: { create_host_path: false }`,
 * which makes the container start fail-fast instead:
 *
 * ```
 * Error response from daemon: invalid mount config for type "bind":
 * stat /…/file.yaml: no such file or directory
 * ```
 *
 * Applied centrally by [registerGenerateDockerComposeTask] so every generated
 * `docker-compose.yml` inherits the safer behaviour without each module having
 * to spell out the long syntax in its template.
 *
 * **Scope of rewriting.** Only entries whose source starts with `/` or `.` are
 * rewritten — those are bind mounts. Named-volume entries (no path prefix in the
 * source) are passed through unchanged. Entries already in long syntax (`type:`
 * keyword in the same list item, or any line that does not match the short-form
 * regex) are also passed through.
 *
 * **Comment preservation.** The rewrite is line-based, not YAML-round-trip, so
 * comments outside the rewritten volume entries stay intact. Comments inline
 * with a volume entry are dropped — short-syntax volumes are single-line and
 * any inline comment would be lost when expanded to multi-line long syntax.
 */
internal object DockerComposeVolumeRewriter {

  private val volumeEntryPattern = Regex("""^(\s+)- ["']?(.+?)["']?\s*(?:#.*)?$""")
  private val volumesKeyPattern = Regex("""^(\s+)volumes:\s*(?:#.*)?$""")

  /**
   * Modes that map to `read_only: true` (or false). Compose short syntax allows
   * combining several modes with `,` — e.g. `ro,rslave`.
   */
  private val readOnlyModes: Set<String> = setOf("ro")
  private val readWriteModes: Set<String> = setOf("rw")

  /**
   * Propagation modes recognised by the Linux kernel and Docker. Mapped onto
   * `bind.propagation` in the long syntax.
   */
  private val propagationModes: Set<String> = setOf(
    "rprivate", "private", "rshared", "shared", "rslave", "slave",
  )

  /**
   * SELinux relabel modes. Mapped onto `bind.selinux` in the long syntax.
   */
  private val selinuxModes: Set<String> = setOf("z", "Z")

  /**
   * The modes of [modes] that [longSyntaxBlock] would drop: one it has no key for, and every one past
   * the first of a family. [applyComposeBindMountGuards] refuses such an entry.
   */
  internal fun untranslatedModes(modes: List<String>): List<String> {
    val carried: List<String> = listOfNotNull(
      modes.firstOrNull { it in readOnlyModes || it in readWriteModes },
      modes.firstOrNull { it in propagationModes },
      modes.firstOrNull { it in selinuxModes },
    )

    return modes - carried.toSet()
  }

  fun rewrite(content: String): String {
    if (content.isEmpty()) return content
    val lines = content.lines()
    val output = StringBuilder(content.length + 256)
    var inServiceVolumes = false
    var volumesKeyIndent = 0
    val lineEnding = if ("\r\n" in content) "\r\n" else "\n"

    for (line in lines) {
      if (inServiceVolumes) {
        val firstNonWs = line.indexOfFirst { !it.isWhitespace() }
        if (firstNonWs < 0) {
          // Blank line — stay inside the volumes block.
          output.append(line).append(lineEnding)
          continue
        }
        if (firstNonWs <= volumesKeyIndent) {
          // Indentation back to or above the `volumes:` key → block ended.
          inServiceVolumes = false
          // Fall through so the line is processed by the top-level branch below.
        } else {
          val match = volumeEntryPattern.matchEntire(line)
          val rewritten = match?.let { rewriteEntry(it.groupValues[1], it.groupValues[2]) }
          if (rewritten != null) {
            output.append(rewritten).append(lineEnding)
          } else {
            output.append(line).append(lineEnding)
          }
          continue
        }
      }

      val volumesMatch = volumesKeyPattern.matchEntire(line)
      if (volumesMatch != null) {
        val indent = volumesMatch.groupValues[1].length
        if (indent > 0) {
          // Service-level volumes block. Top-level `volumes:` (indent 0) defines
          // named volumes via map keys, not list entries, so the rewrite does
          // not apply there — leave it untouched.
          inServiceVolumes = true
          volumesKeyIndent = indent
        }
      }
      output.append(line).append(lineEnding)
    }
    // The loop always emits a line ending after the final element. For input
    // that ends with a separator, `String.lines()` produces a synthetic empty
    // trailing element whose appended terminator restores the original final
    // newline. For input without a trailing separator, the loop emits one more
    // terminator than the input had. Strip exactly one terminator either way —
    // the synthetic empty element preserves the original shape.
    output.setLength(output.length - lineEnding.length)
    return output.toString()
  }

  /**
   * The long-syntax block for one short-syntax bind mount, or `null` when [entry] is a named volume
   * or already long syntax. A bind mount that does not parse fails the build rather than passing
   * through unguarded.
   */
  private fun rewriteEntry(indent: String, entry: String): String? {
    val trimmed: String = entry.trim()

    // A bind-mount source starts with `/` or `.`. A named volume carries no path prefix and a
    // long-syntax entry starts with a key, and neither is this rewrite's business; everything else
    // is a bind mount, so a shape that does not parse is a broken compose file rather than a
    // fourth kind of entry.
    if (trimmed.isEmpty() || (trimmed.first() != '/' && trimmed.first() != '.')) return null

    val parts: List<String> = trimmed.split(":")
    require(parts.size in 2..3) {
      "Bind mount [$entry] splits into ${parts.size} colon-separated parts; the short syntax is " +
        "`SOURCE:TARGET[:MODE]`."
    }

    val source: String = parts[0]
    val target: String = parts[1]
    require(source.isNotEmpty() && target.isNotEmpty()) {
      "Bind mount [$entry] leaves its source or its target empty."
    }
    require(target.startsWith("/")) {
      "Bind mount [$entry] names the relative target [$target]; a path inside a container is absolute."
    }

    val modes: List<String> = parts.getOrNull(2)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    return longSyntaxBlock(indent, source, target, modes)
  }

  /**
   * The long-syntax block for a mount already split into its parts — what
   * [applyComposeBindMountGuards] calls, holding them from its own regex.
   */
  internal fun longSyntaxBlock(indent: String, source: String, target: String, modes: List<String>): String {
    val readOnly: Boolean? = when {
      modes.any { it in readOnlyModes } -> true
      modes.any { it in readWriteModes } -> false
      else -> null
    }
    val propagation = modes.firstOrNull { it in propagationModes }
    val selinux = modes.firstOrNull { it in selinuxModes }

    val itemIndent = indent
    val keyIndent = "$indent  "
    val nestedIndent = "$indent    "
    val sb = StringBuilder()
    sb.append(itemIndent).append("- type: bind").append('\n')
    sb.append(keyIndent).append("source: ").append(source).append('\n')
    sb.append(keyIndent).append("target: ").append(target).append('\n')
    if (readOnly == true) {
      sb.append(keyIndent).append("read_only: true").append('\n')
    } else if (readOnly == false) {
      sb.append(keyIndent).append("read_only: false").append('\n')
    }
    sb.append(keyIndent).append("bind:").append('\n')
    sb.append(nestedIndent).append("create_host_path: false")
    if (propagation != null) {
      sb.append('\n').append(nestedIndent).append("propagation: ").append(propagation)
    }
    if (selinux != null) {
      sb.append('\n').append(nestedIndent).append("selinux: ").append(selinux)
    }
    return sb.toString()
  }
}
