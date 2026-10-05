package it.neckar.ci

/** Prefix every schedule-gating CI variable name must carry. Compile-time const so it is usable in the enum's init block. */
private const val SchedulePrefix: String = "SCHEDULE_"

/**
 * Single source of truth for the `SCHEDULE_*` CI variables that gate scheduled
 * pipelines in `neckarit/neckar-hub`.
 *
 * Set to `"true"` on a GitLab pipeline schedule (configured in [PipelineSchedules])
 * to activate the matching include in `.gitlab-ci.yml`. Build files use
 * `Project.inSchedule(variable)` with an entry instead of a raw string, so a typo
 * turns into a compile error instead of a silent `false`.
 *
 * ## One name per schedule
 *
 * A schedule is named `<verb>-<object>` in [scheduleName] — English, imperative, lower case. Every
 * other spelling of that schedule is derived from it, so none of them can drift:
 *
 * | Derived | Form | Example |
 * |---|---|---|
 * | enum constant | PascalCase | `VerifyBaseImageLocks` |
 * | [variableName] | `SCHEDULE_` + SCREAMING_SNAKE | `SCHEDULE_VERIFY_BASE_IMAGE_LOCKS` |
 * | job file under `gitlab-ci.d/schedule/` | the name plus `.yml` | `verify-base-image-locks.yml` |
 * | `PipelineSchedule.description` | the name in title case | `Verify Base Image Locks` |
 * | job titles inside the file | `⏲ ` plus a [ScheduleVerb] and its object | `⏲ verify the base image locks` |
 *
 * Only the description and the job titles may spell a proper name the way it is written —
 * `Mirror JWKS Seeds`, not `Mirror Jwks Seeds`. `verifyGitlabCiVariables` compares them against
 * [scheduleName] with case and separators removed, so the spelling is free and the words are not.
 *
 * The verb comes first because the schedule overview reads as a list of what the pipeline does. It
 * comes from [ScheduleVerb], so "sync" cannot be used where only one direction exists and a job that
 * runs tests is not named after the Gradle task that happens to run them.
 *
 * `verifyGitlabCiVariables` enforces all of it, and additionally cross-checks that every
 * `$SCHEDULE_*` reference in `.gitlab-ci.yml` resolves to an entry here and that every entry is
 * referenced at least once (dead-entry detection).
 */
enum class ScheduleVariable(
  val variableName: String,
  val description: String,
) {
  BuildDockerImages("SCHEDULE_BUILD_DOCKER_IMAGES", "Schedule: build and push every Docker image"),
  DeployLizergySpielwiese("SCHEDULE_DEPLOY_LIZERGY_SPIELWIESE", "Schedule: nightly deploy of the Lizergy Spielwiese environment"),
  DeployAuthorizedKeys("SCHEDULE_DEPLOY_AUTHORIZED_KEYS", "Schedule: deploy the declared authorized_keys to every host"),
  MirrorInfrastructureData("SCHEDULE_MIRROR_INFRASTRUCTURE_DATA", "Schedule: mirror the host inventory into the repository"),
  MirrorJwksSeeds("SCHEDULE_MIRROR_JWKS_SEEDS", "Schedule: mirror the realms' published public signature keys into the checked-in JWKS seed files, opens a merge request when a realm rotated a key. Rotates nothing and writes nothing back to Keycloak"),
  PublishMeisterchartsToGithub("SCHEDULE_PUBLISH_MEISTERCHARTS_TO_GITHUB", "Schedule: publish the MeisterCharts source code to the GitHub develop branch"),
  PublishReports("SCHEDULE_PUBLISH_REPORTS", "Schedule: full report sweep (build + tests + Detekt + Kover + pnpm-audit + build profile) and reports.neckar.it image rebuild"),
  PublishSonatypeSnapshots("SCHEDULE_PUBLISH_SONATYPE_SNAPSHOTS", "Schedule: deploy the open-source artifacts to Sonatype Snapshots"),
  ReconcileContinuousDeploy("SCHEDULE_RECONCILE_CONTINUOUS_DEPLOY", "Schedule: re-run the main build and deploy in reconcile mode, catching up whatever a failed main pipeline left undeployed"),
  ResetSnapshotHistory("SCHEDULE_RESET_SNAPSHOT_HISTORY", "Schedule: weekly rolling-squash of the neckar-hub-gitlab-data snapshot history (collapse commits older than 7 days)"),
  RunChromeTests("SCHEDULE_RUN_CHROME_TESTS", "Schedule: Chrome browser integration tests"),
  RunHousekeeping("SCHEDULE_RUN_HOUSEKEEPING", "Schedule: housekeeping jobs (license headers, lock files, generated sources, registry cleanup)"),
  RunLadleTests("SCHEDULE_RUN_LADLE_TESTS", "Schedule: Playwright suites of every package that serves Ladle stories, screenshot comparisons included"),
  RunMeisterchartsE2eTests("SCHEDULE_RUN_MEISTERCHARTS_E2E_TESTS", "Schedule: MeisterCharts end-to-end tests, publishes the report"),
  RunPublicSiteTests("SCHEDULE_RUN_PUBLIC_SITE_TESTS", "Schedule: end-to-end watch over the public websites, publishes the report"),
  RunRenovate("SCHEDULE_RUN_RENOVATE", "Schedule: Renovate dependency update bot"),
  SnapshotElektromeisterMongodb("SCHEDULE_SNAPSHOT_ELEKTROMEISTER_MONGODB", "Schedule: nightly Elektromeister MongoDB snapshot upload to the Package Registry"),
  TestElektromeisterMigration("SCHEDULE_TEST_ELEKTROMEISTER_MIGRATION", "Schedule: nightly Elektromeister migration test against the production snapshot"),
  TestLizergySpielwiese("SCHEDULE_TEST_LIZERGY_SPIELWIESE", "Schedule: nightly Playwright end-to-end tests against Lizergy Spielwiese, publishes the report image"),
  VerifyBaseImageLocks("SCHEDULE_VERIFY_BASE_IMAGE_LOCKS", "Schedule: watchdog that verifies the pinned base image digests against the registry and their source commits against the git history"),
  VerifyDockerImageTags("SCHEDULE_VERIFY_DOCKER_IMAGE_TAGS", "Schedule: retraction watchdog that verifies the pinned Docker image tags and digests and the pinned downloads still exist"),
  VerifyStandaloneBuilds("SCHEDULE_VERIFY_STANDALONE_BUILDS", "Schedule: weekly watchdog that builds every standalone-capable project from an empty Gradle home, so its dependencies must resolve from the public repositories"),
  ;

  /**
   * The one name this schedule is known by, `<verb>-<object>` — `verify-base-image-locks`. Names its
   * job file, and is what the enum constant, the [variableName], the description and the job titles
   * are checked against.
   */
  val scheduleName: String
    get() = variableName.removePrefix(SchedulePrefix).lowercase().replace('_', '-')

  /**
   * The file under `gitlab-ci.d/schedule/` holding this schedule's jobs.
   *
   * [ReconcileContinuousDeploy] has none — it loads `gitlab-ci.d/main.yml`, the same jobs the main
   * branch pipeline runs. `verifyGitlabCiVariables` therefore checks the name against whatever file
   * `.gitlab-ci.yml` includes for this variable, rather than requiring this one to exist.
   */
  val jobFileName: String
    get() = "$scheduleName.yml"

  init {
    require(variableName.startsWith(SchedulePrefix)) { "Schedule variable name must start with `$SchedulePrefix`: $variableName" }
    require(variableName == variableName.uppercase()) { "Schedule variable name must be upper case: $variableName" }
    require(name == scheduleName.split('-').joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }) {
      "Enum constant must be the PascalCase of '$scheduleName' but is '$name'"
    }
    require(ScheduleVerb.of(scheduleName.substringBefore('-')) != null) {
      "Schedule '$scheduleName' must start with one of ${ScheduleVerb.entries.map { it.word }} — see ScheduleVerb"
    }
  }

  companion object {
    const val Prefix: String = SchedulePrefix

    /**
     * [text] reduced to its lower-case letters and digits.
     *
     * How a description is compared against a [scheduleName]: `Mirror JWKS Seeds` and
     * `mirror-jwks-seeds` must agree on the words, not on how they are capitalised or separated.
     */
    fun normalizeName(text: String): String = text.lowercase().filter { it.isLetterOrDigit() }
  }
}

/**
 * The verbs a scheduled job may be named after.
 *
 * A closed list, so the schedule overview reads as one list of actions rather than a mix of nouns
 * (`Housekeeping`), Gradle task names (`gradle chromeTests`) and verbs that claim more than the job
 * does. `sync` is deliberately absent: every job here moves data in one direction, and a word that
 * suggests two hides which one.
 *
 * Adding a verb is allowed and cheap — adding a *synonym* of one already here is what turns the
 * overview back into prose.
 */
enum class ScheduleVerb(val word: String) {
  /** Produces an artifact from sources. */
  Build("build"),

  /** Removes what is no longer needed. */
  CleanUp("clean"),

  /** Transfers a state onto a running host or service, as https://glossary.neckar.it defines it. */
  Deploy("deploy"),

  /** Writes a file this repository holds from a generator. */
  Generate("generate"),

  /** Copies an external state into this repository, one direction. Never `sync`. */
  Mirror("mirror"),

  /** Makes an artifact available outside this repository. */
  Publish("publish"),

  /** Brings a system back to what the repository declares. */
  Reconcile("reconcile"),

  /** Discards accumulated state and starts it over. */
  Reset("reset"),

  /** Executes something whose own name says what it does. */
  Run("run"),

  /** Copies a live data set to a point-in-time archive. */
  Snapshot("snapshot"),

  /** Exercises a system and reports whether it behaved. */
  Test("test"),

  /** Rewrites a file this repository holds to a newer state. */
  Update("update"),

  /** Checks a claim and fails when it no longer holds; changes nothing. */
  Verify("verify"),
  ;

  companion object {
    fun of(word: String): ScheduleVerb? = entries.find { it.word == word }
  }
}
