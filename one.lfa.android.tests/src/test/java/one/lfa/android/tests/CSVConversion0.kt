package one.lfa.android.tests

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.nio.charset.Charset

class CSVConversion0 {
  companion object {

    @JvmStatic
    fun main(args: Array<String>) {
      val csv =
        CSVParser.parse(File("timor.csv"), Charset.defaultCharset(), CSVFormat.DEFAULT)

      println("<resources>")
      csv.forEach {
        println("<string name=\"${it.get(0)}\">${it.get(2)}</string>")
      }
      println("</resources>")
    }
  }
}
