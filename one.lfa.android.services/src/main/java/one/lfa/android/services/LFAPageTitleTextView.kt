package one.lfa.android.services

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * A simple extension of AppCompatTextView that handles cases where we're getting corrupted page titles from NYPL-Simplified/Simplified-Android-Core.
 * This happens (rarely) when we're handling languages that use non-latin characters. As of 11-03-2022 we have only instance of this happening with LFA's books.
 *
 * @constructor
 * Same as AppCompatTextView
 *
 * @param context
 * @param attrs
 */
class LFAPageTitleTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

  override fun setText(text: CharSequence, type: BufferType?) {
    if (text.isNullOrEmpty()) {
      super.setText(text, type)

      return
    }

    for ((original, sub) in PAGE_TITLES_MAP) {
      if (text.toString().contains(original)) {
        super.setText(text.toString().replace(original, sub), type)

        return
      }
    }

    super.setText(text, type)
  }

  companion object {
    val PAGE_TITLES_MAP = mapOf(
      "ဒီီစာာအုုပ် ်က ဖတ််လို့့�ကော�\uDBC0\uDCE0 ာင်းး\uDBC0\uDCE1လားး \uDBC0\uDCE1။" to "ဒီစာအုပ်က ဖတ်လို့ကောင်းလား။"
    )
  }

}
