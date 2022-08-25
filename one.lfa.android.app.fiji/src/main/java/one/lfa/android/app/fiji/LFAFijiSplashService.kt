package one.lfa.android.app.fiji

import org.nypl.simplified.ui.branding.BrandingSplashServiceType

/**
 * A splash service for the app.
 */

class LFAFijiSplashService : BrandingSplashServiceType {

  override val shouldShowLibrarySelectionScreen: Boolean =
    true

  override fun splashImageResource(): Int {
    return R.drawable.splash
  }

  override fun splashImageTitleResource(): Int {
    return R.drawable.splash
  }
}
