import it.neckar.gradle.execStreamed

import org.apache.commons.lang3.SystemUtils
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations

description = """Meistercharts Examples"""

//task("build") {
//  group = "Build"
//  description = "Builds the example project"
//
//  dependsOn.add("updateJavaScript")
//}
//defaultTasks.add("build")

tasks.register("updateJavaScript") {
  group = "Build"
  description = "Update MeisterChart related JavaScript files"
}

/**
 * Upgrades the dependencies of all meistercharts
 * demo projects
 */
tasks.register("meisterchartsDemosUpgrade") {
  val examplesDirectory = projectDir
  val execOperations = serviceOf<ExecOperations>()
  doLast {
    execOperations.execStreamed {
      workingDir = examplesDirectory
      commandLine("./updateDependencies.sh")
    }
  }
}

if (SystemUtils.IS_OS_LINUX) {
  tasks.register<Exec>("publishExamples") {
    group = "Publishing"
    description = "Upload example files to the scp destination in MEISTERCHARTS_EXAMPLES_TARGET"

    commandLine = listOf("./publish.sh")
  }
}

