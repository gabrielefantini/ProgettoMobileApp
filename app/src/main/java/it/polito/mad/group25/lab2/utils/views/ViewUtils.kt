package it.polito.mad.group25.lab2.utils.views

import android.view.View
import it.polito.mad.group25.lab2.utils.persistence.ViewsSerializationGroup

object ViewUtils {
    fun <T : View, S> loadViews(
        viewProvider: (Int) -> T?,
        storage: S?,
        vararg serializationGroups: ViewsSerializationGroup<T, S>
    ): Boolean {
        if (storage == null)
            return false
        var anyUpdate = false
        for (group in serializationGroups)
            for (id in group.idsMapped) {
                val view = viewProvider(id.first)
                if (view != null)
                    if (group.persistor.load(id.second, view, storage))
                        anyUpdate = true
            }
        return anyUpdate
    }

    fun <T : View, S> saveViews(
        viewProvider: (Int) -> T?,
        storage: S,
        vararg serializationGroups: ViewsSerializationGroup<T, S>
    ) {
        for (group in serializationGroups)
            for (id in group.idsMapped) {
                val view = viewProvider(id.first)
                if (view != null)
                    group.persistor.save(id.second, view, storage)
            }
    }
}

