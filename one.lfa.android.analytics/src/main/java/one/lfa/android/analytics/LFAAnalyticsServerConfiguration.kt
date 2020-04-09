package one.lfa.android.analytics

import java.net.URI

/**
 * Configuration for a single analytics server.
 */

class LFAAnalyticsServerConfiguration(

  /**
   * The address to which analytics data will be POSTed.
   */

  val address: URI,

  /**
   * Authentication information for the server.
   */

  val authentication: LFAAnalyticsAuthentication
)
