import com.meistercharts.version.MeisterChartsVersion
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.LoggerFactory

fun main() {
  LogConfigurer.initializeFromLocalStorage(Level.INFO)
  logger.info("Initialized Log levels from local storage. Active root Level: ${LogConfigurer.rootLevel}")
  logger.info("Starting DemosJS - ${MeisterChartsVersion.versionAsStringVerbose}")
}

private val logger = LoggerFactory.getLogger("demosjs")

