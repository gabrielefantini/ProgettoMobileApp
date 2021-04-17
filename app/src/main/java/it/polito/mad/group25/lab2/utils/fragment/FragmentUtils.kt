package it.polito.mad.group25.lab2.utils.fragment

import android.widget.Toast
import androidx.fragment.app.Fragment
import it.polito.mad.group25.lab2.utils.views.ViewUtils

fun Fragment.showError(errorText: String) =
    Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show()