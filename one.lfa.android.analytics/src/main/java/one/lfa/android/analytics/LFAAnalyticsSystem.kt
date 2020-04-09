package one.lfa.android.analytics

import com.io7m.jfunctional.Option
import com.io7m.jfunctional.OptionType
import org.nypl.simplified.analytics.api.AnalyticsConfiguration
import org.nypl.simplified.analytics.api.AnalyticsEvent
import org.nypl.simplified.analytics.api.AnalyticsSystem
import org.nypl.simplified.files.DirectoryUtilities
import org.nypl.simplified.files.FileUtilities
import org.nypl.simplified.http.core.HTTPAuthBasic
import org.nypl.simplified.http.core.HTTPAuthType
import org.nypl.simplified.http.core.HTTPProblemReportLogging
import org.nypl.simplified.http.core.HTTPResultError
import org.nypl.simplified.http.core.HTTPResultException
import org.nypl.simplified.http.core.HTTPResultMatcherType
import org.nypl.simplified.http.core.HTTPResultOKType
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.zip.GZIPOutputStream

/**
 * The LFA analytics system.
 */

class LFAAnalyticsSystem(
  private val baseConfiguration: AnalyticsConfiguration,
  private val lfaConfiguration: LFAAnalyticsConfiguration,
  private val baseDirectory: File,
  private val executor: ExecutorService
) : AnalyticsSystem {

  private val logger =
    LoggerFactory.getLogger(LFAAnalyticsSystem::class.java)

  private val outbox =
    File(this.baseDirectory, "outbox")
  private val logFile =
    File(this.baseDirectory, "logFile.txt")

  private lateinit var output: FileWriter

  init {
    this.executor.execute {
      this.logger.debug("creating analytics directory {}", this.baseDirectory)
      DirectoryUtilities.directoryCreate(this.baseDirectory)
      DirectoryUtilities.directoryCreate(this.outbox)
      this.output = FileWriter(this.logFile, true)
      this.executor.execute { this.trySendAll() }
    }
  }

  override fun onAnalyticsEvent(event: AnalyticsEvent): Unit =
    this.executor.execute {
      this.consumeEvent(event)
    }

  private fun consumeEvent(event: AnalyticsEvent) {

    /*
     * Roll over the log file if necessary, and trigger a send of everything else.
     */

    if (this.logFile.length() >= this.lfaConfiguration.logFileSizeLimit) {
      val outboxFile = File(this.outbox, UUID.randomUUID().toString() + ".log")
      FileUtilities.fileRename(this.logFile, outboxFile)
      this.output = FileWriter(this.logFile, true)
      this.trySendAll()
    }

    /*
     * Write the event to the output file.
     */

    val eventText = this.eventToText(event)
    if (eventText != null) {
      this.output.append(eventText)
      this.output.write("\n")
      this.output.flush()
    }
  }

  private fun eventToText(event: AnalyticsEvent): String? {
    return when (event) {
      is AnalyticsEvent.ProfileLoggedIn -> {
        val eventBuilder = StringBuilder(128)
        eventBuilder.append("profile_selected,")
        eventBuilder.append(event.profileUUID)
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(event.displayName))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["gender"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.birthDate)))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["role"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["school"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["grade"])))
        eventBuilder.toString()
      }

      is AnalyticsEvent.ProfileCreated -> {
        val eventBuilder = StringBuilder(128)
        eventBuilder.append("profile_created,")
        eventBuilder.append(event.profileUUID)
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(event.displayName))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["gender"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.birthDate)))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["role"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["school"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["grade"])))
        eventBuilder.toString()
      }

      is AnalyticsEvent.ProfileDeleted -> {
        val eventBuilder = StringBuilder(128)
        eventBuilder.append("profile_deleted,")
        eventBuilder.append(event.profileUUID)
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(event.displayName))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["gender"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.birthDate)))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["role"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["school"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["grade"])))
        eventBuilder.toString()
      }

      is AnalyticsEvent.ProfileUpdated -> {
        val eventBuilder = StringBuilder(128)
        eventBuilder.append("profile_modified,")
        eventBuilder.append(event.profileUUID)
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(event.displayName))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["gender"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.birthDate)))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["role"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["school"])))
        eventBuilder.append(',')
        eventBuilder.append(this.scrubCommas(this.orEmpty(event.attributes["grade"])))
        eventBuilder.toString()
      }

      is AnalyticsEvent.ProfileLoggedOut ->
        null

      is AnalyticsEvent.CatalogSearched ->
        "catalog_searched,${event.searchQuery}"

      is AnalyticsEvent.BookOpened ->
        "book_opened,${event.profileUUID},${event.profileDisplayName},${event.bookTitle}"

      is AnalyticsEvent.BookPageTurned ->
        "book_open_page,${event.bookPage}/${event.bookPagesTotal},${event.bookTitle}"

      is AnalyticsEvent.BookClosed ->
        null

      is AnalyticsEvent.ApplicationOpened ->
        "app_open,${event.packageName},${event.packageVersion},${event.packageVersionCode}"
    }
  }

  private fun trySendAll() {
    this.logger.debug("attempting to send analytics data")
    this.outbox.list().forEach { file ->
      this.executor.execute { this.trySend(File(this.outbox, file)) }
    }
  }

  private fun trySend(file: File) {
    this.logger.debug("attempting send of {}", file)

    val data = this.compressAndReadLogFile(file)
    this.logger.debug("compressed data size: {}", data.size)

    if (data.isEmpty()) {
      file.delete()
      return
    }

    for (serverConfiguration in this.lfaConfiguration.servers) {
      val auth: OptionType<HTTPAuthType> =
        this.httpAuthFor(serverConfiguration.authentication)

      val result =
        this.baseConfiguration.http.post(
          auth,
          serverConfiguration.address,
          data,
          "application/json"
        )

      val sent = result.matchResult(
        object : HTTPResultMatcherType<InputStream, Boolean, Exception> {
          override fun onHTTPError(
            error: HTTPResultError<InputStream>
          ): Boolean {
            HTTPProblemReportLogging.logError(
              this@LFAAnalyticsSystem.logger,
              serverConfiguration.address,
              error.message,
              error.status,
              error.problemReport
            )
            return false
          }

          override fun onHTTPException(
            exception: HTTPResultException<InputStream>
          ): Boolean {
            this@LFAAnalyticsSystem.logger.debug("failed to send analytics data: ", exception.error)
            return false
          }

          @Throws(Exception::class)
          override fun onHTTPOK(
            result: HTTPResultOKType<InputStream>
          ): Boolean {
            this@LFAAnalyticsSystem.logger.debug("server accepted {}, deleting it", file)
            file.delete()
            return true
          }
        })

      if (sent) {
        return
      }
    }

    this.logger.error("failed to send analytics data to any available URI")
  }

  private fun httpAuthFor(
    authentication: LFAAnalyticsAuthentication
  ): OptionType<HTTPAuthType> {
    return when (authentication) {
      LFAAnalyticsAuthentication.None ->
        Option.none()
      is LFAAnalyticsAuthentication.TokenBased -> {
        Option.some(
          HTTPAuthBasic.create(
            this.lfaConfiguration.deviceID,
            authentication.token
          )
        )
      }
    }
  }

  @Throws(IOException::class)
  private fun compressAndReadLogFile(file: File): ByteArray {
    val buffer = ByteArray(4096)
    ByteArrayOutputStream(this.lfaConfiguration.logFileSizeLimit / 10).use { output ->
      GZIPOutputStream(output).use { gzip ->
        FileInputStream(file).use { input ->
          while (true) {
            val r = input.read(buffer)
            if (r == -1) {
              break
            }
            gzip.write(buffer, 0, r)
          }
        }
        gzip.flush()
        gzip.finish()
      }
      return output.toByteArray()
    }
  }

  private fun orEmpty(text: String?): String {
    return text ?: ""
  }

  private fun scrubCommas(text: String): String {
    return text.replace(",", "")
  }
}
