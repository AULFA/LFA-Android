package one.lfa.android.laos

import org.nypl.simplified.ui.branding.BrandingSplashServiceType

/**
 * A splash service for the app.
 */

class LFALaosSplashService : BrandingSplashServiceType {
  override fun splashImageResource(): Int {
    return R.drawable.splash
  }

  override fun splashImageTitleResource(): Int {
    return R.drawable.splash
  }
}
