package one.lfa.android.timor

import org.joda.time.DateTime
import org.nypl.simplified.accounts.api.AccountProviderFallbackType
import org.nypl.simplified.accounts.api.AccountProviderImmutable
import org.nypl.simplified.accounts.api.AccountProviderType
import java.net.URI

class LFATimorAccountsFallback : AccountProviderFallbackType {
  override fun get(): AccountProviderType =
    AccountProviderImmutable(
      addAutomatically = true,
      annotationsURI = null,
      authentication = null,
      authenticationDocumentURI = null,
      catalogURI = URI.create("content://au.org.libraryforall/f07ef516-ea25-498f-ba23-80ff2904e6e6/feeds/B3CB1505FEF3ADA2894675D1077D8D41CC55A5D4858611ECC9712BABE77A8B4D.atom"),
      cardCreatorURI = null,
      displayName = "LFA Timor",
      eula = null,
      id = URI.create("urn:uuid:f07ef516-ea25-498f-ba23-80ff2904e6e6"),
      idNumeric = -1,
      isProduction = true,
      license = null,
      loansURI = null,
      logo = URI.create("simplified-asset:logos/timor.png"),
      mainColor = "#ec1c24",
      patronSettingsURI = null,
      privacyPolicy = null,
      subtitle = "LFA Laos",
      supportEmail = null,
      supportsReservations = false,
      supportsSimplyESynchronization = false,
      updated = DateTime.now()
    )
}
