package one.lfa.android.tests

import android.content.Context
import com.io7m.jfunctional.Option
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import one.lfa.android.analytics.LFAAnalyticsAuthentication
import one.lfa.android.analytics.LFAAnalyticsConfiguration
import one.lfa.android.analytics.LFAAnalyticsServerConfiguration
import one.lfa.android.analytics.LFAAnalyticsSystem
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.api.LSHTTPClientType
import org.librarysimplified.http.vanilla.LSHTTPClients
import org.mockito.Mockito
import org.nypl.simplified.analytics.api.AnalyticsConfiguration
import org.nypl.simplified.analytics.api.AnalyticsEvent
import org.nypl.simplified.opds.core.OPDSAcquisitionFeedEntry
import org.nypl.simplified.opds.core.OPDSAvailabilityLoaned
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

abstract class LFAAnalyticsContract {

  private lateinit var http: LSHTTPClientType
  private lateinit var server: MockWebServer
  private lateinit var executor: ExecutorService

  private val LOG =
    LoggerFactory.getLogger(LFAAnalyticsContract::class.java)

  @JvmField
  @Rule
  var expected = ExpectedException.none()

  @Before
  fun setup() {
    val config =
      LSHTTPClientConfiguration(
        applicationName = "tests",
        applicationVersion = "1.0.0"
      )
    this.http = LSHTTPClients().create(Mockito.mock(Context::class.java), config)
    this.executor = Executors.newSingleThreadExecutor()
    this.server = MockWebServer()
    this.server.start(20000)
  }

  @After
  fun tearDown() {
    this.executor.shutdown()
    this.server.close()
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
            address = this.server.url("analytics0").toUri(),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = this.server.url("analytics1").toUri(),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = this.server.url("analytics2").toUri(),
            authentication = LFAAnalyticsAuthentication.None
          )
        )
      )

    for (server in lfaConfiguration.servers) {
      for (i in 1..10) {
        this.server.enqueue(MockResponse().setResponseCode(400))
      }
    }

    val config =
      AnalyticsConfiguration(context, http)

    val system =
      LFAAnalyticsSystem(
        context = context,
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
    assertEquals(4, fileCount)
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

    val lfaConfiguration =
      LFAAnalyticsConfiguration(
        deviceID = "eaf06952-141c-4f16-8516-1a2c01503e87",
        logFileSizeLimit = 100,
        servers = listOf(
          LFAAnalyticsServerConfiguration(
            address = this.server.url("analytics0").toUri(),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = this.server.url("analytics1").toUri(),
            authentication = LFAAnalyticsAuthentication.None
          ),
          LFAAnalyticsServerConfiguration(
            address = this.server.url("analytics2").toUri(),
            authentication = LFAAnalyticsAuthentication.None
          )
        )
      )

    val config =
      AnalyticsConfiguration(context, http)

    for (server in lfaConfiguration.servers) {
      this.server.enqueue(MockResponse().setResponseCode(200))
    }

    val system =
      LFAAnalyticsSystem(
        context = context,
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

    for (i in 1..2) {
      system.onAnalyticsEvent(
        AnalyticsEvent.ApplicationOpened(
          packageName = "com.example",
          packageVersion = "1.0.0",
          packageVersionCode = i))
    }

    Thread.sleep(2000)
    this.executor.submit(Callable { }).get()

    val outboxDirectory = File(file, "outbox")
    assertTrue(outboxDirectory.list().isEmpty())

    val request0 = this.server.takeRequest()
    assertEquals("POST", request0.method)
    assertTrue(request0.bodySize > 90L)

    val uncompressedBytes =
      GZIPInputStream(ByteArrayInputStream(request0.body.readByteArray())).readBytes()
    val uncompressedText =
      String(uncompressedBytes, StandardCharsets.UTF_8)

    LOG.debug("text: {}", uncompressedText)
    assertTrue(uncompressedText.contains("profile_created"))
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

    val lfaConfiguration =
      LFAAnalyticsConfiguration(
        deviceID = "eaf06952-141c-4f16-8516-1a2c01503e87",
        logFileSizeLimit = 1024,
        servers = listOf(
          LFAAnalyticsServerConfiguration(
            address = this.server.url("analytics0").toUri(),
            authentication = LFAAnalyticsAuthentication.None
          )
        )
      )

    val config =
      AnalyticsConfiguration(context, http)

    this.server.enqueue(MockResponse().setResponseCode(200))

    val system =
      LFAAnalyticsSystem(
        context = context,
        baseConfiguration = config,
        lfaConfiguration = lfaConfiguration,
        baseDirectory = file,
        executor = this.executor)

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

    assertEquals(
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

