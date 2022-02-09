package one.lfa.android.kenya

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFAKenyaAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("content://au.org.libraryforall/58f0fb4b-0f60-4939-a4cf-1b2b74d6c587/feeds/A3F5F15BA1946CB9268F3BBBED376135F0638A375532F18033F6482CBD1B1075.atom"),
      cardCreatorURI = null,
      displayName = "Somali Collection",
      eula = null,
      id = URI.create("urn:uuid:58f0fb4b-0f60-4939-a4cf-1b2b74d6c587"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/somalia.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "Somali Collection",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}
