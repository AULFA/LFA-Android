package one.lfa.android.grande

import org.nypl.simplified.ui.branding.BrandingSplashServiceType

/**
 * A splash service for the app.
 */

class LFAOnlineSplashService : BrandingSplashServiceType {
  override fun splashImageResource(): Int {
    return R.drawable.splash
  }
}
