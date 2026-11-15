pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  // Same plugin and version as the build-logic/settings.gradle.kts of the monorepo.
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// An included build reads no gradle.properties of the build including it. The export writes the build platform release into the
// gradle.properties of its root: internal/open/meistercharts for the copy of MeisterCharts, internal/open for the copy of internal/open,
// which reaches this build through internal/open/project-registry. Inside the monorepo neither file names a version.
val buildplatformVersion: String? = listOf("../gradle.properties", "../../gradle.properties")
  .firstNotNullOfOrNull { candidate ->
    providers.fileContents(layout.rootDirectory.file(candidate)).asText.orNull
      ?.lineSequence()
      ?.map { it.trim() }
      ?.firstOrNull { it.startsWith("buildplatformVersion=") }
      ?.substringAfter('=')
  }

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    // The plugins the convention plugins of the build platform depend on.
    gradlePluginPortal()
  }
  versionCatalogs {
    create("libs") {
      if (buildplatformVersion == null) {
        from(files("../../../buildplatform/gradle/libs.versions.toml"))
      } else {
        from("it.neckar.buildplatform:version-catalog:$buildplatformVersion")
      }
    }
  }
}

rootProject.name = "meistercharts-project-registry"

if (buildplatformVersion == null) {
  // The registry types that `meistercharts-projects` instantiates, from the sources of the monorepo.
  includeBuild("../../../buildplatform")
} else {
  // The build scripts name the artifacts of the build platform without a version, as inside the monorepo. A constraint on
  // `api` gives them the version of the release the export names, also in the build that puts `registry` on its classpath.
  gradle.beforeProject {
    plugins.withId("java-library") {
      listOf("conventions", "projects").forEach { artifact ->
        dependencies.constraints.add("api", "it.neckar.buildplatform:$artifact:$buildplatformVersion")
      }
    }
  }
}

include("meistercharts-projects")
include("registry")
