package one.lfa.android.analytics

/**
 * Authentication details.
 */

sealed class LFAAnalyticsAuthentication {

  /**
   * No authentication required.
   */

  object None : LFAAnalyticsAuthentication()

  /**
   * Token-based authentication required.
   */

  data class TokenBased(
    val token: String
  ) : LFAAnalyticsAuthentication()
}
