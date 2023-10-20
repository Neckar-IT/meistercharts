import Plugins.kotlinJvm

plugins {
  kotlinJvm
}

dependencies {
  api(Libs.symbol_processing_api)
  api(project(Projects.meistercharts_commons))
}


