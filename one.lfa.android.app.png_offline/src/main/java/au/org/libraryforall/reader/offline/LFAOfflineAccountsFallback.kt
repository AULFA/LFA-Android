package au.org.libraryforall.reader.offline

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFAOfflineAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("content://au.org.libraryforall/e5d0c561-aa74-4f19-a043-c084f8346212/feeds/7A0A560E3AF8DDDD996E01FD43C618413A8A2B6CC118933F950C3618939EE1CD.atom"),
      cardCreatorURI = null,
      displayName = "Papua New Guinea",
      eula = null,
      id = URI.create("urn:provider:com.cantookstation.lfa.bundled"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:bundled.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}