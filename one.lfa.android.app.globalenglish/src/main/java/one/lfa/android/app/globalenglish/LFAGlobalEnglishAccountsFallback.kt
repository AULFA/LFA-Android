package one.lfa.android.app.globalenglish

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProvider
import org.nypl.simplified.accounts.api.AccountProviderAuthenticationDescription
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFAGlobalEnglishAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProvider(
      addAutomatically = true,
      annotationsURI = null,
      authentication = AccountProviderAuthenticationDescription.Anonymous,
      authenticationAlternatives = listOf(),
      authenticationDocumentURI = null,
      catalogURI = URI.create("content://au.org.libraryforall/3cd65800-4d21-4c56-8506-5843565a3b75/feeds/E388214F46F3111E4D7FE547930C2915E5D44DE3734C7FBFE4BD34D02124977E.atom"),
      cardCreatorURI = null,
      displayName = "Global English",
      eula = null,
      id = URI.create("urn:uuid:3cd65800-4d21-4c56-8506-5843565a3b75"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/globe.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "",
      supportEmail = null,
      supportsReservations = false,
      updated = DateTime.now()
    )
}
