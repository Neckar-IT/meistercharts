@file:Suppress("SpellCheckingInspection")

import org.gradle.plugin.use.PluginDependencySpec

object Plugins {
  const val dokka: String = "org.jetbrains.dokka"

  const val versions: String = "com.github.ben-manes.versions"

  const val shadow: String = "com.github.johnrengelman.shadow"

  const val nexusStaging: String = "io.codearte.nexus-staging"

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
  const val projectConfig: String = "it.neckar.project.config"

  const val generateTsDeclaration: String = "it.neckar.ksp.generating.ts-plugin"

  const val gitLabDb: String = "it.neckar.gitlab.db"

  const val npmBundle: String = "it.neckar.npm-bundle"
  const val taskTree: String = "com.dorongold.task-tree"
  const val detekt: String = "io.gitlab.arturbosch.detekt"
  const val pdfOverview: String = "it.neckar.pdf-overview"

  const val kotlinMultiPlatform: String = "org.jetbrains.kotlin.multiplatform"

  const val kotlinJvm: String = "org.jetbrains.kotlin.jvm"

  const val jacoco: String = "org.gradle.jacoco"
  const val pythonPswidersk: String = "com.pswidersk.python-plugin"
  const val verifyMainClassExists: String = "it.neckar.verify.main-class-exists"
  const val additionalGitRepository: String = "it.neckar.repos.additional-git-repository"
  const val generatePackageJson: String = "it.neckar.repos.generate-package-json"
  const val installPnpmDependency: String = "it.neckar.repos.install-pnpm-dependency"

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

  const val licenseFormatBase = "com.github.hierynomus.license-base"
  const val licenseFormat = "com.github.hierynomus.license"

  const val dependenciesReport: String = "it.neckar.dependencies-report"
  const val jibCli: String = "it.neckar.jib-cli-plugin"

  const val asciidoctor: String = "org.asciidoctor.jvm.convert"
  const val asciidoctorPdf: String = "org.asciidoctor.jvm.pdf"

  const val plantUml: String = "com.cosminpolifronie.gradle.plantuml"
  const val kvision: String = "kvision"
  const val jib: String = "com.google.cloud.tools.jib"

  const val springBoot: String = "org.springframework.boot"
  const val springDependencyManagement: String = "io.spring.dependency-management"

  const val schemaGen: String = "com.javagen.schema-gen"
  const val node: String = "com.github.node-gradle.node"
  const val kover: String = "org.jetbrains.kotlinx.kover"
  const val analyze: String = "ca.cutterslade.analyze"

  const val copyResourcesFromDependencies: String = "it.neckar.copy-resources-from-dependencies"

  const val dockerPasswordFromSecretFile: String = "it.neckar.docker-password-from-secret-file"
  const val secretFromPropertiesFile: String = "it.neckar.secret-from-properties-file"

  @Deprecated("Does not seem to work")
  const val ssh: String = "online.colaba.ssh"

  /**
   * Only for JavaFX 17+
   */
  const val javafx: String = "org.openjfx.javafxplugin"


  const val ksp: String = "com.google.devtools.ksp"
  const val kspBoxing: String = "it.neckar.ksp.boxing-plugin"
}

inline val org.gradle.plugin.use.PluginDependenciesSpec.kotlinJvm: PluginDependencySpec
  get() = id(Plugins.kotlinJvm)

inline val org.gradle.plugin.use.PluginDependenciesSpec.java: PluginDependencySpec
  get() = id(Plugins.java)

inline val org.gradle.plugin.use.PluginDependenciesSpec.javaLibrary: PluginDependencySpec
  get() = id(Plugins.javaLibrary)

inline val org.gradle.plugin.use.PluginDependenciesSpec.dokka: PluginDependencySpec
  get() = id(Plugins.dokka)

inline val org.gradle.plugin.use.PluginDependenciesSpec.kotlinMultiPlatform: PluginDependencySpec
  get() = id(Plugins.kotlinMultiPlatform)

inline val org.gradle.plugin.use.PluginDependenciesSpec.kotlinxSerialization: PluginDependencySpec
  get() = id(Plugins.kotlinxSerialization)

inline val org.gradle.plugin.use.PluginDependenciesSpec.jibCli: PluginDependencySpec
  get() = id(Plugins.jibCli)

inline val org.gradle.plugin.use.PluginDependenciesSpec.shadow: PluginDependencySpec
  get() = id(Plugins.shadow)

inline val org.gradle.plugin.use.PluginDependenciesSpec.launch4j: PluginDependencySpec
  get() = id(Plugins.launch4j)

inline val org.gradle.plugin.use.PluginDependenciesSpec.download: PluginDependencySpec
  get() = id(Plugins.download)

inline val org.gradle.plugin.use.PluginDependenciesSpec.nexusStaging: PluginDependencySpec
  get() = id(Plugins.nexusStaging)

inline val org.gradle.plugin.use.PluginDependenciesSpec.versions: PluginDependencySpec
  get() = id(Plugins.versions)

inline val org.gradle.plugin.use.PluginDependenciesSpec.licenseReport: PluginDependencySpec
  get() = id(Plugins.licenseReport)

inline val org.gradle.plugin.use.PluginDependenciesSpec.licenseFormat: PluginDependencySpec
  get() = id(Plugins.licenseFormat)

inline val org.gradle.plugin.use.PluginDependenciesSpec.licenseFormatBase: PluginDependencySpec
  get() = id(Plugins.licenseFormatBase)

inline val org.gradle.plugin.use.PluginDependenciesSpec.generateIcons: PluginDependencySpec
  get() = id(Plugins.generateIcons)

inline val org.gradle.plugin.use.PluginDependenciesSpec.projectConfig: PluginDependencySpec
  get() = id(Plugins.projectConfig)

inline val org.gradle.plugin.use.PluginDependenciesSpec.generateTsDeclaration: PluginDependencySpec
  get() = id(Plugins.generateTsDeclaration)

inline val org.gradle.plugin.use.PluginDependenciesSpec.gitLabDb: PluginDependencySpec
  get() = id(Plugins.gitLabDb)

inline val org.gradle.plugin.use.PluginDependenciesSpec.asciidoctor: PluginDependencySpec
  get() = id(Plugins.asciidoctor)

inline val org.gradle.plugin.use.PluginDependenciesSpec.asciidoctorPdf: PluginDependencySpec
  get() = id(Plugins.asciidoctorPdf)

inline val org.gradle.plugin.use.PluginDependenciesSpec.generatePackageJson: PluginDependencySpec
  get() = id(Plugins.generatePackageJson)

inline val org.gradle.plugin.use.PluginDependenciesSpec.installPnpmDependency: PluginDependencySpec
  get() = id(Plugins.installPnpmDependency)

inline val org.gradle.plugin.use.PluginDependenciesSpec.plantUml: PluginDependencySpec
  get() = id(Plugins.plantUml)

inline val org.gradle.plugin.use.PluginDependenciesSpec.npmBundle: PluginDependencySpec
  get() = id(Plugins.npmBundle)

inline val org.gradle.plugin.use.PluginDependenciesSpec.verifyMainClassExists: PluginDependencySpec
  get() = id(Plugins.verifyMainClassExists)

inline val org.gradle.plugin.use.PluginDependenciesSpec.additionalGitRepository: PluginDependencySpec
  get() = id(Plugins.additionalGitRepository)

inline val org.gradle.plugin.use.PluginDependenciesSpec.taskTree: PluginDependencySpec
  get() = id(Plugins.taskTree)

inline val org.gradle.plugin.use.PluginDependenciesSpec.detekt: PluginDependencySpec
  get() = id(Plugins.detekt)

inline val org.gradle.plugin.use.PluginDependenciesSpec.jmh: PluginDependencySpec
  get() = id(Plugins.jmh)

inline val org.gradle.plugin.use.PluginDependenciesSpec.pdfOverview: PluginDependencySpec
  get() = id(Plugins.pdfOverview)

inline val org.gradle.plugin.use.PluginDependenciesSpec.consoleReporter: PluginDependencySpec
  get() = id(Plugins.consoleReporter)

inline val org.gradle.plugin.use.PluginDependenciesSpec.node: PluginDependencySpec
  get() = id(Plugins.node)

inline val org.gradle.plugin.use.PluginDependenciesSpec.kvision: PluginDependencySpec
  get() = id(Plugins.kvision)

inline val org.gradle.plugin.use.PluginDependenciesSpec.jib: PluginDependencySpec
  get() = id(Plugins.jib)

inline val org.gradle.plugin.use.PluginDependenciesSpec.intellij: PluginDependencySpec
  get() = id(Plugins.intellij)

inline val org.gradle.plugin.use.PluginDependenciesSpec.springBoot: PluginDependencySpec
  get() = id(Plugins.springBoot)

@Deprecated("Use the python plugin instead")
inline val org.gradle.plugin.use.PluginDependenciesSpec.pythonPswidersk: PluginDependencySpec
  get() = id(Plugins.pythonPswidersk)

inline val org.gradle.plugin.use.PluginDependenciesSpec.python: PluginDependencySpec
  get() = id(Plugins.python)

inline val org.gradle.plugin.use.PluginDependenciesSpec.springDependencyManagement: PluginDependencySpec
  get() = id(Plugins.springDependencyManagement)

inline val org.gradle.plugin.use.PluginDependenciesSpec.schemaGen: PluginDependencySpec
  get() = id(Plugins.schemaGen)

inline val org.gradle.plugin.use.PluginDependenciesSpec.kover: PluginDependencySpec
  get() = id(Plugins.kover)

inline val org.gradle.plugin.use.PluginDependenciesSpec.dependenciesReport: PluginDependencySpec
  get() = id(Plugins.dependenciesReport)

inline val org.gradle.plugin.use.PluginDependenciesSpec.javafx: PluginDependencySpec
  get() = id(Plugins.javafx)

inline val org.gradle.plugin.use.PluginDependenciesSpec.analyze: PluginDependencySpec
  get() = id(Plugins.analyze)

inline val org.gradle.plugin.use.PluginDependenciesSpec.copyResourcesFromDependencies: PluginDependencySpec
  get() = id(Plugins.copyResourcesFromDependencies)

inline val org.gradle.plugin.use.PluginDependenciesSpec.dockerPasswordFromSecretFile: PluginDependencySpec
  get() = id(Plugins.dockerPasswordFromSecretFile)

inline val org.gradle.plugin.use.PluginDependenciesSpec.secretFromPropertiesFile: PluginDependencySpec
  get() = id(Plugins.secretFromPropertiesFile)

inline val org.gradle.plugin.use.PluginDependenciesSpec.ksp: PluginDependencySpec
  get() = id(Plugins.ksp)

inline val org.gradle.plugin.use.PluginDependenciesSpec.kspBoxing: PluginDependencySpec
  get() = id(Plugins.kspBoxing)

@Deprecated("Does not seem to work")
inline val org.gradle.plugin.use.PluginDependenciesSpec.ssh: PluginDependencySpec
  get() = id(Plugins.ssh)
