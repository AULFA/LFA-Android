package au.org.libraryforall.reader.solomon

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFASolomonAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("content://au.org.libraryforall/0b063894-7c42-40db-9194-01b9281bc73c/feeds/88D92F5F06E62596CEB61976A23A54DB6414EC113151FF879C04D1CD7C85AF81.atom"),
      cardCreatorURI = null,
      displayName = "Solomon Islands",
      eula = null,
      id = URI.create("urn:provider:com.cantookstation.lfasolomonislandscollection"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/solomon_islands.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}
