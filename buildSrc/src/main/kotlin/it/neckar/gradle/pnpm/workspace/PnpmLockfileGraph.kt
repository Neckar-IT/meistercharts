package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.pnpm.dependency.NpmPackageName

/**
 * The installed copies of `pnpm-lock.yaml` and the dependency entries pointing at them. An unknown
 * field of an importer, a snapshot or an installing entry fails with its line number.
 */
class PnpmLockfileGraph(
  val snapshotKeys: List<PnpmSnapshotKey>,
  val dependencyEdges: List<PnpmDependencyEdge>,
) {
  fun packageNamesWithoutSnapshot(packageNames: Set<NpmPackageName>): Set<NpmPackageName> {
    return packageNames - snapshotKeys.map { it.packageName }.toSet()
  }

  fun dependentsOf(snapshotKey: PnpmSnapshotKey): List<PnpmDependent> {
    return dependencyEdges.filter { it.resolvesTo(snapshotKey) }.map { it.dependent }.distinct()
  }

  companion object {
    /** Blocks of an importer whose entries install a package. */
    private val importerInstallingBlockNames: Set<String> = setOf("dependencies", "devDependencies", "optionalDependencies")

    /** Fields of an importer that install nothing into the workspace package: pnpm's own config and binary. */
    private val importerFieldNamesWithoutEdges: Set<String> = setOf("configDependencies", "packageManagerDependencies")

    /** Blocks of a snapshot whose entries install a package; an installed package has no dev dependencies. */
    private val snapshotInstallingBlockNames: Set<String> = setOf("dependencies", "optionalDependencies")

    /** Fields of a snapshot that install nothing. */
    private val snapshotFieldNamesWithoutEdges: Set<String> = setOf("transitivePeerDependencies", "optional")

    fun parse(lockfileText: String): PnpmLockfileGraph {
      val reader = Reader()

      lockfileText.lineSequence().forEachIndexed { index, line ->
        if (line.isBlank()) {
          return@forEachIndexed
        }

        reader.readLine(LockfileLine(lineNumber = index + 1, text = line))
      }

      return reader.finish()
    }

    private fun fail(lineNumber: Int, line: String, problem: String): Nothing {
      throw IllegalArgumentException("pnpm-lock.yaml line $lineNumber has $problem: <$line>")
    }
  }

  private class LockfileLine(val lineNumber: Int, val text: String) {
    val indentation: Int = text.length - text.trimStart(' ').length

    // Lazy: the list items under `transitivePeerDependencies:` carry no key and are never read.
    val entry: Entry by lazy { Entry.parse(lineNumber = lineNumber, line = text, indentation = indentation) }

    fun fail(problem: String): Nothing {
      fail(lineNumber, text, problem)
    }
  }

  private enum class Section(val header: String) {
    Importers("importers:"),
    Snapshots("snapshots:"),
  }

  /** The state of one pass over the lockfile: the entry that owns the current block and what it installs. */
  private class Reader {
    private val snapshotKeys = mutableListOf<PnpmSnapshotKey>()
    private val dependencyEdges = mutableListOf<PnpmDependencyEdge>()

    private var section: Section? = null

    private var dependent: PnpmDependent? = null
    private var insideInstallingBlock: Boolean = false

    /** An importer dependency whose `version:` line has not come yet. */
    private var importerDependencyWithoutVersion: Entry? = null

    fun readLine(line: LockfileLine) {
      if (line.indentation == 0) {
        // Also the `---` that opens each YAML document.
        startSection(Section.entries.find { it.header == line.text })
        return
      }

      when (section) {
        null -> Unit
        Section.Importers -> readImporterLine(line)
        Section.Snapshots -> readSnapshotLine(line)
      }
    }

    private fun startSection(newSection: Section?) {
      requireNoDependencyWithoutVersion()
      section = newSection
      startBlockOwner(null)
    }

    private fun readImporterLine(line: LockfileLine) {
      // A dependency's `version:` sits at 8; any shallower line ends the dependency.
      if (line.indentation <= 6) {
        requireNoDependencyWithoutVersion()
      }

      when (line.indentation) {
        2 -> startBlockOwner(PnpmDependent.Importer(line.entry.requireBlockKey()))
        4 -> insideInstallingBlock = line.entry.isInstallingBlock(importerInstallingBlockNames, importerFieldNamesWithoutEdges)
        6 -> if (insideInstallingBlock) {
          readImporterDependency(line.entry)
        }

        8 -> if (insideInstallingBlock) {
          readImporterDependencyField(line.entry)
        }

        else -> line.fail("an indentation the importers section does not use")
      }
    }

    private fun readSnapshotLine(line: LockfileLine) {
      when (line.indentation) {
        2 -> {
          val snapshotKey = PnpmSnapshotKey.parse(line.entry.requireBlockKey())
          snapshotKeys += snapshotKey
          startBlockOwner(PnpmDependent.Snapshot(snapshotKey))
        }

        4 -> insideInstallingBlock = line.entry.isInstallingBlock(snapshotInstallingBlockNames, snapshotFieldNamesWithoutEdges)
        6 -> if (insideInstallingBlock) {
          addEdge(line.entry, NpmPackageName(line.entry.key), line.entry.requireResolution())
        }

        else -> line.fail("an indentation the snapshots section does not use")
      }
    }

    fun finish(): PnpmLockfileGraph {
      requireNoDependencyWithoutVersion()
      return PnpmLockfileGraph(snapshotKeys = snapshotKeys, dependencyEdges = dependencyEdges)
    }

    private fun startBlockOwner(newDependent: PnpmDependent?) {
      dependent = newDependent
      insideInstallingBlock = false
    }

    private fun readImporterDependency(entry: Entry) {
      if (entry.value != null) {
        entry.fail("a dependency with an inline value instead of `specifier:` and `version:`")
      }
      importerDependencyWithoutVersion = entry
    }

    private fun readImporterDependencyField(entry: Entry) {
      val dependency = importerDependencyWithoutVersion
      when (entry.key) {
        "version" -> {
          val dependencyName = NpmPackageName((dependency ?: entry.fail("a version outside a dependency")).key)
          addEdge(entry, dependencyName, entry.requireResolution())
          importerDependencyWithoutVersion = null
        }

        "specifier" -> if (dependency == null) {
          entry.fail("a specifier outside a dependency")
        }

        else -> entry.fail("a dependency field other than specifier and version")
      }
    }

    private fun addEdge(entry: Entry, dependencyName: NpmPackageName, resolution: PnpmResolution) {
      dependencyEdges += PnpmDependencyEdge(
        dependent = dependent ?: entry.fail("a dependency outside an importer or snapshot"),
        dependencyName = dependencyName,
        resolution = resolution,
      )
    }

    private fun requireNoDependencyWithoutVersion() {
      importerDependencyWithoutVersion?.fail("a dependency without a version")
    }
  }

  /** One `key: value` line; pnpm quotes what YAML would misread (`'@types/node'`, `'catalog:'`), `{}` is an empty block. */
  private class Entry(
    val lineNumber: Int,
    val line: String,
    val key: String,
    val value: String?,
  ) {
    fun isInstallingBlock(installingBlockNames: Set<String>, fieldNamesWithoutEdges: Set<String>): Boolean {
      return when (key) {
        in installingBlockNames -> {
          requireBlockKey()
          true
        }

        in fieldNamesWithoutEdges -> false
        else -> fail("a field other than ${installingBlockNames + fieldNamesWithoutEdges}")
      }
    }

    /** The key of a line that opens a block, whose entries follow on the next lines. */
    fun requireBlockKey(): String {
      if (value != null) {
        fail("an inline value where a block belongs")
      }
      return key
    }

    fun requireResolution(): PnpmResolution {
      return PnpmResolution(value ?: fail("no resolution"))
    }

    fun fail(problem: String): Nothing {
      fail(lineNumber, line, problem)
    }

    companion object {
      fun parse(lineNumber: Int, line: String, indentation: Int): Entry {
        val content = line.substring(indentation)

        // YAML separates key and value by `: `, so an unquoted tarball key keeps the colon of its URL.
        val keyEnd = if (content.startsWith("'")) {
          content.indexOf("':", startIndex = 1) + 1
        } else {
          content.indexOf(": ").takeIf { it >= 0 } ?: if (content.endsWith(":")) content.length - 1 else -1
        }
        if (keyEnd <= 0) {
          fail(lineNumber, line, "no key")
        }

        val rawValue = content.substring(keyEnd + 1).trim()
        return Entry(
          lineNumber = lineNumber,
          line = line,
          key = unquote(content.substring(0, keyEnd), lineNumber, line),
          value = if (rawValue.isEmpty() || rawValue == "{}") null else unquote(rawValue, lineNumber, line),
        )
      }

      /** Removes the single quotes pnpm puts around a scalar; escapes, double quotes and `''` fail, pnpm writes none of them. */
      private fun unquote(scalar: String, lineNumber: Int, line: String): String {
        if (scalar.startsWith("\"")) {
          fail(lineNumber, line, "a double-quoted scalar")
        }
        if (scalar.startsWith("'").not()) {
          return scalar
        }

        val quoted = scalar.removeSurrounding("'")
        if (quoted.length != scalar.length - 2 || quoted.contains('\'')) {
          fail(lineNumber, line, "a single-quoted scalar with an escape or without its closing quote")
        }
        if (quoted.isEmpty()) {
          fail(lineNumber, line, "an empty quoted scalar")
        }
        return quoted
      }
    }
  }
}
