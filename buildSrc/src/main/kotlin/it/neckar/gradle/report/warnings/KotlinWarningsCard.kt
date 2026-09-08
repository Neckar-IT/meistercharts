package it.neckar.gradle.report.warnings

import it.neckar.gradle.report.escapeHtml
import java.io.File

/**
 * Renders the `Kotlin Compiler Warnings` card of the reports.neckar.it index: the repository-wide stock of
 * compiler diagnostics, broken down by category and by module, with the full finding list underneath.
 *
 * The markup reuses the triage classes the report index already defines for the Detekt triage card
 * (`.report-item-wide`, `.triage-headline`, `.triage-panel`, `.summary-table`, `.findings-table`,
 * `.findings-filter-banner`, `.findings-list-wrapper`) and its filter script, which keys on
 * `data-triage-id` plus the `data-filter-*` attributes of a clickable summary row. No styles of its own.
 *
 * The git hash every entry point takes pins the `file:line` links to a tree, so they keep resolving after
 * `main` has moved on. Caller's duty to pass the hash the fragments were compiled from — in the nightly
 * Reports job both come from the same checkout, in a local working copy the fragments can be older.
 */
object KotlinWarningsCard {

  /**
   * The card for the [KotlinWarningFragments] under [fragmentDirectory], or an empty string when that
   * directory holds none — which is every build outside the nightly Reports schedule.
   *
   * A fragment that does not parse throws: it is written by the capture of a build in this repository, so
   * a malformed one is a defect, not a missing input.
   */
  fun renderFromFragments(fragmentDirectory: File, gitHash: String): String =
    render(KotlinWarningFragments.readAll(KotlinWarningFragments.fragmentFilesIn(fragmentDirectory)), gitHash)

  /**
   * The card, or an empty string when there is nothing to show — a card claiming zero findings would be
   * indistinguishable from a run whose capture never saw a compilation.
   */
  fun render(warnings: List<KotlinWarning>, gitHash: String): String {
    if (warnings.isEmpty()) return ""

    val categoryRows = warnings.rankedBy { it.categoryLabel() }
      .joinToString("\n") { (category, count) -> summaryRow(filterType = "diagnostic", label = "Kategorie", value = category, count = count) }

    val moduleRows = warnings.rankedBy { it.modulePath }
      .joinToString("\n") { (module, count) -> summaryRow(filterType = "module", label = "Modul", value = module, count = count) }

    val findingsToRender = warnings.sortedWith(compareBy({ it.filePath }, { it.line }, { it.column })).take(FindingsCap)
    val truncatedNote = if (warnings.size > FindingsCap) {
      """<p class="report-description">Tabelle zeigt die ersten $FindingsCap von ${warnings.size} Findings — der Rest steht im Job-Artefakt <code>build-reports/${KotlinWarningFragments.DirectoryName}/</code>.</p>"""
    } else ""

    val findingsRows = findingsToRender.joinToString("\n") { warning -> findingsRow(warning, gitHash) }

    return $$"""
<div class="report-item report-item-wide" data-triage-id="$$TriageId">
    <div class="report-header">
        <div class="report-title">⚠️ Kotlin Compiler Warnings</div>
        <a href="#" class="toggle-btn findings-toggle" data-folder-id="$$TriageId">Alle Findings zeigen</a>
    </div>
    <p class="report-description">
        Alle erfassten Kotlin-Compiler-Warnungen, zusammengeführt über die Targets, die sie gemeldet haben.
        Klick auf eine Kategorie oder ein Modul filtert die Findings-Tabelle. Die File:Line-Links springen in
        den GitLab-Blob, gepinnt an gitHash $${gitHash.take(8).escapeHtml()}.
    </p>
    <div class="triage-headline">
        <span><span class="num">$${warnings.size}</span>Warnungen</span>
        <span><span class="num">$${warnings.map { it.categoryLabel() }.distinct().size}</span>Kategorien</span>
        <span><span class="num">$${warnings.map { it.modulePath }.distinct().size}</span>Module</span>
        <span><span class="num">$${warnings.map { it.filePath }.distinct().size}</span>Files</span>
    </div>
    <div class="triage-grid">
        <div class="triage-panel">
            <h4>Top Kategorien</h4>
            <table class="summary-table">
                <tbody>
                    $$categoryRows
                </tbody>
            </table>
        </div>
        <div class="triage-panel">
            <h4>Top Module</h4>
            <table class="summary-table">
                <tbody>
                    $$moduleRows
                </tbody>
            </table>
        </div>
    </div>
    <div class="findings-filter-banner" id="findings-filter-banner-$$TriageId" data-active="false">
        <span class="findings-filter-label"></span>
        <button type="button" class="clear-filter">Filter zurücksetzen</button>
    </div>
    <div class="findings-list-wrapper" id="findings-list-$$TriageId" data-expanded="false">
        $$truncatedNote
        <table class="findings-table">
            <colgroup>
                <col style="width: 18%">
                <col style="width: 30%">
                <col style="width: 32%">
                <col style="width: 20%">
            </colgroup>
            <thead>
                <tr>
                    <th>Kategorie</th>
                    <th>File:Line</th>
                    <th>Meldung</th>
                    <th>Modul · Targets</th>
                </tr>
            </thead>
            <tbody>
                $$findingsRows
            </tbody>
        </table>
    </div>
</div>""".trimIndent()
  }

  /**
   * Wires the summary panels to the findings table. One warnings card per page, so a constant reads better
   * in the generated HTML than a random identifier.
   */
  private const val TriageId: String = "kotlin-warnings"

  /**
   * Beyond this many rows the rendered table becomes its own scrolling problem. The full set stays
   * available as the job artifact the card is generated from.
   */
  private const val FindingsCap: Int = 5000

  /** Number of entries a summary panel lists — the concentration is what the panel is for, not the tail. */
  private const val SummaryPanelSize: Int = 20

  /** Shown for a diagnostic the compiler reported without an internal name. */
  private const val UnnamedCategoryLabel: String = "(ohne Diagnose-Namen)"

  private fun KotlinWarning.categoryLabel(): String = diagnostic ?: UnnamedCategoryLabel

  /** Most frequent first; ties broken by name so the panel is stable between runs. */
  private fun List<KotlinWarning>.rankedBy(selector: (KotlinWarning) -> String): List<Pair<String, Int>> =
    groupingBy(selector)
      .eachCount()
      .toList()
      .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
      .take(SummaryPanelSize)

  private fun summaryRow(filterType: String, label: String, value: String, count: Int): String {
    val escaped = value.escapeHtml()

    return """<tr class="filter-row" data-filter-type="$filterType" data-filter-label="$label" data-filter-value="$escaped" title="$escaped">
        <td class="label"><span class="rule-name">$escaped</span></td>
        <td class="num">$count</td>
      </tr>""".trimIndent()
  }

  private fun findingsRow(warning: KotlinWarning, gitHash: String): String {
    val sourceHref = "https://git.neckar.it/neckarit/neckar-hub/-/blob/$gitHash/${warning.filePath}#L${warning.line}".escapeHtml()

    // data-diagnostic / data-module are what the index's filter script matches against.
    return """<tr data-diagnostic="${warning.categoryLabel().escapeHtml()}" data-module="${warning.modulePath.escapeHtml()}">
        <td>${warning.categoryLabel().escapeHtml()}</td>
        <td><a href="$sourceHref" target="_blank" rel="noopener">${warning.filePath.escapeHtml()}:${warning.line}</a></td>
        <td>${warning.message.escapeHtml()}</td>
        <td>${warning.modulePath.escapeHtml()} · ${warning.targets.sorted().joinToString(", ").escapeHtml()}</td>
      </tr>""".trimIndent()
  }
}
