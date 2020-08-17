package one.lfa.android.services

import org.nypl.simplified.buildconfig.api.BuildConfigOAuthScheme
import org.nypl.simplified.buildconfig.api.BuildConfigurationServiceType
import org.nypl.simplified.main.BuildConfig

class LFABuildConfigurationService : BuildConfigurationServiceType {

  override val allowAccountsAccess: Boolean
    get() = false

  override val allowAccountsRegistryAccess: Boolean
    get() = false

  override val oauthCallbackScheme: BuildConfigOAuthScheme
    get() = BuildConfigOAuthScheme("lfa_oauth")

  override val showDebugBookDetailStatus: Boolean
    get() = false

  override val showHoldsTab: Boolean
    get() = false

  override val showSettingsTab: Boolean
    get() = true

  override val supportErrorReportEmailAddress: String
    get() = "co+org.libraryforall.errors@io7m.com"

  override val supportErrorReportSubject: String
    get() = "[LFA error report]"

  override val vcsCommit: String
    get() = BuildConfig.GIT_COMMIT
}