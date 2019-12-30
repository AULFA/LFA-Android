package one.lfa.android.services

import org.nypl.simplified.ui.catalog.CatalogConfigurationServiceType

/**
 * The catalog configuration service for the application.
 */

class LFACatalogConfigurationService : CatalogConfigurationServiceType {

  override val showSettingsTab: Boolean
    get() = true

  override val showHoldsTab: Boolean
    get() = false

  override val showAllCollectionsInLocalFeeds: Boolean
    get() = true

  override val supportErrorReportEmailAddress: String
    get() = "co+org.libraryforall.errors@io7m.com"

  override val supportErrorReportSubject: String
    get() = "[LFA error report]"
}
