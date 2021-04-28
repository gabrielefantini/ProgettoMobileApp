package it.polito.mad.group25.lab.utils.views

import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout

private data class TextInputLayoutConstraintData(
    val hasConstraints: Boolean,
    val textEditId: Int = -1,
    val checker: (CharSequence) -> Boolean = CharSequence::isNotEmpty,
    val onErrorMessage: String? = null,
    val onToast: Boolean = false
)

fun TextInputLayout.setConstraints(
    textEditId: Int,
    onMissingMessage: String,
    onToast: Boolean = false,
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
            else {
                if (onToast) {
                    Toast.makeText(context, onMissingMessage, Toast.LENGTH_LONG).show()
                    target.error = "!"
                } else target.error = onMissingMessage
            }
    }
    tag = TextInputLayoutConstraintData(true, textEditId, checker, onMissingMessage, onToast)
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
    if (!result) {
        error = if ((tag as TextInputLayoutConstraintData).onToast) {
            Toast.makeText(
                context,
                (tag as TextInputLayoutConstraintData).onErrorMessage,
                Toast.LENGTH_LONG
            ).show()
            "!"
        } else
            (tag as TextInputLayoutConstraintData).onErrorMessage

    }
    return result
}