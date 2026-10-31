import it.neckar.gradle.Plugins
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec


inline val PluginDependenciesSpec.kotlinJvm: PluginDependencySpec
  get() = id(Plugins.KotlinJvm)

inline val PluginDependenciesSpec.java: PluginDependencySpec
  get() = id(Plugins.Java)

inline val PluginDependenciesSpec.javaLibrary: PluginDependencySpec
  get() = id(Plugins.JavaLibrary)

inline val PluginDependenciesSpec.dokka: PluginDependencySpec
  get() = id(Plugins.Dokka)

inline val PluginDependenciesSpec.kotlinMultiPlatform: PluginDependencySpec
  get() = id(Plugins.KotlinMultiPlatform)

inline val PluginDependenciesSpec.kotlinxSerialization: PluginDependencySpec
  get() = id(Plugins.KotlinxSerialization)

inline val PluginDependenciesSpec.systemDependencies: PluginDependencySpec
  get() = id(Plugins.SystemDependencies)

inline val PluginDependenciesSpec.jibCli: PluginDependencySpec
  get() = id(Plugins.JibCli)

inline val PluginDependenciesSpec.jibService: PluginDependencySpec
  get() = id(Plugins.JibService)

inline val PluginDependenciesSpec.otelAgent: PluginDependencySpec
  get() = id(Plugins.otelAgent)

inline val PluginDependenciesSpec.dockerHubPublish: PluginDependencySpec
  get() = id(Plugins.DockerHubPublish)

inline val PluginDependenciesSpec.shadow: PluginDependencySpec
  get() = id(Plugins.Shadow)

inline val PluginDependenciesSpec.launch4j: PluginDependencySpec
  get() = id(Plugins.Launch4j)

inline val PluginDependenciesSpec.download: PluginDependencySpec
  get() = id(Plugins.Download)

inline val PluginDependenciesSpec.mavenPublish: PluginDependencySpec
  get() = id(Plugins.MavenPublish)

inline val PluginDependenciesSpec.versions: PluginDependencySpec
  get() = id(Plugins.Versions)

inline val PluginDependenciesSpec.licenseReport: PluginDependencySpec
  get() = id(Plugins.LicenseReport)

inline val PluginDependenciesSpec.spotless: PluginDependencySpec
  get() = id(Plugins.Spotless)

inline val PluginDependenciesSpec.generateIcons: PluginDependencySpec
  get() = id(Plugins.GenerateIcons)

inline val PluginDependenciesSpec.provideSourceCodeFromBuildSrc: PluginDependencySpec
  get() = id(Plugins.ProvideSourceCodeFromBuildSrc)

inline val PluginDependenciesSpec.generateTsDeclaration: PluginDependencySpec
  get() = id(Plugins.GenerateTsDeclaration)

inline val PluginDependenciesSpec.gitlabPipelines: PluginDependencySpec
  get() = id(Plugins.GitlabPipelines)

inline val PluginDependenciesSpec.neckarItAsciidoctor: PluginDependencySpec
  get() = id(Plugins.NeckarItAsciidoctor)

inline val PluginDependenciesSpec.asciidoctor: PluginDependencySpec
  get() = id(Plugins.Asciidoctor)

inline val PluginDependenciesSpec.asciidoctorPdf: PluginDependencySpec
  get() = id(Plugins.AsciidoctorPdf)

inline val PluginDependenciesSpec.asciidoctorDiagram: PluginDependencySpec
  get() = id(Plugins.AsciidoctorDiagram)

inline val PluginDependenciesSpec.generateAuditReport: PluginDependencySpec
  get() = id(Plugins.GenerateAuditReport)

inline val PluginDependenciesSpec.buildProfileReport: PluginDependencySpec
  get() = id(Plugins.BuildProfileReport)

inline val PluginDependenciesSpec.npmBundle: PluginDependencySpec
  get() = id(Plugins.npmBundle)

inline val PluginDependenciesSpec.verifyMainClassExists: PluginDependencySpec
  get() = id(Plugins.VerifyMainClassExists)

inline val PluginDependenciesSpec.verifyGitlabAccessToken: PluginDependencySpec
  get() = id(Plugins.VerifyGitlabAccessToken)

inline val PluginDependenciesSpec.additionalGitRepository: PluginDependencySpec
  get() = id(Plugins.AdditionalGitRepository)

inline val PluginDependenciesSpec.runDockerServices: PluginDependencySpec
  get() = id(Plugins.RunDockerServices)

inline val PluginDependenciesSpec.ngrokTunnel: PluginDependencySpec
  get() = id(Plugins.NgrokTunnel)

/**
 * Use task tree like this:
 *
 * `gradle <task 1>...<task N> taskTree`
 *
 * see https://github.com/dorongold/gradle-task-tree for documentation
 */
inline val PluginDependenciesSpec.taskTree: PluginDependencySpec
  get() = id(Plugins.TaskTree)

inline val PluginDependenciesSpec.detekt: PluginDependencySpec
  get() = id(Plugins.Detekt)

inline val PluginDependenciesSpec.jmh: PluginDependencySpec
  get() = id(Plugins.jmh)

inline val PluginDependenciesSpec.pdfOverview: PluginDependencySpec
  get() = id(Plugins.pdfOverview)

inline val PluginDependenciesSpec.html2pdf: PluginDependencySpec
  get() = id(Plugins.html2pdf)

inline val PluginDependenciesSpec.consoleReporter: PluginDependencySpec
  get() = id(Plugins.ConsoleReporter)

inline val PluginDependenciesSpec.node: PluginDependencySpec
  get() = id(Plugins.Node)

inline val PluginDependenciesSpec.kvision: PluginDependencySpec
  get() = id(Plugins.Kvision)

inline val PluginDependenciesSpec.jib: PluginDependencySpec
  get() = id(Plugins.Jib)

inline val PluginDependenciesSpec.intellijPlatform: PluginDependencySpec
  get() = id(Plugins.IntellijPlatform)

inline val PluginDependenciesSpec.ideaExt: PluginDependencySpec
  get() = id(Plugins.IdeaExt)

inline val PluginDependenciesSpec.python: PluginDependencySpec
  get() = id(Plugins.Python)

inline val PluginDependenciesSpec.schemaGen: PluginDependencySpec
  get() = id(Plugins.SchemaGen)

inline val PluginDependenciesSpec.kover: PluginDependencySpec
  get() = id(Plugins.Kover)

inline val PluginDependenciesSpec.javafx: PluginDependencySpec
  get() = id(Plugins.Javafx)

inline val PluginDependenciesSpec.analyze: PluginDependencySpec
  get() = id(Plugins.Analyze)

inline val PluginDependenciesSpec.webResourcesFromDependencies: PluginDependencySpec
  get() = id(Plugins.WebResourcesFromDependencies)

inline val PluginDependenciesSpec.resourcesConvention: PluginDependencySpec
  get() = id(Plugins.ResourcesConvention)

inline val PluginDependenciesSpec.secretStore: PluginDependencySpec
  get() = id(Plugins.SecretStore)

inline val PluginDependenciesSpec.secretsLoader: PluginDependencySpec
  get() = id(Plugins.SecretsLoader)

inline val PluginDependenciesSpec.mattermostBot: PluginDependencySpec
  get() = id(Plugins.MattermostBot)

inline val PluginDependenciesSpec.mattermostBots: PluginDependencySpec
  get() = id(Plugins.MattermostBots)

inline val PluginDependenciesSpec.hostDeployment: PluginDependencySpec
  get() = id(Plugins.HostDeployment)

inline val PluginDependenciesSpec.serviceDeployment: PluginDependencySpec
  get() = id(Plugins.ServiceDeployment)

inline val PluginDependenciesSpec.deploymentBuildRules: PluginDependencySpec
  get() = id(Plugins.DeploymentBuildRules)

inline val PluginDependenciesSpec.backup: PluginDependencySpec
  get() = id(Plugins.Backup)

inline val PluginDependenciesSpec.backupSchedule: PluginDependencySpec
  get() = id(Plugins.BackupSchedule)

inline val PluginDependenciesSpec.delivery: PluginDependencySpec
  get() = id(Plugins.Delivery)

inline val PluginDependenciesSpec.buildVariables: PluginDependencySpec
  get() = id(Plugins.BuildVariables)

inline val PluginDependenciesSpec.productsCatalog: PluginDependencySpec
  get() = id(Plugins.ProductsCatalog)

inline val PluginDependenciesSpec.deliveryConditions: PluginDependencySpec
  get() = id(Plugins.DeliveryConditions)

inline val PluginDependenciesSpec.hostDeclaration: PluginDependencySpec
  get() = id(Plugins.HostDeclaration)

inline val PluginDependenciesSpec.localhostInfrastructure: PluginDependencySpec
  get() = id(Plugins.LocalhostInfrastructure)

inline val PluginDependenciesSpec.moduleGroup: PluginDependencySpec
  get() = id(Plugins.ModuleGroup)

inline val PluginDependenciesSpec.customDeployment: PluginDependencySpec
  get() = id(Plugins.CustomDeployment)

inline val PluginDependenciesSpec.ksp: PluginDependencySpec
  get() = id(Plugins.ksp)

inline val PluginDependenciesSpec.kspBoxing: PluginDependencySpec
  get() = id(Plugins.kspBoxing)

inline val PluginDependenciesSpec.specHarvest: PluginDependencySpec
  get() = id(Plugins.SpecHarvest)

inline val PluginDependenciesSpec.kspSerialization: PluginDependencySpec
  get() = id(Plugins.kspSerialization)

inline val PluginDependenciesSpec.openapiValidator: PluginDependencySpec
  get() = id(Plugins.OpenapiValidator)

inline val org.gradle.plugin.use.PluginDependenciesSpec.openapiGenerationConfig: PluginDependencySpec
  get() = id(Plugins.OpenapiGenerationConfig)

inline val PluginDependenciesSpec.orvalConvert: PluginDependencySpec
  get() = id(Plugins.OrvalConvert)

inline val PluginDependenciesSpec.verifyPnpmWorkspaceYaml: PluginDependencySpec
  get() = id(Plugins.VerifyPnpmWorkspaceYaml)

inline val PluginDependenciesSpec.verifyPnpmWorkspaceDependencies: PluginDependencySpec
  get() = id(Plugins.VerifyPnpmWorkspaceDependencies)

inline val PluginDependenciesSpec.verifyPnpmLockfilePeerVariants: PluginDependencySpec
  get() = id(Plugins.VerifyPnpmLockfilePeerVariants)

inline val PluginDependenciesSpec.disableDistTasks: PluginDependencySpec
  get() = id(Plugins.DisableDistTasks)

inline val PluginDependenciesSpec.executableApplication: PluginDependencySpec
  get() = id(Plugins.ExecutableApplication)

inline val PluginDependenciesSpec.ktorServiceApplication: PluginDependencySpec
  get() = id(Plugins.KtorServiceApplication)

inline val PluginDependenciesSpec.generateTypesList: PluginDependencySpec
  get() = id(Plugins.GenerateTypesList)

inline val PluginDependenciesSpec.typesListCollector: PluginDependencySpec
  get() = id(Plugins.TypesListCollector)

inline val PluginDependenciesSpec.pnpmKotlinInterop: PluginDependencySpec
  get() = id(Plugins.pnpmKotlinInterop)

inline val PluginDependenciesSpec.tailwind: PluginDependencySpec
  get() = id(Plugins.Tailwind)

inline val PluginDependenciesSpec.keycloakClient: PluginDependencySpec
  get() = id(Plugins.KeycloakClient)

inline val PluginDependenciesSpec.keycloakClients: PluginDependencySpec
  get() = id(Plugins.KeycloakClients)

inline val PluginDependenciesSpec.openTofu: PluginDependencySpec
  get() = id(Plugins.OpenTofu)

inline val PluginDependenciesSpec.linksSiteGenerate: PluginDependencySpec
  get() = id(Plugins.LinksSiteGenerate)

inline val PluginDependenciesSpec.certificates: PluginDependencySpec
  get() = id(Plugins.Certificates)

inline val PluginDependenciesSpec.localDev: PluginDependencySpec
  get() = id(Plugins.LocalDev)

inline val PluginDependenciesSpec.projectDeclaration: PluginDependencySpec
  get() = id(Plugins.ProjectDeclaration)

inline val PluginDependenciesSpec.specGenerator: PluginDependencySpec
  get() = id(Plugins.SpecGenerator)

inline val PluginDependenciesSpec.dependencyFence: PluginDependencySpec
  get() = id(Plugins.DependencyFence)

inline val PluginDependenciesSpec.openModule: PluginDependencySpec
  get() = id(Plugins.OpenModule)

