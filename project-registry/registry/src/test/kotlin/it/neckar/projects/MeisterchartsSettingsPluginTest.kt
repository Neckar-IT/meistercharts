package it.neckar.projects

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.single
import it.neckar.gradle.mavenGroupOf
import it.neckar.projects.MeisterchartsSettingsPlugin.Companion.useMeisterchartsCoordinates
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MeisterchartsSettingsPluginTest {
  @TempDir
  lateinit var rootDir: File

  @Test
  fun `every project of the export has the group the monorepo gives it`() {
    val root = rootProject()
    val history = ProjectBuilder.builder().withName("meistercharts-history").withParent(root).build()
    val historyCore = ProjectBuilder.builder().withName("meistercharts-history-core").withParent(history).build()

    historyCore.useMeisterchartsCoordinates()

    assertThat(historyCore.group).isEqualTo(mavenGroupOf(listOf(), ":internal:open:meistercharts:meistercharts-history:meistercharts-history-core"))
  }

  @Test
  fun `the registry of the export names each project below its root`() {
    assertThat(Projects.meistercharts.meisterchartsHistory.meisterchartsHistoryCore.path)
      .isEqualTo(GradleProjectPath(":meistercharts-history:meistercharts-history-core"))
  }

  @Test
  fun `a MeisterCharts coordinate resolves to the project of this build`() {
    val consumer = consumerOf(rootProject())

    consumer.dependencies.add("implementation", "it.neckar.open.meistercharts:meistercharts-core")

    assertThat(compileClasspathComponents(consumer)).contains("project ':meistercharts-core'")
  }

  @Test
  fun `an open coordinate resolves from the repositories`() {
    val consumer = consumerOf(rootProject())

    consumer.dependencies.add("implementation", "it.neckar.open:kotlin-lang:10.2.0")

    assertThat(resolutionFailures(consumer)).single().contains("no repositories are defined")
  }

  private fun rootProject(): Project = ProjectBuilder.builder().withProjectDir(rootDir).build()

  /** `:meistercharts-canvas` next to its sibling `:meistercharts-core`, both configured as the settings plugin configures them. */
  private fun consumerOf(root: Project): Project {
    ProjectBuilder.builder().withName("meistercharts-core").withParent(root).build().also {
      it.plugins.apply("java-library")
      it.useMeisterchartsCoordinates()
    }
    return ProjectBuilder.builder().withName("meistercharts-canvas").withParent(root).build().also {
      it.plugins.apply("java-library")
      it.useMeisterchartsCoordinates()
    }
  }

  private fun compileClasspathComponents(consumer: Project): List<String> {
    return consumer.configurations.getByName("compileClasspath")
      .incoming.resolutionResult.allComponents
      .map(ResolvedComponentResult::getId)
      .map { it.displayName }
  }

  /** Why the resolution failed for each dependency: this test declares no repository, so a coordinate left to the repositories fails. */
  private fun resolutionFailures(consumer: Project): List<String> {
    return consumer.configurations.getByName("compileClasspath")
      .incoming.resolutionResult.allDependencies
      .filterIsInstance<UnresolvedDependencyResult>()
      .map { result -> generateSequence(result.failure) { it.cause }.mapNotNull { it.message }.joinToString(" / ") }
  }
}
