package one.lfa.android.laos.online

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFALaosAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Basic(
        description = "Basic",
        barcodeFormat = null,
        passwordKeyboard = AccountProviderAuthenticationDescription.KeyboardInput.DEFAULT,
        keyboard = AccountProviderAuthenticationDescription.KeyboardInput.DEFAULT,
        passwordMaximumLength = 0,
        labels = mapOf(),
        logoURI = null
      ),
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("https://lfalaos.cantookstation.com/catalog/featuredresources.atom"),
      cardCreatorURI = null,
      displayName = "ປະເທດລາວ",
      eula = null,
      id = URI.create("urn:provider:com.cantookstation.lfalaos"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/laos.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}
