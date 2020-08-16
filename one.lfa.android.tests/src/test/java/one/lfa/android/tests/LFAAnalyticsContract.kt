package one.lfa.android.tests

import android.content.Context
import com.io7m.jfunctional.Option
import one.lfa.android.analytics.LFAAnalyticsAuthentication
import one.lfa.android.analytics.LFAAnalyticsConfiguration
import one.lfa.android.analytics.LFAAnalyticsServerConfiguration
import one.lfa.android.analytics.LFAAnalyticsSystem
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mockito
import org.nypl.simplified.analytics.api.AnalyticsConfiguration
import org.nypl.simplified.analytics.api.AnalyticsEvent
import org.nypl.simplified.http.core.HTTPResultError
import org.nypl.simplified.http.core.HTTPResultOK
import org.nypl.simplified.opds.core.OPDSAcquisitionFeedEntry
import org.nypl.simplified.opds.core.OPDSAvailabilityLoaned
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class LFAAnalyticsContract {

  private lateinit var executor: ExecutorService

  private val LOG =
    LoggerFactory.getLogger(LFAAnalyticsContract::class.java)

  @JvmField
  @Rule
  var expected = ExpectedException.none()

  @Before
  fun setup() {
    this.executor = Executors.newSingleThreadExecutor()
  }

  @After
  fun tearDown() {
    this.executor.shutdown()
  }

  /**
   * If the server fails to accept analytics data, the data stays in the output until it is
   * accepted.
   */

  @Test
  fun testSimpleRolloverFailure() {
    val context =
      Mockito.mock(Context::class.java)

    val file = File.createTempFile("lfa-analytics-", "dir")
    file.delete()
    file.mkdirs()

    val lfaConfiguration =
      LFAAnalyticsConfiguration(
        deviceID = "eaf06952-141c-4f16-8516-1a2c01503e87",
        logFileSizeLimit = 100,
        servers = listOf(
          LFAAnalyticsServerConfiguration(
            address = URI.create("http://www.example.com/analytics0"),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = URI.create("http://www.example.com/analytics1"),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = URI.create("http://www.example.com/analytics2"),
            authentication = LFAAnalyticsAuthentication.None
          )
        )
      )

    val http = MockingHTTP()
    val error =
      HTTPResultError<InputStream>(
        400,
        "OUCH!",
        0L,
        mutableMapOf(),
        0L,
        ByteArrayInputStream(ByteArray(0)),
        Option.none())

    for (server in lfaConfiguration.servers) {
      for (i in 1..3) {
        http.addResponse(server.address, error)
      }
    }

    val config =
      AnalyticsConfiguration(context, http)

    val system =
      LFAAnalyticsSystem(
        context = configuration.context,
        baseConfiguration = config,
        lfaConfiguration = lfaConfiguration,
        baseDirectory = file,
        executor = this.executor)

    system.onAnalyticsEvent(
      AnalyticsEvent.ProfileCreated(
        profileUUID = UUID.randomUUID(),
        displayName = "Name",
        birthDate = "2020-01-01",
        attributes = sortedMapOf(
          Pair("school", "A School")
        )
      )
    )

    for (i in 1..10) {
      system.onAnalyticsEvent(
        AnalyticsEvent.ApplicationOpened(
          packageName = "com.example",
          packageVersion = "1.0.0",
          packageVersionCode = i))
    }

    Thread.sleep(1000)
    this.executor.submit(Callable { }).get()

    val fileCount = File(file, "outbox").list().size
    Assert.assertEquals(4, fileCount)

    for (server in lfaConfiguration.servers) {
      for (i in 1..5) {
        Assert.assertTrue(http.responsesNow()[server.address]!!.isEmpty())
      }
    }
  }

  /**
   * If the server accepts analytics data, the data is removed from the outbox.
   */

  @Test
  fun testSimpleRollover() {
    val context =
      Mockito.mock(Context::class.java)

    val file = File.createTempFile("lfa-analytics-", "dir")
    file.delete()
    file.mkdirs()

    val http = MockingHTTP()

    val lfaConfiguration =
      LFAAnalyticsConfiguration(
        deviceID = "eaf06952-141c-4f16-8516-1a2c01503e87",
        logFileSizeLimit = 100,
        servers = listOf(
          LFAAnalyticsServerConfiguration(
            address = URI.create("http://www.example.com/analytics0"),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = URI.create("http://www.example.com/analytics1"),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = URI.create("http://www.example.com/analytics2"),
            authentication = LFAAnalyticsAuthentication.None
          )
        )
      )

    http.addResponse(
      lfaConfiguration.servers[0].address,
      HTTPResultOK<InputStream>(
        "OK",
        200,
        ByteArrayInputStream(ByteArray(0)),
        0L,
        mutableMapOf(),
        0L))

    val config =
      AnalyticsConfiguration(context, http)

    val system =
      LFAAnalyticsSystem(
        context = configuration.context,
        baseConfiguration = config,
        lfaConfiguration = lfaConfiguration,
        baseDirectory = file,
        executor = this.executor)

    Assert.assertTrue(!http.responsesNow().isEmpty())

    system.onAnalyticsEvent(
      AnalyticsEvent.ProfileCreated(
        profileUUID = UUID.randomUUID(),
        displayName = "Name",
        birthDate = "2020-01-01",
        attributes = sortedMapOf(
          Pair("school", "A School")
        )
      )
    )

    for (i in 1..2) {
      system.onAnalyticsEvent(
        AnalyticsEvent.ApplicationOpened(
          packageName = "com.example",
          packageVersion = "1.0.0",
          packageVersionCode = i))
    }

    Thread.sleep(2000)
    this.executor.submit(Callable { }).get()

    Assert.assertTrue(File(file, "outbox").list().size == 0)
    Assert.assertTrue(http.responsesNow()[lfaConfiguration.servers[0].address]!!.isEmpty())
  }

  /**
   * Correctly formatted event data is sent.
   */

  @Test
  fun testEventData() {
    val context =
      Mockito.mock(Context::class.java)

    val file = File.createTempFile("lfa-analytics-", "dir")
    file.delete()
    file.mkdirs()

    val http = MockingHTTP()

    val lfaConfiguration =
      LFAAnalyticsConfiguration(
        deviceID = "eaf06952-141c-4f16-8516-1a2c01503e87",
        logFileSizeLimit = 1024,
        servers = listOf(
          LFAAnalyticsServerConfiguration(
            address = URI.create("http://www.example.com/0"),
            authentication = LFAAnalyticsAuthentication.None
          )
        )
      )

    http.addResponse(
      lfaConfiguration.servers[0].address,
      HTTPResultOK<InputStream>(
        "OK",
        200,
        ByteArrayInputStream(ByteArray(0)),
        0L,
        mutableMapOf(),
        0L))

    val config =
      AnalyticsConfiguration(context, http)

    val system =
      LFAAnalyticsSystem(
        context = configuration.context,
        baseConfiguration = config,
        lfaConfiguration = lfaConfiguration,
        baseDirectory = file,
        executor = this.executor)

    Assert.assertTrue(!http.responsesNow().isEmpty())

    system.onAnalyticsEvent(
      AnalyticsEvent.ApplicationOpened(
        timestamp = LocalDateTime(0L),
        packageName = "com.example",
        packageVersion = "1.0.0",
        packageVersionCode = 100
      )
    )

    system.onAnalyticsEvent(
      AnalyticsEvent.ProfileCreated(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        displayName = "Profile0",
        birthDate = "2020-01-01",
        attributes = sortedMapOf<String, String>()
      )
    )

    system.onAnalyticsEvent(
      AnalyticsEvent.ProfileLoggedIn(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        displayName = "Profile0",
        birthDate = "2020-01-01",
        attributes = sortedMapOf<String, String>()
      )
    )

    system.onAnalyticsEvent(
      AnalyticsEvent.CatalogSearched(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        accountProvider = URI("http://www.example.com"),
        accountUUID = UUID.fromString("45b9369e-1004-4422-8ce2-5d5cc54dfc3e"),
        searchQuery = "Book0"
      )
    )

    system.onAnalyticsEvent(
      AnalyticsEvent.CatalogSearched(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        accountProvider = URI("http://www.example.com"),
        accountUUID = UUID.fromString("45b9369e-1004-4422-8ce2-5d5cc54dfc3e"),
        searchQuery = "Book1"
      )
    )

    val opdsAcquisitionFeedEntry0 =
      OPDSAcquisitionFeedEntry.newBuilder(
        "eda7943a-2151-49c7-87d5-c9e16e3437e2",
        "Title",
        DateTime.now(),
        OPDSAvailabilityLoaned.get(Option.none(), Option.none(), Option.none())
      ).build()

    system.onAnalyticsEvent(
      AnalyticsEvent.BookOpened(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        accountProvider = URI("http://www.example.com"),
        accountUUID = UUID.fromString("45b9369e-1004-4422-8ce2-5d5cc54dfc3e"),
        profileDisplayName = "Name",
        opdsEntry = opdsAcquisitionFeedEntry0,
        targetURI = URI.create("http://book-uri.com")
      )
    )

    system.onAnalyticsEvent(
      AnalyticsEvent.BookPageTurned(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        accountProvider = URI("http://www.example.com"),
        accountUUID = UUID.fromString("45b9369e-1004-4422-8ce2-5d5cc54dfc3e"),
        opdsEntry = opdsAcquisitionFeedEntry0,
        bookPage = 0,
        bookPagesTotal = 100,
        bookPageTitle = "Page Title"
      )
    )

    system.onAnalyticsEvent(
      AnalyticsEvent.BookClosed(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        accountProvider = URI("http://www.example.com"),
        accountUUID = UUID.fromString("45b9369e-1004-4422-8ce2-5d5cc54dfc3e"),
        opdsEntry = opdsAcquisitionFeedEntry0
      )
    )

    system.onAnalyticsEvent(
      AnalyticsEvent.ProfileLoggedOut(
        timestamp = LocalDateTime(0L),
        credentials = null,
        profileUUID = UUID.fromString("a0316364-fd7f-41be-8907-95dac45fb647"),
        displayName = "Profile0"
      )
    )

    Thread.sleep(2000)
    this.executor.submit(Callable { }).get()

    Assert.assertEquals(
      resourceText("analyticsLog.txt"),
      File(file, "logFile.txt").readText()
    )
  }

  private fun resourceText(
    name: String
  ): String {
    val path =
      "/one/lfa/android/analytics/${name}"
    val resource =
      LFAAnalyticsContract::class.java.getResourceAsStream(path)
        ?: throw FileNotFoundException(path)
    return String(resource.readBytes())
  }
}

