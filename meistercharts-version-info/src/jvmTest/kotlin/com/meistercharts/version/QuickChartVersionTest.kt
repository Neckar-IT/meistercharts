package com.meistercharts.version

import assertk.*
import assertk.assertions.*
import com.meistercharts.version.MeisterChartsVersion
import org.junit.jupiter.api.Test

/**
 */
class meisterchartsVersionTest {
  @Test
  fun testIt() {
    assertThat(MeisterChartsVersion.version).startsWith("1.")
    assertThat(MeisterChartsVersion.monorepoVersion).startsWith("9.")
    assertThat(MeisterChartsVersion.gitCommit).isNotNull()
    assertThat(MeisterChartsVersion.buildDateDay).isNotNull()
    assertThat(MeisterChartsVersion.versionAsStringVerbose).contains(MeisterChartsVersion.version)
  }
}
