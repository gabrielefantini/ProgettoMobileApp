package it.polito.mad.group25.lab.utils.fragment

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.showError(errorText: String) =
    Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show()