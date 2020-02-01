package au.org.libraryforall.reader.offline

import org.nypl.simplified.ui.branding.BrandingSplashServiceType

/**
 * A splash service for the app.
 */

class LFAOfflineSplashService : BrandingSplashServiceType {
  override fun splashImageResource(): Int {
    return R.drawable.splash
  }
}