package it.polito.mad.group25.lab.utils.activity

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.util.*


fun Activity.showError(errorText: String) =
    Toast.makeText(this, errorText, Toast.LENGTH_LONG).show()

private object ExecutedFragmentTransactionsHolder {
    val executed: MutableList<String> = LinkedList()
}

fun FragmentManager.onceTransaction(
    tag: String,
    transactionModifier: (FragmentTransaction) -> FragmentTransaction
): Int? {
    if (ExecutedFragmentTransactionsHolder.executed.contains(tag)) return null
    ExecutedFragmentTransactionsHolder.executed.add(tag)
    return transactionModifier(beginTransaction()).commit()
}