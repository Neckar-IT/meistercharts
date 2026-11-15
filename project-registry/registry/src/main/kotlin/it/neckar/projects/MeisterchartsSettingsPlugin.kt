package it.neckar.projects

import it.neckar.gradle.NeckarBasePlugin
import it.neckar.gradle.NeckarOpenVersionProperty
import it.neckar.gradle.ProjectRegistration
import it.neckar.gradle.configureNodeJsRootExtension
import it.neckar.gradle.configureRegisteredKotlinModules
import it.neckar.gradle.limitParallelNodeTasksAndKotlinJsLinks
import it.neckar.gradle.mavenGroupOf
import it.neckar.gradle.substituteNeckarCoordinatesWithSourceProjects
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * Applied in `internal/open/meistercharts/settings.gradle.kts`, which the export of `internal/open/meistercharts` alone reads. Sets for
 * every project what the monorepo sets for the projects under `internal/open/meistercharts`:
 * - [NeckarBasePlugin], the group `it.neckar.open.meistercharts` and the version `neckarOpenVersion` of `gradle.properties`
 * - the sources of this build for the coordinates of the group [MeisterchartsGroup]; every other coordinate, `it.neckar.open` included,
 *   resolves from the repositories
 * - the Node.js of the Kotlin/JS targets and the limits of the node tasks and Kotlin/JS links
 * - the conventions of each Kotlin module the settings include
 */
class MeisterchartsSettingsPlugin : Plugin<Settings> {
  override fun apply(settings: Settings) {
    val neckarOpenVersion: String = settings.providers.gradleProperty(NeckarOpenVersionProperty).get()
    settings.gradle.allprojects {
      pluginManager.apply(NeckarBasePlugin::class.java)
      version = neckarOpenVersion
      useMeisterchartsCoordinates()
    }

    settings.gradle.rootProject {
      configureNodeJsRootExtension()
      limitParallelNodeTasksAndKotlinJsLinks()

      // After the root project: the conventions read its version catalog, which a project has once its evaluation starts. Gradle evaluates
      // the root project before its subprojects, so their build scripts still see the Kotlin extensions.
      afterEvaluate {
        // Read once the include lines of the settings ran: the registry also lists the modules the export leaves out, such as meistercharts-fx.
        val includedKotlinModules: List<ConfiguredProject> = Projects.meistercharts.selfAndDescendants
          .filter { it.type in KotlinModuleTypes }
          .filter { settings.findProject(it.path.path) != null }

        configureRegisteredKotlinModules(
          baseProject = this,
          projects = includedKotlinModules,
          kspProcessorPathPrefix = NoKspProcessorPath,
          allowedKspDependencyPaths = emptySet(),
          registrationOf = { configuredProject -> ProjectRegistration(file = MeisterchartsProjects.RegistryFile, accessor = projectsAccessor(GradleProjectPath(":meistercharts").resolve(configuredProject.path))) },
        )
      }
    }
  }

  companion object {
    /** The directory segments of `internal/open/meistercharts` that [mavenGroupOf] keeps. */
    private val GroupRootSegments: List<String> = listOf("open", "meistercharts")

    /** `it.neckar.open.meistercharts`, the group of every project of this build. */
    private val MeisterchartsGroup: String = mavenGroupOf(GroupRootSegments, GradleProjectPath.Root.path)

    /** The build holds no KSP processor: the processors resolve from Maven Central as `it.neckar.open` coordinates. */
    private const val NoKspProcessorPath: String = ":ksp"

    private val KotlinModuleTypes: Set<ProjectType> = setOf(ProjectType.KotlinMultiplatform, ProjectType.KotlinJvm)

    /** The group of the monorepo, and the sources of this build for the coordinates of [MeisterchartsGroup] alone. */
    internal fun Project.useMeisterchartsCoordinates() {
      group = mavenGroupOf(GroupRootSegments, path)
      substituteNeckarCoordinatesWithSourceProjects { requestedGroup -> requestedGroup == MeisterchartsGroup }
    }
  }
}
