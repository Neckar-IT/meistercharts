package it.neckar.gradle.report.warnings

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.security.MessageDigest

/**
 * Renders findings as a GitLab Code Quality report, the same schema Detekt and oxlint already emit —
 * `gitlab-ci.d/mr.yml` merges all of them into the single file `artifacts.reports.codequality`
 * accepts, and GitLab turns each entry into an inline annotation in the MR diff.
 *
 * The field shape follows the Detekt reporter's output byte for byte, including its camelCase
 * `checkName`: that is the producer already proven to render in this pipeline.
 */
object KotlinWarningsCodeQualityReport {

  // encodeDefaults: severity, type and categories are constant per finding and carry no default in
  // the Code Quality schema — omitted, GitLab drops the finding's severity and renders nothing.
  private val json = Json {
    prettyPrint = true
    encodeDefaults = true
  }

  fun render(warnings: List<KotlinWarning>): String =
    json.encodeToString(ListSerializer(CodeQualityFinding.serializer()), warnings.map { it.toFinding() })

  private fun KotlinWarning.toFinding(): CodeQualityFinding = CodeQualityFinding(
    checkName = CheckName,
    description = "$message (${targets.sorted().joinToString(", ")})",
    location = CodeQualityLocation(
      path = filePath,
      positions = CodeQualityPositions(begin = CodeQualityPosition(line = line, column = column)),
    ),
    fingerprint = deduplicationKey.md5(),
  )

  /** One check name for every compiler diagnostic: the compiler names the rule in the message. */
  private const val CheckName: String = "kotlin-compiler-warning"

  /**
   * GitLab identifies a finding across pipelines by its fingerprint. Derived from the deduplication
   * key so the same warning keeps its identity between builds. MD5 matches what the Detekt reporter
   * writes; it is an identifier here, not a security primitive.
   */
  private fun String.md5(): String =
    MessageDigest.getInstance("MD5").digest(toByteArray()).joinToString("") { "%02x".format(it) }
}

/**
 * Kotlin warnings are advisory, never a merge gate — a diagnostic that must block is raised to
 * `:error` via `-Xwarning-level` instead and fails the compilation outright.
 */
private const val MinorSeverity: String = "minor"

/**
 * One category for every compiler diagnostic. Deciding it per finding would mean matching the message
 * text, which is exactly what the capture avoids — and the mix the compiler reports (deprecations,
 * unchecked casts, declarations that are not exportable to JS) is correctness, not formatting.
 */
private const val BugRiskCategory: String = "Bug Risk"

@Serializable
internal data class CodeQualityFinding(
  val checkName: String,
  val description: String,
  val location: CodeQualityLocation,
  val fingerprint: String,
  val categories: List<String> = listOf(BugRiskCategory),
  val otherLocations: List<String> = emptyList(),
  val severity: String = MinorSeverity,
  val type: String = "issue",
)

@Serializable
internal data class CodeQualityLocation(
  val path: String,
  val positions: CodeQualityPositions,
)

@Serializable
internal data class CodeQualityPositions(
  val begin: CodeQualityPosition,
)

@Serializable
internal data class CodeQualityPosition(
  val line: Int,
  val column: Int,
)
