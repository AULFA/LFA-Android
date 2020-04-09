package one.lfa.android.tests

import one.lfa.android.analytics.LFAAnalyticsAuthentication
import one.lfa.android.analytics.LFAAnalyticsConfiguration
import org.junit.Assert
import org.junit.Test
import java.io.FileNotFoundException
import java.io.InputStream

class LFAAnalyticsConfigurationTest {

  @Test
  fun testParsing()
  {
    val configuration =
      LFAAnalyticsConfiguration.parseFromStream(
        resourceStream(name = "analyticsConfiguration.xml"),
        "5ddd34e4-e317-4d9c-a8e1-c79da59c4780"
      )

    Assert.assertEquals(
      "5ddd34e4-e317-4d9c-a8e1-c79da59c4780",
      configuration.deviceID)
    Assert.assertEquals(2, configuration.servers.size)
    Assert.assertEquals(20485760, configuration.logFileSizeLimit)

    run {
      val server = configuration.servers[0]
      Assert.assertEquals("http://www.example.com/0", server.address.toString())
      Assert.assertEquals(LFAAnalyticsAuthentication.None, server.authentication)
    }

    run {
      val server = configuration.servers[1]
      Assert.assertEquals("http://www.example.com/1", server.address.toString())
      Assert.assertEquals(LFAAnalyticsAuthentication.TokenBased("88cb923e-d318-4466-893e-ffa82a1eedee"), server.authentication)
    }
  }

  private fun resourceStream(
    name: String
  ): InputStream {
    val path =
      "/one/lfa/android/analytics/${name}"
    return LFAAnalyticsContract::class.java.getResourceAsStream(path)
      ?: throw FileNotFoundException(path)
  }
}