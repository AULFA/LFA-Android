package one.lfa.android.laos

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderDescription
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFALaosAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("content://au.org.libraryforall/f5b1f8d9-43ad-42ea-9b96-a291e7ce8fb5/feeds/DAE5313E5E65D49C7AFF7388819E7A42A93B0614523BE11C278489FC536670AE.atom"),
      cardCreatorURI = null,
      displayName = "LFA Laos",
      eula = null,
      id = URI.create("urn:uuid:f5b1f8d9-43ad-42ea-9b96-a291e7ce8fb5"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/laos.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "LFA Laos",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}