package one.lfa.android.services

object LFAWhitespace {

  @JvmStatic
  val WHITESPACE =
    Regex("[\\p{javaWhitespace}\u00A0\u2007\u202F]+")

  @JvmStatic
  fun isWhitespace(ch: Char): Boolean {
    return Character.isWhitespace(ch)
  }
}