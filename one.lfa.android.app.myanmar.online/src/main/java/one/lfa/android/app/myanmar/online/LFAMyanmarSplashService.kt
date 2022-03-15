package one.lfa.android.app.myanmar.online

import org.nypl.simplified.ui.branding.BrandingSplashServiceType

/**
 * A splash service for the app.
 */

class LFAMyanmarSplashService : BrandingSplashServiceType {

  override val shouldShowLibrarySelectionScreen: Boolean =
    false

  override fun splashImageResource(): Int {
    return R.drawable.splash
  }

  override fun splashImageTitleResource(): Int {
    return R.drawable.splash
  }
}
