package one.lfa.android.services

import org.nypl.simplified.ui.settings.SettingsConfigurationServiceType

/**
 * The settings configuration service for the application.
 */

class LFASettingsConfigurationService : SettingsConfigurationServiceType {

  override val allowAccountsAccess: Boolean
    get() = false

}
