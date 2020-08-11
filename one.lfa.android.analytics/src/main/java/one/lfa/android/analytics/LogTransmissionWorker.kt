package one.lfa.android.analytics

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.joda.time.LocalDateTime
import org.nypl.simplified.analytics.api.AnalyticsEvent
import org.nypl.simplified.analytics.api.AnalyticsType

/**
 * A task that updates the repositories and then publishes a notification if an update
 * is available.
 */

class LogTransmissionWorker(
        context: Context,
        workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    private lateinit var analytics: AnalyticsType

    override fun doWork(): Result {
        // @Mark -> analytics is obviously null.  Where should this class be instantiated so I can pass it the analytics system?
        this.analytics.publishEvent(
                AnalyticsEvent.SyncRequested(
                        timestamp = LocalDateTime.now(),
                        credentials = null
                )
        )

        return Result.success()
    }
}