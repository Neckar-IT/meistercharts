@file:Suppress("SpellCheckingInspection")

import Plugins.generateTypesList
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

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

  const val kotlinxSerialization: String = "org.jetbrains.kotlin.plugin.serialization"

  const val download: String = "de.undercouch.download"

  const val generateIcons: String = "it.neckar.generate-icons"
  const val provideSourceCodeFromBuildSrc: String = "it.neckar.source-code-from-build-src"

  const val generateTsDeclaration: String = "it.neckar.ksp.generating.ts-plugin"

  const val gitLabDb: String = "it.neckar.gitlab.db"

  const val npmBundle: String = "it.neckar.npm-bundle"
  const val taskTree: String = "com.dorongold.task-tree"
  const val taskInfo: String = "org.barfuin.gradle.taskinfo"
  const val detekt: String = "io.gitlab.arturbosch.detekt"
  const val pdfOverview: String = "it.neckar.pdf-overview"
  const val pnpmKotlinInterop: String = "it.neckar.gradle.pnpm.kotlin-interop"

  const val kotlinMultiPlatform: String = "org.jetbrains.kotlin.multiplatform"

  const val kotlinJvm: String = "org.jetbrains.kotlin.jvm"

  const val verifyMainClassExists: String = "it.neckar.verify.main-class-exists"
  const val verifyGitlabAccessToken: String = "it.neckar.verify.gitlab-access-token"

  const val additionalGitRepository: String = "it.neckar.repos.additional-git-repository"
  const val generatePackageJson: String = "it.neckar.repos.generate-package-json"
  const val generateViteEnvFile: String = "it.neckar.repos.generate-vite-env-file"
  const val installPnpmDependency: String = "it.neckar.repos.install-pnpm-dependency"
  const val generateAuditReport: String = "it.neckar.gradle.dependencies.audit-report"
  const val buildProfileReport: String = "it.neckar.gradle.report.build-profile"
  const val generatePnpmWorkspaceYaml: String = "it.neckar.repos.pnpm.generate-workspace-yaml"
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
  const val dockerGenerateObjects: String = "it.neckar.docker.generate-objects"
  const val runDockerServices: String = "it.neckar.docker.services"
  const val ngrokTunnel: String = "it.neckar.ngrok-tunnel"

  const val publishToGitlabPages: String = "it.neckar.publish.gitlab-pages"
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

inline val PluginDependenciesSpec.kotlinJvm: PluginDependencySpec
  get() = id(Plugins.kotlinJvm)

inline val PluginDependenciesSpec.java: PluginDependencySpec
  get() = id(Plugins.java)

inline val PluginDependenciesSpec.javaLibrary: PluginDependencySpec
  get() = id(Plugins.javaLibrary)

inline val PluginDependenciesSpec.dokka: PluginDependencySpec
  get() = id(Plugins.dokka)

inline val PluginDependenciesSpec.kotlinMultiPlatform: PluginDependencySpec
  get() = id(Plugins.kotlinMultiPlatform)

inline val PluginDependenciesSpec.kotlinxSerialization: PluginDependencySpec
  get() = id(Plugins.kotlinxSerialization)

inline val PluginDependenciesSpec.jibCli: PluginDependencySpec
  get() = id(Plugins.jibCli)

inline val PluginDependenciesSpec.jibService: PluginDependencySpec
  get() = id(Plugins.jibService)

inline val PluginDependenciesSpec.dockerHubPublish: PluginDependencySpec
  get() = id(Plugins.dockerHubPublish)

inline val PluginDependenciesSpec.shadow: PluginDependencySpec
  get() = id(Plugins.shadow)

inline val PluginDependenciesSpec.launch4j: PluginDependencySpec
  get() = id(Plugins.launch4j)

inline val PluginDependenciesSpec.download: PluginDependencySpec
  get() = id(Plugins.download)

inline val PluginDependenciesSpec.mavenPublish: PluginDependencySpec
  get() = id(Plugins.mavenPublish)

inline val PluginDependenciesSpec.versions: PluginDependencySpec
  get() = id(Plugins.versions)

inline val PluginDependenciesSpec.licenseReport: PluginDependencySpec
  get() = id(Plugins.licenseReport)

inline val PluginDependenciesSpec.spotless: PluginDependencySpec
  get() = id(Plugins.spotless)

inline val PluginDependenciesSpec.generateIcons: PluginDependencySpec
  get() = id(Plugins.generateIcons)

inline val PluginDependenciesSpec.provideSourceCodeFromBuildSrc: PluginDependencySpec
  get() = id(Plugins.provideSourceCodeFromBuildSrc)

inline val PluginDependenciesSpec.generateTsDeclaration: PluginDependencySpec
  get() = id(Plugins.generateTsDeclaration)

inline val PluginDependenciesSpec.gitLabDb: PluginDependencySpec
  get() = id(Plugins.gitLabDb)

inline val PluginDependenciesSpec.neckarItAsciidoctor: PluginDependencySpec
  get() = id(Plugins.neckarItAsciidoctor)

inline val PluginDependenciesSpec.asciidoctor: PluginDependencySpec
  get() = id(Plugins.asciidoctor)

inline val PluginDependenciesSpec.asciidoctorPdf: PluginDependencySpec
  get() = id(Plugins.asciidoctorPdf)

inline val PluginDependenciesSpec.asciidoctorGems: PluginDependencySpec
  get() = id(Plugins.asciidoctorGems)

inline val PluginDependenciesSpec.generatePackageJson: PluginDependencySpec
  get() = id(Plugins.generatePackageJson)

inline val PluginDependenciesSpec.generateViteEnvFile: PluginDependencySpec
  get() = id(Plugins.generateViteEnvFile)

inline val PluginDependenciesSpec.generateAuditReport: PluginDependencySpec
  get() = id(Plugins.generateAuditReport)

inline val PluginDependenciesSpec.buildProfileReport: PluginDependencySpec
  get() = id(Plugins.buildProfileReport)

inline val PluginDependenciesSpec.installPnpmDependency: PluginDependencySpec
  get() = id(Plugins.installPnpmDependency)

inline val PluginDependenciesSpec.npmBundle: PluginDependencySpec
  get() = id(Plugins.npmBundle)

inline val PluginDependenciesSpec.verifyMainClassExists: PluginDependencySpec
  get() = id(Plugins.verifyMainClassExists)

inline val PluginDependenciesSpec.verifyGitlabAccessToken: PluginDependencySpec
  get() = id(Plugins.verifyGitlabAccessToken)

inline val PluginDependenciesSpec.additionalGitRepository: PluginDependencySpec
  get() = id(Plugins.additionalGitRepository)

inline val PluginDependenciesSpec.publishToGitlabPages: PluginDependencySpec
  get() = id(Plugins.publishToGitlabPages)

inline val PluginDependenciesSpec.dockerGenerateObjects: PluginDependencySpec
  get() = id(Plugins.dockerGenerateObjects)

inline val PluginDependenciesSpec.runDockerServices: PluginDependencySpec
  get() = id(Plugins.runDockerServices)

inline val PluginDependenciesSpec.ngrokTunnel: PluginDependencySpec
  get() = id(Plugins.ngrokTunnel)

/**
 * Use task tree like this:
 *
 * `gradle <task 1>...<task N> taskTree`
 *
 * see https://github.com/dorongold/gradle-task-tree for documentation
 */
inline val PluginDependenciesSpec.taskTree: PluginDependencySpec
  get() = id(Plugins.taskTree)

inline val PluginDependenciesSpec.taskInfo: PluginDependencySpec
  get() = id(Plugins.taskInfo)

inline val PluginDependenciesSpec.detekt: PluginDependencySpec
  get() = id(Plugins.detekt)

inline val PluginDependenciesSpec.jmh: PluginDependencySpec
  get() = id(Plugins.jmh)

inline val PluginDependenciesSpec.pdfOverview: PluginDependencySpec
  get() = id(Plugins.pdfOverview)

inline val PluginDependenciesSpec.consoleReporter: PluginDependencySpec
  get() = id(Plugins.consoleReporter)

inline val PluginDependenciesSpec.node: PluginDependencySpec
  get() = id(Plugins.node)

inline val PluginDependenciesSpec.kvision: PluginDependencySpec
  get() = id(Plugins.kvision)

inline val PluginDependenciesSpec.jib: PluginDependencySpec
  get() = id(Plugins.jib)

inline val PluginDependenciesSpec.intellij: PluginDependencySpec
  get() = id(Plugins.intellij)

inline val PluginDependenciesSpec.python: PluginDependencySpec
  get() = id(Plugins.python)

inline val PluginDependenciesSpec.schemaGen: PluginDependencySpec
  get() = id(Plugins.schemaGen)

inline val PluginDependenciesSpec.kover: PluginDependencySpec
  get() = id(Plugins.kover)

inline val PluginDependenciesSpec.javafx: PluginDependencySpec
  get() = id(Plugins.javafx)

inline val PluginDependenciesSpec.analyze: PluginDependencySpec
  get() = id(Plugins.analyze)

inline val PluginDependenciesSpec.copyResourcesFromDependencies: PluginDependencySpec
  get() = id(Plugins.copyResourcesFromDependencies)

inline val PluginDependenciesSpec.resourcesConvention: PluginDependencySpec
  get() = id(Plugins.resourcesConvention)

inline val PluginDependenciesSpec.secretsLoader: PluginDependencySpec
  get() = id(Plugins.secretsLoader)

inline val PluginDependenciesSpec.ksp: PluginDependencySpec
  get() = id(Plugins.ksp)

inline val PluginDependenciesSpec.kspBoxing: PluginDependencySpec
  get() = id(Plugins.kspBoxing)

inline val PluginDependenciesSpec.specHarvest: PluginDependencySpec
  get() = id(Plugins.specHarvest)

inline val PluginDependenciesSpec.kspSerialization: PluginDependencySpec
  get() = id(Plugins.kspSerialization)

inline val PluginDependenciesSpec.openapiValidator: PluginDependencySpec
  get() = id(Plugins.openapiValidator)

inline val org.gradle.plugin.use.PluginDependenciesSpec.openapiGenerationConfig: PluginDependencySpec
  get() = id(Plugins.openapiGenerationConfig)

inline val PluginDependenciesSpec.generatePnpmWorkspaceYaml: PluginDependencySpec
  get() = id(Plugins.generatePnpmWorkspaceYaml)

@Suppress("DEPRECATION")
@Deprecated("Use disableDistTasks instead", ReplaceWith("disableDistTasks"))
inline val PluginDependenciesSpec.skipDistForApplication: PluginDependencySpec
  get() = id(Plugins.skipDistForApplication)

@Suppress("DEPRECATION")
@Deprecated("Use disableDistTasks instead", ReplaceWith("disableDistTasks"))
inline val PluginDependenciesSpec.skipShadowDistZipForShadowPlugin: PluginDependencySpec
  get() = id(Plugins.skipShadowDistZipForShadowPlugin)

inline val PluginDependenciesSpec.disableDistTasks: PluginDependencySpec
  get() = id(Plugins.disableDistTasks)

inline val PluginDependenciesSpec.executableApplication: PluginDependencySpec
  get() = id(Plugins.executableApplication)

inline val PluginDependenciesSpec.serviceApplication: PluginDependencySpec
  get() = id(Plugins.serviceApplication)

inline val PluginDependenciesSpec.generateIgnoreProjectSets: PluginDependencySpec
  get() = id(Plugins.generateIgnoreProjectSets)

inline val PluginDependenciesSpec.generateTypesList: PluginDependencySpec
  get() = id(Plugins.generateTypesList)

inline val PluginDependenciesSpec.typesListCollector: PluginDependencySpec
  get() = id(Plugins.typesListCollector)

inline val PluginDependenciesSpec.pnpmKotlinInterop: PluginDependencySpec
  get() = id(Plugins.pnpmKotlinInterop)

inline val PluginDependenciesSpec.tailwind: PluginDependencySpec
  get() = id(Plugins.tailwind)

inline val PluginDependenciesSpec.keycloakClient: PluginDependencySpec
  get() = id(Plugins.keycloakClient)

inline val PluginDependenciesSpec.linksSiteGenerate: PluginDependencySpec
  get() = id(Plugins.linksSiteGenerate)

inline val PluginDependenciesSpec.certificates: PluginDependencySpec
  get() = id(Plugins.certificates)

inline val PluginDependenciesSpec.localDev: PluginDependencySpec
  get() = id(Plugins.localDev)

