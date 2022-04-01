package one.lfa.android.app.vietnam

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
      catalogURI = URI.create("content://au.org.libraryforall/0c41f814-fd53-431f-8d18-2d861544d26a/feeds/07A1BEDCC41D0B7C9B1194CA395AC20C0155188951511B7B0C8F2E9BA8DCCA31.atom"),
      cardCreatorURI = null,
      displayName = "Vietnam",
      eula = null,
      id = URI.create("urn:uuid:0c41f814-fd53-431f-8d18-2d861544d26a"),
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
