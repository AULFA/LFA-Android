package one.lfa.android.analytics

import android.content.Context
import com.io7m.jfunctional.Option
import com.io7m.jfunctional.OptionType
import com.io7m.junreachable.UnreachableCodeException
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat
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
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

/**
 * The LFA analytics system.
 */

class LFAAnalyticsSystem(
  private val context: Context,
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

  private val dateFormatter =
    DateTimeFormatterBuilder()
      .appendYear(4, 4)
      .appendMonthOfYear(2)
      .appendDayOfMonth(2)
      .appendHourOfDay(2)
      .appendMinuteOfHour(2)
      .appendSecondOfMinute(2)
      .appendMillisOfSecond(4)
      .toFormatter()

  private lateinit var output: FileWriter

  @Volatile
  private var latestSchoolName: String? = null

  init {
    this.executor.execute {
      this.logger.debug("creating analytics directory {}", this.baseDirectory)
      DirectoryUtilities.directoryCreate(this.baseDirectory)
      DirectoryUtilities.directoryCreate(this.outbox)
      this.output = FileWriter(this.logFile, true)
      this.executor.execute { this.trySendAll() }
      this.enqueueLogTransmissionTask(context)
    }
  }

  private fun enqueueLogTransmissionTask(context: Context) {

    /*
     * Start a task to handle log transmissions.
     */

    val workRequestContraints =
            Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true)
                    .build()

    val workRequest =
            PeriodicWorkRequestBuilder<LogTransmissionWorker>(1, TimeUnit.MINUTES)
                    .setConstraints(workRequestContraints)
                    .setInitialDelay(1L, TimeUnit.MINUTES)
                    .addTag("one.lfa.android.analytics")
                    .build()

    WorkManager.getInstance(context)
            .enqueue(workRequest)
  }

  override fun onAnalyticsEvent(event: AnalyticsEvent): Unit =
    this.executor.execute {
      this.consumeEvent(event)
    }

  private fun consumeEvent(event: AnalyticsEvent) {

      /*
       * If the event is a sync request, roll the log file over and send it right away.
       */

      if (event is AnalyticsEvent.SyncRequested) {
        this.trySendAll()
        return
      }

    /*
     * Roll over the log file if necessary, and trigger a send of everything else.
     */


    if (this.logFile.length() >= this.lfaConfiguration.logFileSizeLimit) {
      this.rolloverLog()
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

  private fun rolloverLog() {
    val outboxFile = File(this.outbox, UUID.randomUUID().toString() + ".log")
    FileUtilities.fileRename(this.logFile, outboxFile)
    this.output = FileWriter(this.logFile, true)
  }

  private fun eventToText(event: AnalyticsEvent): String? {
    val bodyText = this.eventBodyToText(event) ?: return null
    val timestamp = ISODateTimeFormat.dateTimeNoMillis().print(event.timestamp)
    return "$timestamp,$bodyText"
  }

  private fun eventBodyToText(event: AnalyticsEvent): String? {
    return when (event) {
      is AnalyticsEvent.ProfileLoggedIn -> {
        this.latestSchoolName = this.orEmpty(event.attributes["school"])

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
        this.latestSchoolName = this.orEmpty(event.attributes["school"])

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
        this.latestSchoolName = this.orEmpty(event.attributes["school"])

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
        this.latestSchoolName = this.orEmpty(event.attributes["school"])

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
        "book_opened,${event.profileUUID},${event.profileDisplayName},${event.opdsEntry.title}"

      is AnalyticsEvent.BookPageTurned ->
        "book_open_page,${event.bookPage}/${event.bookPagesTotal},${event.opdsEntry.title}"

      is AnalyticsEvent.BookClosed ->
        null

      is AnalyticsEvent.ApplicationOpened ->
        "app_open,${event.packageName},${event.packageVersion},${event.packageVersionCode}"

      is AnalyticsEvent.SyncRequested -> {
        throw UnreachableCodeException()
      }
    }
  }

  private fun trySendAll() {
    this.logger.debug("attempting to send analytics data")
    val files = this.outbox.listFiles()
    this.logger.debug("{} analytics data files are ready for sending", files.size)
    for (file in files) {
      this.executor.execute { this.trySend(file) }
    }
  }

  private fun trySend(
    file: File
  ) {
    this.copyToExternalStorage(file)

    this.logger.debug("attempting send of {}", file)

    val data = this.compressAndReadLogFile(file)
    this.logger.debug("compressed data size: {}", data.size)

    if (data.isEmpty()) {
      file.delete()
      return
    }

    var sent = false
    for (serverConfiguration in this.lfaConfiguration.servers) {
      this.logger.debug("post {}", serverConfiguration.address)

      val auth: OptionType<HTTPAuthType> =
        this.httpAuthFor(serverConfiguration.authentication)

      val result =
        this.baseConfiguration.http.post(
          auth,
          serverConfiguration.address,
          data,
          "application/json"
        )

      sent = sent || result.matchResult(
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
    }

    if (sent) {
      return
    }

    this.logger.error("failed to send analytics data to any available URI")
  }

  private fun copyToExternalStorage(file: File) {
    try {
      val cacheDir = this.context.externalCacheDir
      if (cacheDir == null) {
        this.logger.error("external cache directory is not available")
        return
      }

      val analyticsDir = File(cacheDir, "analytics")
      analyticsDir.mkdirs()
      val outputFile = File(analyticsDir, file.name)
      this.logger.debug("copying {} -> {}", file, outputFile)
      FileUtilities.fileCopy(file, outputFile)
      this.logger.debug("copied {} -> {}", file, outputFile)
    } catch (e: Exception) {
      this.logger.error("could not copy analytics log: ", e)
    }
  }

  private fun tokenUsername(): String {
    val schoolName =
      checkNotNull(this.latestSchoolName)
    val timestamp =
      this.dateFormatter.print(LocalDateTime.now())

    return String.format(
      "%s_%s_%s",
      schoolName,
      this.lfaConfiguration.deviceID,
      timestamp
    )
  }

  private fun httpAuthFor(
    authentication: LFAAnalyticsAuthentication
  ): OptionType<HTTPAuthType> {
    return when (authentication) {
      LFAAnalyticsAuthentication.None ->
        Option.none()
      is LFAAnalyticsAuthentication.TokenBased ->
        Option.some(HTTPAuthBasic.create(this.tokenUsername(), authentication.token))
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
