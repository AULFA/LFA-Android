package one.lfa.android.analytics

import org.nypl.simplified.analytics.api.AnalyticsEvent
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class AnalyticsEventsThrottler(private val executor: ScheduledExecutorService, private val targetFunction: (AnalyticsEvent) -> Unit) {
  private val updateEvents = ArrayDeque<AnalyticsEvent.ProfileUpdated>()
  private var lastCreateEvent: AnalyticsEvent.ProfileCreated? = null
  private var isThrottlingProfileCreated = false
  private var isThrottlingProfileUpdated = false
  private var profileUpdatedTimer: ScheduledFuture<*>? = null

  fun handleEvent(event: AnalyticsEvent) {
    when (event) {
      is AnalyticsEvent.ProfileCreated -> {
        // The current version of Simplified generates a ProfileCreated event and 1-2 ProfileUpdated events when a user is created.
        // We're going to collect all these events in a single ProfileCreated event.
        lastCreateEvent = event

        if (isThrottlingProfileCreated) {
          return
        }

        isThrottlingProfileCreated = true
        executor.schedule({
          synchronized(this) {
            var totalEvent = lastCreateEvent?.copy()
            updateEvents.forEach { collectedEvent ->
              totalEvent = totalEvent?.copy(birthDate = collectedEvent.birthDate, attributes = collectedEvent.attributes)
            }

            totalEvent?.let { targetFunction(it) }
            updateEvents.clear()
            lastCreateEvent = null
            isThrottlingProfileCreated = false
          }
        }, 700, TimeUnit.MILLISECONDS)
      }
      is AnalyticsEvent.ProfileUpdated -> {
        // The current version of Simplified generates a ProfileUpdated event and several BookPageTurned events when the reader's text settings are updated.
        // This is because the text settings are tied to the user's profile, and when they're changed the current page is re-rendered.
        // We'll emit only the ProfileUpdated event and throttle the BookPageTurned events for 700 milliseconds.
        if (isThrottlingProfileCreated) {
          updateEvents.add(event)
          return
        }

        isThrottlingProfileUpdated = true
        targetFunction(event)
        profileUpdatedTimer?.cancel(true)
        profileUpdatedTimer = executor.schedule({
          isThrottlingProfileUpdated = false
        }, 700, TimeUnit.MILLISECONDS)
      }
      is AnalyticsEvent.BookPageTurned -> {
        if (isThrottlingProfileUpdated) {
          return
        }

        targetFunction(event)
      }
      else -> {
        targetFunction(event)
      }
    }
  }

}
