import it.neckar.gradle.npmbundle.declaresNpmBundle
import it.neckar.projects.Projects

description = """Meistercharts - pnpm project using the bundle of the current source"""

tasks.named("pnpmRunBuild") {
  declaresNpmBundle(Projects.open.meistercharts.meisterchartsApi.meisterchartsEasyApi.path)
}
