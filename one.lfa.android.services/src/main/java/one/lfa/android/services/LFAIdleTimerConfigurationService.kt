package one.lfa.android.services

import org.nypl.simplified.profiles.api.idle_timer.ProfileIdleTimerConfigurationServiceType

/**
 * The idle timer configuration service for the application.
 */

class LFAIdleTimerConfigurationService : ProfileIdleTimerConfigurationServiceType {

  override val warningWhenSecondsRemaining: Int
    get() = 60

  override val logOutAfterSeconds: Int
    get() = 10 * 60

}
