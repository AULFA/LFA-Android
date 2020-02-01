package one.lfa.android.timor

import org.nypl.simplified.ui.branding.BrandingSplashServiceType

/**
 * A splash service for the app.
 */

class LFATimorSplashService : BrandingSplashServiceType {
  override fun splashImageResource(): Int {
    return R.drawable.splash
  }
}
