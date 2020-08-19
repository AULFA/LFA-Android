package one.lfa.android.analytics

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.joda.time.LocalDateTime
import org.nypl.simplified.analytics.api.AnalyticsEvent
import org.nypl.simplified.analytics.api.AnalyticsType
import org.librarysimplified.services.api.Services
import java.lang.Exception

/**
 * A task that updates the repositories and then publishes a notification if an update
 * is available.
 */

class LogTransmissionWorker(
        context: Context,
        workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

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
        } catch (e: Exception) { }

        return Result.success()
    }
}