package it.polito.mad.group25.lab.utils.views

import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout

private data class TextInputLayoutConstraintData(
    val hasConstraints: Boolean,
    val textEditId: Int = -1,
    val checker: (CharSequence) -> Boolean = CharSequence::isNotEmpty,
    val onErrorMessage: String? = null
)

fun TextInputLayout.setConstraints(
    textEditId: Int,
    onMissingMessage: String,
    checker: (CharSequence) -> Boolean
) {
    val textView = findViewById<TextView>(textEditId)
        ?: throw IllegalArgumentException("Given $textEditId text view id not a child of $id")
    val target = this
    textView.setOnFocusChangeListener { v, hasFocus ->
        v as TextView
        if (!hasFocus)
            if (checker(v.text))
                target.error = null
            else target.error = onMissingMessage
    }
    tag = TextInputLayoutConstraintData(true, textEditId, checker, onMissingMessage)
}

fun TextInputLayout.removeConstraints() {
    this.setOnFocusChangeListener { _, _ -> }
    tag = TextInputLayoutConstraintData(false)
}

fun TextInputLayout.isCompliant(): Boolean {
    if (tag !is TextInputLayoutConstraintData) return false
    if (!(tag as TextInputLayoutConstraintData).hasConstraints) return false
    val textView = findViewById<TextView>((tag as TextInputLayoutConstraintData).textEditId)
    val result = textView != null && (tag as TextInputLayoutConstraintData).checker(textView.text)
    if (!result)
        error = (tag as TextInputLayoutConstraintData).onErrorMessage
    return result
}