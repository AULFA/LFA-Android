package one.lfa.android.grande

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFAOnlineAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = null,
      authenticationDocumentURI = null,
      catalogURI = URI.create("https://lfa.cantookstation.com/catalog/featuredresources.atom"),
      cardCreatorURI = null,
      displayName = "Papua New Guinea",
      eula = null,
      id = URI.create("urn:provider:com.cantookstation.lfa"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/papua_new_guinea.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}