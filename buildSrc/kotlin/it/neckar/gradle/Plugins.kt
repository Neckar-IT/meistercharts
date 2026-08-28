@file:Suppress("SpellCheckingInspection")
package it.neckar.gradle


object Plugins {
  const val dokka: String = "org.jetbrains.dokka"

  const val versions: String = "com.github.ben-manes.versions"

  const val shadowOld: String = "com.github.johnrengelman.shadow"
  const val shadow: String = "com.gradleup.shadow"

  const val mavenPublish: String = "com.vanniktech.maven.publish"

  const val launch4j: String = "edu.sc.seis.launch4j"
  const val consoleReporter: String = "com.github.ksoichiro.console.reporter"

  const val idea: String = "idea"
  const val projectReport: String = "project-report"

  const val javaLibrary: String = "java-library"

  const val java: String = "java"
  const val intellij: String = "org.jetbrains.intellij"
  const val ideaExt: String = "org.jetbrains.gradle.plugin.idea-ext"

  const val kotlinxSerialization: String = "org.jetbrains.kotlin.plugin.serialization"

  const val download: String = "de.undercouch.download"

  const val generateIcons: String = "it.neckar.generate-icons"
  const val provideSourceCodeFromBuildSrc: String = "it.neckar.source-code-from-build-src"

  const val generateTsDeclaration: String = "it.neckar.ksp.generating.ts"

  const val gitlabPipelines: String = "it.neckar.gitlab.pipelines"

  const val npmBundle: String = "it.neckar.npm-bundle"
  const val taskTree: String = "com.dorongold.task-tree"
  const val taskInfo: String = "org.barfuin.gradle.taskinfo"
  const val detekt: String = "dev.detekt"
  const val pdfOverview: String = "it.neckar.pdf-overview"
  const val html2pdf: String = "it.neckar.html2pdf"
  const val pnpmKotlinInterop: String = "it.neckar.pnpm.kotlin-interop"

  const val kotlinMultiPlatform: String = "org.jetbrains.kotlin.multiplatform"

  const val kotlinJvm: String = "org.jetbrains.kotlin.jvm"

  const val verifyMainClassExists: String = "it.neckar.verify.main-class-exists"
  const val verifyGitlabAccessToken: String = "it.neckar.verify.gitlab-access-token"

  /**
   * Registers the root-project `verify` task that checks the developer machine for all required
   * tools, versions and configurations. See VerifyDevSetupPlugin.
   */
  const val verifyDevSetup: String = "it.neckar.verify.dev-setup"

  /**
   * Registers the repo-wide verification tasks (source conventions, deploy-script rules, script
   * check runners, deployment image consistency, verifyMergeRequest aggregator) on the root
   * project. See VerifyRepositoryChecksPlugin.
   */
  const val verifyRepositoryChecks: String = "it.neckar.verify.repository-checks"

  /**
   * Lets a module declare the operating-system packages it needs to build, through a
   * `systemDependencies { }` block. See SystemDependenciesPlugin.
   */
  const val systemDependencies: String = "it.neckar.system-dependencies"

  /**
   * Root-project counterpart: aggregates the declarations of all modules behind `verify` and
   * writes both package lists that install them — the Ansible workstation setup's vars file and the
   * CI image's list. See SystemDependenciesRootPlugin.
   */
  const val systemDependenciesRoot: String = "it.neckar.system-dependencies-root"

  /**
   * Forbids project/external dependencies on a per-configuration basis.
   * See [it.neckar.gradle.fence.DependencyFencePlugin].
   */
  const val dependencyFence: String = "it.neckar.dependency-fence"

  /**
   * Convention plugin for `internal/open` modules — applies a `dependencyFence`
   * rule that bans any `internal/closed` project dependency in production scope.
   * See [it.neckar.gradle.fence.OpenModulePlugin].
   */
  const val openModule: String = "it.neckar.open-module"

  const val additionalGitRepository: String = "it.neckar.repos.additional-git-repository"
  const val generateAuditReport: String = "it.neckar.dependencies.audit-report"
  const val buildProfileReport: String = "it.neckar.report.build-profile"

  /**
   * Writes the structured build report under `build-reports/`: the streaming
   * `build-events.jsonl` (every finished task, written as it happens so a killed build still leaves a
   * parseable record) plus the aggregates `failures.json`, `cache-report.json` and
   * `cache-metrics.json`. Apply once to the root project.
   * See [it.neckar.gradle.report.events.BuildEventsReportPlugin].
   */
  const val buildEventsReport: String = "it.neckar.report.build-events"

  /**
   * Writes `build-reports/logs/<module>/<task>.log` — each task's console output, correctly
   * attributed under parallel builds via build operations. Apply once to the root project.
   * See [it.neckar.gradle.report.events.TaskOutputLogPlugin].
   */
  const val taskOutputLogs: String = "it.neckar.report.task-output-logs"

  /**
   * Writes `build-reports/kotlin-warnings.json` — every Kotlin compiler warning this build produced,
   * in the GitLab Code Quality schema, so the findings become inline annotations in the MR diff.
   * Apply once to the root project.
   * See [it.neckar.gradle.report.warnings.KotlinWarningsReportPlugin].
   */
  const val kotlinWarningsReport: String = "it.neckar.report.kotlin-warnings"

  /**
   * Writes `build-reports/artifact-sizes.json` — the size of every deployable artifact this
   * build produced (frontend bundles, Kotlin/JS bundles, archives, install trees), split by asset type,
   * raw and gzip-compressed. Exported as OTLP metrics from the main-branch CI build. Apply once to the
   * root project. See [it.neckar.gradle.report.artifacts.ArtifactSizeReportPlugin].
   */
  const val artifactSizeReport: String = "it.neckar.report.artifact-sizes"

  const val verifyPnpmWorkspaceYaml: String = "it.neckar.repos.pnpm.verify-workspace-yaml"
  const val verifyPnpmWorkspaceDependencies: String = "it.neckar.repos.pnpm.verify-workspace-dependencies"
  @Deprecated("Use disableDistTasks instead", ReplaceWith("disableDistTasks"))
  const val skipDistForApplication: String = "it.neckar.performance.skip-dist-for-application"
  @Deprecated("Use disableDistTasks instead", ReplaceWith("disableDistTasks"))
  const val skipShadowDistZipForShadowPlugin: String = "it.neckar.performance.skip-shadow-dist-zip-for-shadow"
  const val disableDistTasks: String = "it.neckar.performance.disable-dist-tasks"

  /**
   * Applies the repository-wide strategy for third-party metadata files in fat jars
   * (license/notice texts, service-provider registries, build-time-only metadata). Reacts to the
   * Shadow plugin, so it can be applied to every project unconditionally.
   */
  const val fatJarMetadata: String = "it.neckar.shadow.fat-jar-metadata"

  /**
   * Convention plugin bundling `application`, `disableDistTasks`, and `verifyMainClassExists`.
   * For CLI tools, demos, and other executable applications without Docker deployment.
   */
  const val executableApplication: String = "it.neckar.executable-application"

  /**
   * Convention plugin for Ktor services deployed as Docker images.
   * Extends `executableApplication` with `jibService` for Docker image building, the OTel agent,
   * and the Ktor service conventions (dependency backbone, Jib defaults, run-task port override).
   */
  const val ktorServiceApplication: String = "it.neckar.ktor-service-application"
  const val generateIgnoreProjectSets: String = "it.neckar.generation.ignore-project-sets"
  const val generateTypesList: String = "it.neckar.generation.types-list"
  const val runDockerServices: String = "it.neckar.docker.services"
  const val ngrokTunnel: String = "it.neckar.ngrok-tunnel"

  const val certificates: String = "it.neckar.ssl.certificates"

  /**
   * Collects the types-list (from dependencies or subprojects).
   * Does *NOT* generate the types-list for the project. Use [generateTypesList] instead!
   */
  const val typesListCollector: String = "it.neckar.generation.types-list-collector"

  /**
   * Configures python projects
   */
  const val python: String = "it.neckar.python"

  /**
   * Wires a product that owns a spec project to the extracted spec generator
   */
  const val specGenerator: String = "it.neckar.spec-generator"
  const val base: String = "org.gradle.base"
  const val application: String = "application"

  /**
   * JMH micro benchmarking framework
   */
  const val jmh: String = "me.champeau.jmh"

  /**
   * Creates a license report (used for SDD development)
   */
  const val licenseReport: String = "it.neckar.license-report"

  const val spotless: String = "com.diffplug.spotless"

  const val jibCli: String = "it.neckar.jib-cli"
  const val jibService: String = "it.neckar.jib-service"

  /**
   * Single default source for the OTel agent config: `-javaagent` + `OTEL_*` env defaults on the
   * Jib image and the local `run` task, `otelAgent { }` DSL, extension-JAR bake (#2381).
   * Bundled via `ktorServiceApplication`.
   */
  const val otelAgent: String = "it.neckar.otel-agent"
  const val dockerHubPublish: String = "it.neckar.docker-hub-publish"

  const val asciidoctor: String = "org.asciidoctor.jvm.convert"
  const val asciidoctorPdf: String = "org.asciidoctor.jvm.pdf"
  const val asciidoctorGems: String = "org.asciidoctor.jvm.gems"
  const val neckarItAsciidoctor: String = "it.neckar.asciidoctor"

  const val kvision: String = "kvision"
  const val jib: String = "com.google.cloud.tools.jib"


  const val schemaGen: String = "com.javagen.schema-gen"

  const val node: String = "com.github.node-gradle.node"
  const val kover: String = "org.jetbrains.kotlinx.kover"
  const val analyze: String = "ca.cutterslade.analyze"

  const val webResourcesFromDependencies: String = "it.neckar.web-resources-from-dependencies"

  /**
   * Provides a processResources task for projects without Java/Kotlin plugin.
   * Useful for deployment projects and infrastructure configurations.
   */
  const val resourcesConvention: String = "it.neckar.resources-convention"

  const val secretsLoader: String = "it.neckar.secrets-loader"

  /**
   * Generates a self-contained `deploy` task (host + tag baked into the script) for a module.
   */
  const val deployment: String = "it.neckar.deployment"

  /**
   * Continuous-deploy opt-in for non-image deploys (#2469): the module declares its own
   * deploy tasks + target label instead of hosts/compose.
   */
  const val customDeployment: String = "it.neckar.custom-deployment"

  /**
   * Generates a self-contained `provision` task (host baked into the script) for one-time host provisioning.
   */
  const val provisioning: String = "it.neckar.provisioning"
  const val openapiValidator: String = "it.neckar.openapi.validator"
  const val openapiGenerationConfig: String = "it.neckar.openapi.generation-config"
  const val orvalConvert: String = "it.neckar.openapi.orval-convert"

  /**
   * Only for JavaFX 17+
   */
  const val javafx: String = "org.openjfx.javafxplugin"


  const val ksp: String = "com.google.devtools.ksp"
  const val kspBoxing: String = "it.neckar.ksp.boxing"
  const val specHarvest: String = "it.neckar.ksp.spec-harvest"

  const val tailwind: String = "it.neckar.tailwind"
  const val keycloakClient: String = "it.neckar.keycloak-client"
  const val linksSiteGenerate: String = "it.neckar.links-site-generate"

  /**
   * Convention plugin for local development environments.
   * Registers a `dev` task that starts a tmux session with the correct panes
   * for the product type (backend, frontend, website).
   */
  const val localDev: String = "it.neckar.local-dev"

  /**
   * Verifies serialization related stuff (ser `it.neckar.ksp.serialization.SerializationVerifierProcessor`)
   */
  const val kspSerialization: String = "it.neckar.ksp.serialization"
}
