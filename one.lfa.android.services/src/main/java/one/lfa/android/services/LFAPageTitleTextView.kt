package one.lfa.android.services

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

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
