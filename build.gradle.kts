import com.diffplug.gradle.spotless.SpotlessExtension
import dev.detekt.gradle.extensions.DetektExtension
import it.neckar.ci.Cron
import it.neckar.gradle.Plugins
import it.neckar.open.generated.GeneratedFileCommentSyntax
import it.neckar.open.generated.GeneratedFileMarker
import it.neckar.projects.Projects
import it.neckar.projects.common.AbsoluteUrl
import it.neckar.projects.common.GitBranchName
import it.neckar.projects.common.GitTagPrefix
import it.neckar.projects.common.GitlabComponentLabel
import it.neckar.projects.common.NpmPackageName
import it.neckar.projects.common.ProductId
import it.neckar.projects.common.ProductIndex
import it.neckar.projects.delivery.Decider
import it.neckar.projects.delivery.DeliveryConditions
import it.neckar.projects.delivery.RegistryCoordinate
import it.neckar.projects.delivery.Retention
import it.neckar.projects.delivery.Trigger
import it.neckar.projects.delivery.VersionScheme
import it.neckar.projects.project

description = "Meistercharts"

plugins {
  //dokka
  delivery
  moduleGroup
  projectDeclaration
}

productDeclaration {
  productId = ProductId("meistercharts")
  displayName = "MeisterCharts"
  description = "Charting library"
  technology = "Kotlin/JS"
  productIndex = ProductIndex(40)
}

idea {
  module {
    isDownloadSources = true
  }
}

allprojects {
  val meisterchartsProject = project(Projects.open.meistercharts)
  val currentProject = this

  //Configure detekt license check
  extensions.findByType(DetektExtension::class.java)?.apply {
    val detektConfigBase = meisterchartsProject.files("../../buildplatform/config/detekt/detekt.yml")
    val detektConfigMeistercharts = meisterchartsProject.files("config/detekt/detekt-meistercharts.yml")
    val detektConfigMeisterchartsDemos = meisterchartsProject.files("config/detekt/detekt-meistercharts-demos.yml")
    val detektConfigMeisterchartsFx = meisterchartsProject.files("config/detekt/detekt-meistercharts-fx.yml")

    val configFiles = when {
      currentProject.name.contains("demos") -> {
        files(
          detektConfigBase,
          detektConfigMeisterchartsDemos,
        )
      }

      currentProject.name.endsWith("-fx") -> {
        files(
          detektConfigBase,
          detektConfigMeisterchartsFx,
        )
      }

      else -> {
        files(
          detektConfigBase,
          detektConfigMeistercharts,
        )
      }
    }

    config.setFrom(configFiles)
  }

  /**
   * Used for license formatting - Spotless ensures headers are at correct position (file start)
   */
  apply(plugin = Plugins.Spotless)

  extensions.configure<SpotlessExtension> {
    kotlin {
      target("src/**/*.kt")
      licenseHeaderFile(meisterchartsProject.file("../config/license-headers/apache-2.0.template"))
    }
  }
}

tasks.register("generateFiddleExamplesUrl") {
  val fiddleBaseUrl = "https://jsfiddle.net/gh/get/library/pure/Neckar-IT/meistercharts/tree/main/meistercharts-examples/fiddles/"
  val scriptDir = file("meistercharts-examples/fiddles")
  val buildDir = file("meistercharts-examples/build")
  doLast {
    buildDir.mkdirs()
    val fiddleDemoAdoc = buildDir.resolve("fiddle-links.md")
    fiddleDemoAdoc.printWriter().use { out ->
      out.print(
        GeneratedFileMarker(
          by = "generateFiddleExamplesUrl",
          source = "meistercharts-examples/fiddles (one line per demo directory)",
          regenerateHint = "Add or remove a demo directory and re-run the task.",
        ).render(GeneratedFileCommentSyntax.XmlComment)
      )
      scriptDir.listFiles()?.filter { it.isDirectory && it.name != "build" }
        ?.map { it.name }?.forEach { demo ->
          out.println("${fiddleBaseUrl}${demo}")
        }
    }
  }
}

delivery {
  product = ProductId("meistercharts")
  tagPrefix = GitTagPrefix("meistercharts")
  versionScheme = VersionScheme.Semver
  versionProperty = "meistercharts.version"
  componentLabels = listOf(GitlabComponentLabel("Component::MeisterCharts"))

  registry("npm", RegistryCoordinate.NpmPackage(NpmPackageName("@meistercharts/meistercharts"))) {
    trigger = Trigger.Decision(by = Decider.Human)
    retention = Retention.Accumulates
    conditions(DeliveryConditions.PipelineIsGreen, DeliveryConditions.VersionExceedsPredecessor, DeliveryConditions.NotesComplete)
  }

  registry("distribution-archive", RegistryCoordinate.GitRemote(AbsoluteUrl("https://git.neckar.it/neckarit/meistercharts-releases.git"), GitBranchName("master"))) {
    trigger = Trigger.Decision(by = Decider.Human)
    retention = Retention.Accumulates
    conditions(DeliveryConditions.PipelineIsGreen, DeliveryConditions.VersionExceedsPredecessor, DeliveryConditions.NotesComplete)
  }

  registry("github-mirror-main", RegistryCoordinate.GitRemote(AbsoluteUrl("https://github.com/Neckar-IT/meistercharts"), GitBranchName("main"))) {
    trigger = Trigger.Decision(by = Decider.Human)
    retention = Retention.HoldsCurrentState
    conditions(DeliveryConditions.PipelineIsGreen, DeliveryConditions.VersionExceedsPredecessor, DeliveryConditions.NotesComplete)
  }

  registry("npm-snapshots", RegistryCoordinate.NpmPackage(NpmPackageName("@meistercharts/meistercharts"))) {
    trigger = Trigger.Schedule(Cron("0 4 * * *"))
    retention = Retention.HoldsCurrentState
    conditions(DeliveryConditions.PipelineIsGreen)
  }

  registry("github-mirror-develop", RegistryCoordinate.GitRemote(AbsoluteUrl("https://github.com/Neckar-IT/meistercharts"), GitBranchName("develop"))) {
    trigger = Trigger.Schedule(Cron("0 4 * * *"))
    retention = Retention.HoldsCurrentState
    conditions(DeliveryConditions.PipelineIsGreen)
  }
}
