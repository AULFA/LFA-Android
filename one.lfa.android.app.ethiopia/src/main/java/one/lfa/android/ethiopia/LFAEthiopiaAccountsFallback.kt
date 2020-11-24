package one.lfa.android.ethiopia

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFAEthiopiaAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("content://au.org.libraryforall/58f0fb4b-0f60-4939-a4cf-1b2b74d6c587/feeds/DAE5313E5E65D49C7AFF7388819E7A42A93B0614523BE11C278489FC536670AE.atom"),
      cardCreatorURI = null,
      displayName = "LFA Ethiopia",
      eula = null,
      id = URI.create("urn:uuid:58f0fb4b-0f60-4939-a4cf-1b2b74d6c587"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/ethiopia.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "LFA Ethiopia",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}
