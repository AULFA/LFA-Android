package one.lfa.android.app.indigenous

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFAIndigenousAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("https://lfaaustralianindigenouscollection.cantookstation.com/catalog/featuredresources.atom"),
      cardCreatorURI = null,
      displayName = "LFA Australian Aboriginal & Torres Strait Islander",
      eula = null,
      id = URI.create("urn:uuid:c6537298-674d-421a-8997-e6dfe5d9000d"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/indigenous.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "LFA Australian Aboriginal & Torres Strait Islander",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}
