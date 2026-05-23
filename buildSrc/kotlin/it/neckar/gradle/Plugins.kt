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

  const val generateTsDeclaration: String = "it.neckar.ksp.generating.ts-plugin"

  const val gitLabDb: String = "it.neckar.gitlab.db"
  const val gitlabPipelines: String = "it.neckar.gitlab.pipelines"

  const val npmBundle: String = "it.neckar.npm-bundle"
  const val taskTree: String = "com.dorongold.task-tree"
  const val taskInfo: String = "org.barfuin.gradle.taskinfo"
  const val detekt: String = "dev.detekt"
  const val pdfOverview: String = "it.neckar.pdf-overview"
  const val pnpmKotlinInterop: String = "it.neckar.gradle.pnpm.kotlin-interop"

  const val kotlinMultiPlatform: String = "org.jetbrains.kotlin.multiplatform"

  const val kotlinJvm: String = "org.jetbrains.kotlin.jvm"

  const val verifyMainClassExists: String = "it.neckar.verify.main-class-exists"
  const val verifyGitlabAccessToken: String = "it.neckar.verify.gitlab-access-token"

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
  const val generateViteEnvFile: String = "it.neckar.repos.generate-vite-env-file"
  const val generateAuditReport: String = "it.neckar.gradle.dependencies.audit-report"
  const val buildProfileReport: String = "it.neckar.gradle.report.build-profile"
  const val verifyPnpmWorkspaceYaml: String = "it.neckar.repos.pnpm.verify-workspace-yaml"
  @Deprecated("Use disableDistTasks instead", ReplaceWith("disableDistTasks"))
  const val skipDistForApplication: String = "it.neckar.performance.skip-dist-for-application"
  @Deprecated("Use disableDistTasks instead", ReplaceWith("disableDistTasks"))
  const val skipShadowDistZipForShadowPlugin: String = "it.neckar.performance.skip-shadow-dist-zip-for-shadow"
  const val disableDistTasks: String = "it.neckar.performance.disable-dist-tasks"

  /**
   * Convention plugin bundling `application`, `disableDistTasks`, and `verifyMainClassExists`.
   * For CLI tools, demos, and other executable applications without Docker deployment.
   */
  const val executableApplication: String = "it.neckar.executable-application"

  /**
   * Convention plugin for Ktor services deployed as Docker images.
   * Extends `executableApplication` with `jibService` for Docker image building.
   */
  const val serviceApplication: String = "it.neckar.service-application"
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
  const val base: String = "org.gradle.base"

  /**
   * JMH micro benchmarking framework
   */
  const val jmh: String = "me.champeau.jmh"

  /**
   * Creates a license report (used for SDD development)
   */
  const val licenseReport: String = "it.neckar.license-report"

  const val spotless: String = "com.diffplug.spotless"

  const val jibCli: String = "it.neckar.jib-cli-plugin"
  const val jibService: String = "it.neckar.jib-service"
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

  const val copyResourcesFromDependencies: String = "it.neckar.copy-resources-from-dependencies"

  /**
   * Provides a processResources task for projects without Java/Kotlin plugin.
   * Useful for deployment projects and infrastructure configurations.
   */
  const val resourcesConvention: String = "it.neckar.resources-convention"

  const val secretsLoader: String = "it.neckar.secrets-loader"
  const val openapiValidator: String = "it.neckar.openapi.validator"
  const val openapiGenerationConfig: String = "it.neckar.gradle.openapi.generation-config"
  const val orvalConvert: String = "it.neckar.gradle.openapi.orval-convert"

  /**
   * Only for JavaFX 17+
   */
  const val javafx: String = "org.openjfx.javafxplugin"


  const val ksp: String = "com.google.devtools.ksp"
  const val kspBoxing: String = "it.neckar.ksp.boxing-plugin"
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
  const val kspSerialization: String = "it.neckar.ksp.serialization-plugin"
}
