package one.lfa.android.app.ethiopia

import org.nypl.simplified.ui.branding.BrandingSplashServiceType

/**
 * A splash service for the app.
 */

class LFAEthiopiaSplashService : BrandingSplashServiceType {

  override val shouldShowLibrarySelectionScreen: Boolean =
    false

  override fun splashImageResource(): Int {
    return R.drawable.splash
  }

  override fun splashImageTitleResource(): Int {
    return R.drawable.splash
  }
}
