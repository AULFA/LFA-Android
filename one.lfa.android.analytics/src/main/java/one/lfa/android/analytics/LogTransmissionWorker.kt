package one.lfa.android.analytics

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.joda.time.LocalDateTime
import org.nypl.simplified.analytics.api.AnalyticsEvent
import org.nypl.simplified.analytics.api.AnalyticsType
import org.librarysimplified.services.api.Services
import org.slf4j.LoggerFactory
import java.lang.Exception

/**
 * A task that requests an analytics sync event
 */

class LogTransmissionWorker(
        context: Context,
        workerParameters: WorkerParameters)
  : Worker(context, workerParameters) {

  private val logger = LoggerFactory.getLogger(LogTransmissionWorker::class.java)

  override fun doWork(): Result {
    try {
      val services = Services.serviceDirectory()
      val analytics = services.requireService(AnalyticsType::class.java)

      analytics.publishEvent(
              AnalyticsEvent.SyncRequested(
                      timestamp = LocalDateTime.now(),
                      credentials = null
              )
      )
    } catch (e: Exception) {
      logger.error("LogTransmissionWorker exception: ", e)
    }

    return Result.success()
  }
}
