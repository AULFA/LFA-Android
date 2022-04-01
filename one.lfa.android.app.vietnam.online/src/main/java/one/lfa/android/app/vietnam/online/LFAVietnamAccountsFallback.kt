package one.lfa.android.app.vietnam.online

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFAVietnamAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("https://lfavietnamcollection.cantookstation.com/catalog/featuredresources.atom"),
      cardCreatorURI = null,
      displayName = "Việt Nam",
      eula = null,
      id = URI.create("urn:provider:com.cantookstation.lfavietnamcollection"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/vietnam.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}
