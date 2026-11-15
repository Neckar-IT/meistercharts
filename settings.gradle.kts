// Read by the export of internal/open/meistercharts alone. verifyStandaloneIncludes keeps the includes equal to the MeisterCharts
// modules of the root settings.gradle.kts, minus those the root build.gradle.kts leaves out.

import org.gradle.util.GradleVersion

rootProject.name = "meistercharts"
// The monorepo evaluates build.gradle.kts as the subproject :internal:open:meistercharts, with the delivery and product declarations.
rootProject.buildFileName = "standalone.gradle.kts"

val minimumGradleVersion: String = providers.gradleProperty("minimumGradleVersion").get()
val buildplatformVersion: String = providers.gradleProperty("buildplatformVersion").get()

// The export carries the wrapper of the monorepo; without the check an older Gradle fails inside the Kotlin plugin,
// where the message names anything but the cause.
require(GradleVersion.current() >= GradleVersion.version(minimumGradleVersion)) {
  "meistercharts needs Gradle $minimumGradleVersion or newer, this is ${GradleVersion.current().version}."
}

pluginManagement {
  val foojayResolverVersion: String = providers.gradleProperty("foojayResolverVersion").get()

  repositories {
    gradlePluginPortal()
    mavenCentral()
  }

  // Not from the catalog: pluginManagement is evaluated before any catalog exists.
  plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolverVersion
  }

  // it.neckar.open.meistercharts.settings and the registry `Projects`, which names `Projects.meistercharts.meisterchartsCore` as `:meistercharts-core`.
  includeBuild("project-registry")
}

plugins {
  // Resolves the JDK 25 toolchain the same way the monorepo does.
  id("org.gradle.toolchains.foojay-resolver-convention")
  // Puts the convention plugins of the build platform at buildplatformVersion on the classpath of every build script, and sets group,
  // version and it.neckar.base for every project.
  id("it.neckar.open.meistercharts.settings")
}

dependencyResolutionManagement {
  // The version catalog alone resolves here: every project holds the repositories of it.neckar.base.
  repositories {
    mavenCentral()
  }

  versionCatalogs {
    // The repository's only catalog, published by the build platform: same aliases as inside the checkout.
    create("libs") {
      from("it.neckar.buildplatform:version-catalog:$buildplatformVersion")
    }
  }
}

include(":meistercharts-api:meistercharts-easy-api")
include(":meistercharts-canvas")
include(":meistercharts-core")
include(":meistercharts-history:meistercharts-history-api")
include(":meistercharts-history:meistercharts-history-core")
include(":meistercharts-version-info")
