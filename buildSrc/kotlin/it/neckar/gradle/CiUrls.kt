package it.neckar.gradle

import org.gradle.api.Project

/**
 * CI pipeline URL from GitLab CI environment (null when not running in CI).
 */
val Project.ciPipelineUrl: String?
  get() = System.getenv("CI_PIPELINE_URL")

/**
 * CI job URL from GitLab CI environment (null when not running in CI).
 */
val Project.ciJobUrl: String?
  get() = System.getenv("CI_JOB_URL")
