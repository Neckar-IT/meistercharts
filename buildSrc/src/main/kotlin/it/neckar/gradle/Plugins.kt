@file:Suppress("SpellCheckingInspection")
package it.neckar.gradle


object Plugins {
  const val Dokka: String = "org.jetbrains.dokka"

  const val Versions: String = "com.github.ben-manes.versions"

  const val ShadowOld: String = "com.github.johnrengelman.shadow"
  const val Shadow: String = "com.gradleup.shadow"

  const val MavenPublish: String = "com.vanniktech.maven.publish"

  const val Launch4j: String = "edu.sc.seis.launch4j"
  const val ConsoleReporter: String = "com.github.ksoichiro.console.reporter"

  const val Idea: String = "idea"
  const val ProjectReport: String = "project-report"

  const val JavaLibrary: String = "java-library"

  const val Java: String = "java"
  const val IntellijPlatform: String = "org.jetbrains.intellij.platform"
  const val IdeaExt: String = "org.jetbrains.gradle.plugin.idea-ext"

  const val KotlinxSerialization: String = "org.jetbrains.kotlin.plugin.serialization"

  const val Download: String = "de.undercouch.download"

  const val GenerateIcons: String = "it.neckar.generate-icons"
  const val ProvideSourceCodeFromBuildSrc: String = "it.neckar.source-code-from-build-src"

  const val GenerateTsDeclaration: String = "it.neckar.ksp.generating.ts"

  const val GitlabPipelines: String = "it.neckar.gitlab.pipelines"

  const val npmBundle: String = "it.neckar.npm-bundle"
  const val TaskTree: String = "com.dorongold.task-tree"
  const val Detekt: String = "dev.detekt"
  const val pdfOverview: String = "it.neckar.pdf-overview"
  const val html2pdf: String = "it.neckar.html2pdf"
  const val pnpmKotlinInterop: String = "it.neckar.pnpm.kotlin-interop"

  const val KotlinMultiPlatform: String = "org.jetbrains.kotlin.multiplatform"

  const val KotlinJvm: String = "org.jetbrains.kotlin.jvm"

  const val VerifyMainClassExists: String = "it.neckar.verify.main-class-exists"
  const val VerifyGitlabAccessToken: String = "it.neckar.verify.gitlab-access-token"

  /**
   * Registers the root-project `verify` task that checks the developer machine for all required
   * tools, versions and configurations. See VerifyDevSetupPlugin.
   */
  const val VerifyDevSetup: String = "it.neckar.verify.dev-setup"

  /**
   * Registers the repo-wide verification tasks (source conventions, deploy-script rules, script
   * check runners, deployment image consistency, verifyMergeRequest aggregator) on the root
   * project. See VerifyRepositoryChecksPlugin.
   */
  const val VerifyRepositoryChecks: String = "it.neckar.verify.repository-checks"

  /**
   * Inserts the build-guard hook into the `gradlew` the `wrapper` task generates and registers
   * `verifyGradleWrapper`. Apply once to the root project. See GradleWrapperHookPlugin.
   */
  const val GradleWrapperHook: String = "it.neckar.gradle-wrapper-hook"

  /**
   * Lets a module declare the operating-system packages it needs to build, through a
   * `systemDependencies { }` block. See SystemDependenciesPlugin.
   */
  const val SystemDependencies: String = "it.neckar.system-dependencies"

  /**
   * Root-project counterpart: aggregates the declarations of the root build and every module behind
   * `verify` and writes both package lists that install them — the Ansible workstation setup's vars
   * file and the CI image's list. See SystemDependenciesRootPlugin.
   */
  const val SystemDependenciesRoot: String = "it.neckar.system-dependencies-root"

  /**
   * Forbids project/external dependencies on a per-configuration basis.
   * See [it.neckar.gradle.fence.DependencyFencePlugin].
   */
  const val DependencyFence: String = "it.neckar.dependency-fence"

  /**
   * Convention plugin for `internal/open` modules — applies a `dependencyFence`
   * rule that bans any `internal/closed` project dependency in production scope.
   * See [it.neckar.gradle.fence.OpenModulePlugin].
   */
  const val OpenModule: String = "it.neckar.open-module"

  const val AdditionalGitRepository: String = "it.neckar.repos.additional-git-repository"
  const val GenerateAuditReport: String = "it.neckar.dependencies.audit-report"
  const val BuildProfileReport: String = "it.neckar.report.build-profile"

  /**
   * Writes the structured build report into `build-reports/runs/<runId>/`: the streaming
   * `build-events.jsonl` (every finished task, written as it happens so a killed build still leaves a
   * parseable record) plus the aggregates `build-result.json`, `cache-report.json`, `cache-metrics.json`
   * and `configuration-phase-metrics.json`. Apply once to the root project.
   * See [it.neckar.gradle.report.events.BuildEventsReportPlugin].
   */
  const val BuildEventsReport: String = "it.neckar.report.build-events"

  /**
   * Writes `build-reports/runs/<runId>/logs/<module>/<task>.log` — each task's console output, correctly
   * attributed under parallel builds via build operations. Apply once to the root project.
   * See [it.neckar.gradle.report.events.TaskOutputLogPlugin].
   */
  const val TaskOutputLogs: String = "it.neckar.report.task-output-logs"

  /**
   * Writes `build-reports/runs/<runId>/kotlin-warnings.json` — every Kotlin compiler warning this build produced,
   * in the GitLab Code Quality schema, so the findings become inline annotations in the MR diff.
   * Apply once to the root project.
   * See [it.neckar.gradle.report.warnings.KotlinWarningsReportPlugin].
   */
  const val KotlinWarningsReport: String = "it.neckar.report.kotlin-warnings"

  /**
   * Writes `build-reports/runs/<runId>/artifact-sizes.json` — the size of every deployable artifact this
   * build produced (frontend bundles, Kotlin/JS bundles, archives, install trees), split by asset type,
   * raw and gzip-compressed. Exported as OTLP metrics from the main-branch CI build. Apply once to the
   * root project. See [it.neckar.gradle.report.artifacts.ArtifactSizeReportPlugin].
   */
  const val ArtifactSizeReport: String = "it.neckar.report.artifact-sizes"

  const val VerifyPnpmWorkspaceYaml: String = "it.neckar.repos.pnpm.verify-workspace-yaml"
  const val VerifyPnpmWorkspaceDependencies: String = "it.neckar.repos.pnpm.verify-workspace-dependencies"
  const val VerifyPnpmLockfilePeerVariants: String = "it.neckar.repos.pnpm.verify-lockfile-peer-variants"
  const val DisableDistTasks: String = "it.neckar.performance.disable-dist-tasks"

  /**
   * Applies the repository-wide strategy for third-party metadata files in fat jars
   * (license/notice texts, service-provider registries, build-time-only metadata). Reacts to the
   * Shadow plugin, so it can be applied to every project unconditionally.
   */
  const val FatJarMetadata: String = "it.neckar.shadow.fat-jar-metadata"

  /** See [it.neckar.gradle.dependencies.CapabilityConflictPlugin]. */
  const val CapabilityConflicts: String = "it.neckar.dependencies.capability-conflicts"

  /**
   * Registers `verifyUnusedDependencies`, run on request, failing on a declared dependency the module does not use. Reacts to
   * `com.autonomousapps.dependency-analysis`, so it can be applied to every project unconditionally.
   */
  const val UnusedDependencies: String = "it.neckar.dependencies.unused-dependencies"

  /**
   * Convention plugin bundling `application`, `disableDistTasks`, and `verifyMainClassExists`.
   * For CLI tools, demos, and other executable applications without Docker deployment.
   */
  const val ExecutableApplication: String = "it.neckar.executable-application"

  /**
   * Convention plugin for Ktor services deployed as Docker images.
   * Extends `executableApplication` with `jibService` for Docker image building, the OTel agent,
   * and the Ktor service conventions (dependency backbone, Jib defaults, run-task port override).
   */
  const val KtorServiceApplication: String = "it.neckar.ktor-service-application"
  const val GenerateTypesList: String = "it.neckar.generation.types-list"
  const val RunDockerServices: String = "it.neckar.docker.services"
  const val NgrokTunnel: String = "it.neckar.ngrok-tunnel"

  const val Certificates: String = "it.neckar.ssl.certificates"

  /**
   * Collects the types-list (from dependencies or subprojects).
   * Does *NOT* generate the types-list for the project. Use [GenerateTypesList] instead!
   */
  const val TypesListCollector: String = "it.neckar.generation.types-list-collector"

  /**
   * Configures python projects
   */
  const val Python: String = "it.neckar.python"

  /**
   * Wires a product that owns a spec project to the extracted spec generator
   */
  const val SpecGenerator: String = "it.neckar.spec-generator"
  const val Base: String = "org.gradle.base"
  const val Application: String = "application"

  /**
   * JMH micro benchmarking framework
   */
  const val jmh: String = "me.champeau.jmh"

  /**
   * Creates a license report (used for SDD development)
   */
  const val LicenseReport: String = "it.neckar.license-report"

  const val Spotless: String = "com.diffplug.spotless"

  const val JibCli: String = "it.neckar.jib-cli"
  const val JibService: String = "it.neckar.jib-service"

  /**
   * Judges the HTML a project deploys: no `<script>` carrying code, because a document answering
   * under `script-src 'self'` has it parsed into the DOM and never run. Bundled via `frontendProject`.
   */
  const val NoInlineScripts: String = "it.neckar.no-inline-scripts"

  /**
   * Checks that the built output of a frontend carries `health/live`, the file every probe reads
   * the frontend's liveness from at `/health/live`. Bundled via `frontendProject`.
   */
  const val LivenessFile: String = "it.neckar.liveness-file"

  /**
   * The convention of every project registered with `ProjectRole.Frontend`, applied by `configureProjects`
   * rather than a plugins block: names what the frontend delivers once (`frontendProject { output }`) and
   * applies `noInlineScripts` and `livenessFile` to it.
   */
  const val FrontendProject: String = "it.neckar.frontend-project"

  /**
   * Single default source for the OTel agent config: `-javaagent` + `OTEL_*` env defaults on the
   * Jib image and the local `run` task, `otelAgent { }` DSL, extension-JAR bake (#2381).
   * Bundled via `ktorServiceApplication`.
   */
  const val otelAgent: String = "it.neckar.otel-agent"
  const val DockerHubPublish: String = "it.neckar.docker-hub-publish"

  const val Asciidoctor: String = "org.asciidoctor.jvm"
  const val AsciidoctorPdf: String = "org.asciidoctor.jvm.pdf"
  const val AsciidoctorDiagram: String = "org.asciidoctor.jvm.diagram"
  const val NeckarItAsciidoctor: String = "it.neckar.asciidoctor"

  const val Kvision: String = "kvision"
  const val Jib: String = "com.google.cloud.tools.jib"


  const val SchemaGen: String = "com.javagen.schema-gen"

  const val Node: String = "com.github.node-gradle.node"
  const val Kover: String = "org.jetbrains.kotlinx.kover"
  const val Analyze: String = "ca.cutterslade.analyze"

  const val WebResourcesFromDependencies: String = "it.neckar.web-resources-from-dependencies"

  /**
   * Provides a processResources task for projects without Java/Kotlin plugin.
   * Useful for deployment projects and infrastructure configurations.
   */
  const val ResourcesConvention: String = "it.neckar.resources-convention"

  /** `secretStore { }`: the sources a module loads its secrets from, and `slot(key)` for a producer that writes one. */
  const val SecretStore: String = "it.neckar.secret-store"

  /** The former ID of [SecretStore], still named by the standalone builds. */
  const val SecretsLoader: String = "it.neckar.secrets-loader"

  /** `mattermostBot { }`: the Mattermost bot of the module that posts with it, its token in a `SecretSlot`. */
  const val MattermostBot: String = "it.neckar.mattermost-bot"

  /** `mattermost.neckar.it` only: `applyMattermostBots` over every `mattermostBot { }` block. */
  const val MattermostBots: String = "it.neckar.mattermost-bots"

  /** `hostDeployment { }`: the deploy of a host module, whose first step brings the host system. */
  const val HostDeployment: String = "it.neckar.host-deployment"

  /** `serviceDeployment { }`: the deploy of every module with its own deploy that is no host module. */
  const val ServiceDeployment: String = "it.neckar.service-deployment"

  /** The root build only: the deployment rules that read every module, from the images the jib modules push to `deployAllServices`. */
  const val DeploymentBuildRules: String = "it.neckar.deployment.build-rules"

  /**
   * The store of a service module, pulled once a day by `backups-host.neckar.it`:
   * `backup { daily(pullTime = LocalTime(3, 0), timeZone = TimeZone.of("Europe/Berlin")) }`.
   */
  const val Backup: String = "it.neckar.backup"

  /**
   * Check time and default retention of `backups.neckar.it`, the backup service on `backups-host.neckar.it`; collects every `backup { }` block.
   */
  const val BackupSchedule: String = "it.neckar.backup-schedule"

  /**
   * The delivery targets of a product with trigger, conditions and retention:
   * `delivery { product = ProductId("…"); host("…", Fleet.HafenHost) { … } }`.
   */
  const val Delivery: String = "it.neckar.delivery"

  /** The root project of every build: the git and environment variables `gitHash`, `branch`, `ciInformation` and their siblings read. */
  const val BuildVariables: String = "it.neckar.build-variables"

  /** The `settings.gradle.kts` of a project building on its own: the build logic on every build script classpath, the root build's conventions. */
  const val StandaloneSettings: String = "it.neckar.standalone-settings"

  /** The root build only: `generateProductsCatalog` writes `products-catalog.yaml` and `60-catalog.ssh_config`, `verifyProductsCatalog` compares them. */
  const val ProductsCatalog: String = "it.neckar.products-catalog"

  /** The root build only: one task per `DeliveryConditions` entry answers it for `-Pproduct` and records the answer in the delivery log. */
  const val DeliveryConditions: String = "it.neckar.delivery-conditions"

  /**
   * A machine with its kind, inventory source and SSH access:
   * `hostDeclaration { kind = HostKind.Server; inventory = HostInventorySource.HetznerCloud; … }`.
   */
  const val HostDeclaration: String = "it.neckar.host-declaration"

  /**
   * The subdomains of the local infrastructure services on `localhost`:
   * `localhostInfrastructure { subdomains = listOf(LocalhostSubdomain("s3"), …) }`.
   */
  const val LocalhostInfrastructure: String = "it.neckar.localhost-infrastructure"

  /**
   * A directory whose direct modules carry the prefix of its name: `verifyModuleGroup` at `check`.
   */
  const val ModuleGroup: String = "it.neckar.module-group"

  /**
   * The deploy declaration of a non-image deploy: the module declares its own deploy tasks, their targets and their deploy mode
   * instead of hosts/compose.
   */
  const val CustomDeployment: String = "it.neckar.custom-deployment"

  const val OpenapiValidator: String = "it.neckar.openapi.validator"
  const val OpenapiGenerationConfig: String = "it.neckar.openapi.generation-config"
  const val OrvalConvert: String = "it.neckar.openapi.orval-convert"

  /**
   * Only for JavaFX 17+
   */
  const val Javafx: String = "org.openjfx.javafxplugin"


  const val ksp: String = "com.google.devtools.ksp"
  const val kspBoxing: String = "it.neckar.ksp.boxing"
  const val SpecHarvest: String = "it.neckar.ksp.spec-harvest"

  const val Tailwind: String = "it.neckar.tailwind"

  /** `keycloakClient("<clientId>") { }`: a Keycloak client of the module that uses it; a confidential one names the `SecretSlot` of its client secret. */
  const val KeycloakClient: String = "it.neckar.keycloak-client"

  /** `auth.neckar.it` only: `generateKeycloakClients` writes every `keycloakClient("<clientId>") { }` block into the input of the OpenTofu root module. */
  const val KeycloakClients: String = "it.neckar.keycloak-clients"

  const val OpenTofu: String = "it.neckar.opentofu"
  const val LinksSiteGenerate: String = "it.neckar.links-site-generate"

  /**
   * Convention plugin for local development environments.
   * Registers a `dev` task that starts a tmux session with the correct panes
   * for the product type (backend, frontend, website).
   */
  const val LocalDev: String = "it.neckar.local-dev"

  /**
   * The `productDeclaration { }`, `serviceDeclaration { }`, `frontendDeclaration { }` and `infrastructureDeclaration { }` blocks and the `HostLogs` Dozzle, exported for other projects;
   * product and service are also generated as constants (ADL 0202, `docs/gradle/project-declaration.md`).
   */
  const val ProjectDeclaration: String = "it.neckar.project-declaration"

  /**
   * Verifies serialization related stuff (ser `it.neckar.ksp.serialization.SerializationVerifierProcessor`)
   */
  const val kspSerialization: String = "it.neckar.ksp.serialization"
}
