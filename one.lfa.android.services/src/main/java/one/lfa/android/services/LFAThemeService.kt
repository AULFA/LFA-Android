package one.lfa.android.services

import org.nypl.simplified.ui.branding.BrandingThemeOverrideServiceType
import org.nypl.simplified.ui.theme.ThemeValue

/**
 * A theme service for the app.
 */

class LFAThemeService : BrandingThemeOverrideServiceType {
  override fun overrideTheme(): ThemeValue {
    return ThemeValue(
      name = "LFA",
      colorLight = R.color.lfaPrimaryLight,
      colorDark = R.color.lfaPrimaryDark,
      color = R.color.lfaPrimary,
      themeWithActionBar = R.style.LFA_ActionBar,
      themeWithNoActionBar = R.style.LFA_NoActionBar)
  }
}