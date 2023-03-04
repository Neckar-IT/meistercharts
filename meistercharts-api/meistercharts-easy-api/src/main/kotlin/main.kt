import com.meistercharts.design.SegoeUiDesign
import com.meistercharts.js.MeisterChartsPlatform
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer

fun main() {
  //Load logger configuration from local storage
  LogConfigurer.initializeFromLocalStorage(Level.WARN)

  MeisterChartsPlatform.init(corporateDesign = SegoeUiDesign)
}

/**
 * If set to true, the styles will be printed to the console
 */
@Deprecated("Use Logggers instead")
const val StyleDebugEnabled: Boolean = false
