import shadow.org.codehaus.plexus.util.Os

description = """Meistercharts Examples"""

//task("build") {
//  group = "Build"
//  description = "Builds the example project"
//
//  dependsOn.add("updateJavaScript")
//}
//defaultTasks.add("build")

task("updateJavaScript") {
  group = "Build"
  description = "Update MeisterChart related JavaScript files"

  //Ensure MeisterChart is built
  //TODO!
  //dependsOn(":internal:closed:charting:demosjs:build")
}

/**
 * Upgrades the dependencies of all meistercharts
 * demo projects
 */
task("meisterchartsDemosUpgrade") {
  doLast {
    exec {
      commandLine("./updateDependencies.sh")
    }
  }
}

if (Os.isFamily(Os.FAMILY_UNIX)) {
  task<Exec>("publishExamples") {
    group = "Publishing"
    description = "Upload example files to www.neckar.it"

    //Ensure MeisterChart is built
    dependsOn(":internal:closed:charting:demosjs:build")

    commandLine = listOf("./publish.sh")
  }
}

