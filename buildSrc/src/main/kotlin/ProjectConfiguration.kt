import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Simplified project configuration for the standalone meistercharts.com-sync build.
 *
 * The monorepo version of this file has grown to reference many monorepo-specific
 * features (Python, Docker, Certificates, KSP processors). This local version
 * provides only the configuration needed for MeisterCharts multiplatform modules.
 */
object ProjectConfiguration {

  /**
   * Stubs for configuration methods referenced by AbstractProjects.kt.
   * These project types are not used in the meistercharts standalone build.
   */
  fun configureMultiPlatformJvmOnly(project: Project, jvmType: JvmType) {
    configureMultiPlatform(project, jvmType)
  }

  fun configureKspProcessor(project: Project) {}
  fun configurePnpm(project: Project) {}
  fun configurePython(project: Project) {}
  fun configureJvm(project: Project) {}
  fun configureParentProject(project: Project) {}

  fun configureMultiPlatform(project: Project, jvmType: JvmType) {
    with(project) {
      apply(plugin = Plugins.kotlinMultiPlatform)
      apply(plugin = Plugins.detekt)
      apply(plugin = Plugins.kover)

      configureKotlin()
      configureJunit()
      configureToolchain(jvmType)

      requireNotNull(extensions.getByType(KotlinMultiplatformExtension::class.java))

      configureDetekt {
        source.setFrom(
          files(
            "src/commonMain/kotlin",
            "src/jsMain/kotlin",
            "src/jvmMain/kotlin",
          )
        )
      }

      configureKover {}
    }
  }
}

fun Project.configureDetekt(additionalConfig: DetektExtension.() -> Unit) {
  extensions.getByType(DetektExtension::class.java).apply {
    config.from(rootProject.files("config/detekt/detekt.yml"))
    parallel = true
    buildUponDefaultConfig = true
    additionalConfig()
  }

  plugins.withType(io.gitlab.arturbosch.detekt.DetektPlugin::class) {
    tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class) {
      reports {
        html.required.set(true)
      }
    }
  }

  tasks.named("check") {
    this.setDependsOn(this.dependsOn.filterNot {
      it is TaskProvider<*> && it.name.contains("detekt")
    })
  }
}

fun Project.configureKover(additionalConfig: KoverProjectExtension.() -> Unit) {
  val koverProjectExtension = extensions.getByType<KoverProjectExtension>()
  koverProjectExtension.additionalConfig()
}
