package one.lfa.android.analytics

import org.w3c.dom.Element
import java.io.InputStream
import java.net.URI
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Configuration information for LFA analytics.
 */

data class LFAAnalyticsConfiguration(

  /**
   * The list of server configurations.
   */

  val servers: List<LFAAnalyticsServerConfiguration>,

  /**
   * A unique ID for this device.
   */

  val deviceID: String,

  /**
   * The maximum size of a log file in bytes.
   */

  val logFileSizeLimit: Int
) {

  init {
    check(this.servers.isNotEmpty()) { "Must specify at least one server "}
  }

  private data class Parameters(
    var deviceID: String,
    var logFileSizeLimit: Int = 1024 * 1024 * 10
  )

  companion object {

    const val NAMESPACE_1_0 =
      "urn:one.lfa.android.analytics.configuration:1:0"

    fun parseFromStream(
      stream: InputStream,
      deviceID: String
    ): LFAAnalyticsConfiguration {
      val documentBuilders = DocumentBuilderFactory.newInstance()
      documentBuilders.isNamespaceAware = true
      documentBuilders.isValidating = false
      documentBuilders.isXIncludeAware = false

      val documentBuilder = documentBuilders.newDocumentBuilder()
      val document = documentBuilder.parse(stream)
      val root = document.firstChild
      return this.parseFromXML(root as Element, deviceID)
    }

    fun parseFromXML(
      element: Element,
      deviceID: String
    ): LFAAnalyticsConfiguration {
      val parameters =
        Parameters(deviceID)

      this.parseParameters(
        element = this.firstOfAny(element, "Parameters")!!,
        parameters = parameters
      )

      val servers =
        this.parseServers(element)

      return LFAAnalyticsConfiguration(
        servers = servers,
        deviceID = parameters.deviceID,
        logFileSizeLimit = parameters.logFileSizeLimit
      )
    }

    private fun parseServers(
      element: Element
    ): List<LFAAnalyticsServerConfiguration> {
      val servers =
        mutableListOf<LFAAnalyticsServerConfiguration>()

      val serversElement = this.firstOfAny(element, "Servers")!!
      val childCount = serversElement.childNodes.length
      for (childIndex in 0 until childCount) {
        val child = serversElement.childNodes.item(childIndex)
        if (child is Element) {
          servers.add(this.parseServer(child))
        }
      }
      return servers.toList()
    }

    private fun parseServer(
      element: Element
    ): LFAAnalyticsServerConfiguration {
      check(element.tagName == "Server")

      val targetURI =
        URI.create(element.getAttributeNS(null, "address"))

      val authentication =
        this.parseAuthentication(
          this.firstOfAny(element, "AuthenticationNone", "AuthenticationTokenBased")!!
        )

      return LFAAnalyticsServerConfiguration(
        targetURI,
        authentication
      )
    }

    private fun firstOfAny(
      element: Element,
      names: Set<String>
    ): Element? {
      val childCount = element.childNodes.length
      for (index in 0 until childCount) {
        val child = element.childNodes.item(index)
        if (names.contains(child.localName)) {
          return child as Element
        }
      }
      return null
    }

    private fun firstOfAny(
      element: Element,
      vararg names: String
    ): Element? {
      return this.firstOfAny(element, names.toSet())
    }

    private fun parseAuthentication(
      element: Element
    ): LFAAnalyticsAuthentication {
      check(
        element.tagName == "AuthenticationNone" || element.tagName == "AuthenticationTokenBased"
      )
      return when (element.tagName) {
        "AuthenticationNone" ->
          LFAAnalyticsAuthentication.None
        "AuthenticationTokenBased" ->
          LFAAnalyticsAuthentication.TokenBased(
            element.getAttributeNS(null, "tokenValue")
          )
        else ->
          throw IllegalStateException("Unexpected element: ${element.tagName}")
      }
    }

    private fun parseParameters(
      element: Element,
      parameters: Parameters
    ): Parameters {
      check(element.tagName == "Parameters")

      parameters.logFileSizeLimit =
        element.getAttributeNS(null, "logFileSizeLimit").toInt()
      return parameters
    }
  }
}
