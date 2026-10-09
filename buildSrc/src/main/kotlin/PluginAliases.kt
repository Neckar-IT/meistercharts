import it.neckar.gradle.Plugins
import it.neckar.gradle.RegistryPlugins
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec


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

inline val PluginDependenciesSpec.systemDependencies: PluginDependencySpec
  get() = id(Plugins.systemDependencies)

inline val PluginDependenciesSpec.jibCli: PluginDependencySpec
  get() = id(Plugins.jibCli)

inline val PluginDependenciesSpec.jibService: PluginDependencySpec
  get() = id(Plugins.jibService)

inline val PluginDependenciesSpec.otelAgent: PluginDependencySpec
  get() = id(Plugins.otelAgent)

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

inline val PluginDependenciesSpec.gitlabPipelines: PluginDependencySpec
  get() = id(Plugins.gitlabPipelines)

inline val PluginDependenciesSpec.neckarItAsciidoctor: PluginDependencySpec
  get() = id(Plugins.neckarItAsciidoctor)

inline val PluginDependenciesSpec.asciidoctor: PluginDependencySpec
  get() = id(Plugins.asciidoctor)

inline val PluginDependenciesSpec.asciidoctorPdf: PluginDependencySpec
  get() = id(Plugins.asciidoctorPdf)

inline val PluginDependenciesSpec.asciidoctorGems: PluginDependencySpec
  get() = id(Plugins.asciidoctorGems)

inline val PluginDependenciesSpec.generateAuditReport: PluginDependencySpec
  get() = id(Plugins.generateAuditReport)

inline val PluginDependenciesSpec.buildProfileReport: PluginDependencySpec
  get() = id(Plugins.buildProfileReport)

inline val PluginDependenciesSpec.npmBundle: PluginDependencySpec
  get() = id(Plugins.npmBundle)

inline val PluginDependenciesSpec.verifyMainClassExists: PluginDependencySpec
  get() = id(Plugins.verifyMainClassExists)

inline val PluginDependenciesSpec.verifyGitlabAccessToken: PluginDependencySpec
  get() = id(Plugins.verifyGitlabAccessToken)

inline val PluginDependenciesSpec.additionalGitRepository: PluginDependencySpec
  get() = id(Plugins.additionalGitRepository)

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

inline val PluginDependenciesSpec.html2pdf: PluginDependencySpec
  get() = id(Plugins.html2pdf)

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

inline val PluginDependenciesSpec.ideaExt: PluginDependencySpec
  get() = id(Plugins.ideaExt)

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

inline val PluginDependenciesSpec.webResourcesFromDependencies: PluginDependencySpec
  get() = id(Plugins.webResourcesFromDependencies)

inline val PluginDependenciesSpec.resourcesConvention: PluginDependencySpec
  get() = id(Plugins.resourcesConvention)

inline val PluginDependenciesSpec.secretsLoader: PluginDependencySpec
  get() = id(Plugins.secretsLoader)

inline val PluginDependenciesSpec.deployment: PluginDependencySpec
  get() = id(Plugins.deployment)

inline val PluginDependenciesSpec.deploymentBuildRules: PluginDependencySpec
  get() = id(Plugins.deploymentBuildRules)

inline val PluginDependenciesSpec.backup: PluginDependencySpec
  get() = id(Plugins.backup)

inline val PluginDependenciesSpec.archiveEncryptionHost: PluginDependencySpec
  get() = id(Plugins.archiveEncryptionHost)

inline val PluginDependenciesSpec.backupSchedule: PluginDependencySpec
  get() = id(Plugins.backupSchedule)

inline val PluginDependenciesSpec.delivery: PluginDependencySpec
  get() = id(Plugins.delivery)

inline val PluginDependenciesSpec.buildVariables: PluginDependencySpec
  get() = id(Plugins.buildVariables)

inline val PluginDependenciesSpec.productsCatalog: PluginDependencySpec
  get() = id(Plugins.productsCatalog)

inline val PluginDependenciesSpec.deliveryConditions: PluginDependencySpec
  get() = id(Plugins.deliveryConditions)

inline val PluginDependenciesSpec.hostDeclaration: PluginDependencySpec
  get() = id(Plugins.hostDeclaration)

inline val PluginDependenciesSpec.localhostInfrastructure: PluginDependencySpec
  get() = id(Plugins.localhostInfrastructure)

inline val PluginDependenciesSpec.moduleGroup: PluginDependencySpec
  get() = id(Plugins.moduleGroup)

inline val PluginDependenciesSpec.customDeployment: PluginDependencySpec
  get() = id(Plugins.customDeployment)

inline val PluginDependenciesSpec.provisioning: PluginDependencySpec
  get() = id(Plugins.provisioning)

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

inline val PluginDependenciesSpec.orvalConvert: PluginDependencySpec
  get() = id(Plugins.orvalConvert)

inline val PluginDependenciesSpec.verifyPnpmWorkspaceYaml: PluginDependencySpec
  get() = id(Plugins.verifyPnpmWorkspaceYaml)

inline val PluginDependenciesSpec.verifyPnpmWorkspaceDependencies: PluginDependencySpec
  get() = id(Plugins.verifyPnpmWorkspaceDependencies)

inline val PluginDependenciesSpec.verifyPnpmLockfilePeerVariants: PluginDependencySpec
  get() = id(Plugins.verifyPnpmLockfilePeerVariants)

inline val PluginDependenciesSpec.disableDistTasks: PluginDependencySpec
  get() = id(Plugins.disableDistTasks)

inline val PluginDependenciesSpec.executableApplication: PluginDependencySpec
  get() = id(Plugins.executableApplication)

inline val PluginDependenciesSpec.ktorServiceApplication: PluginDependencySpec
  get() = id(Plugins.ktorServiceApplication)

inline val PluginDependenciesSpec.generateIgnoreProjectSets: PluginDependencySpec
  get() = id(RegistryPlugins.generateIgnoreProjectSets)

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

inline val PluginDependenciesSpec.projectDeclaration: PluginDependencySpec
  get() = id(Plugins.projectDeclaration)

inline val PluginDependenciesSpec.specGenerator: PluginDependencySpec
  get() = id(Plugins.specGenerator)

inline val PluginDependenciesSpec.dependencyFence: PluginDependencySpec
  get() = id(Plugins.dependencyFence)

inline val PluginDependenciesSpec.openModule: PluginDependencySpec
  get() = id(Plugins.openModule)

