package it.polito.mad.group25.lab.utils.persistence.impl.firestore

import it.polito.mad.group25.lab.utils.cache.CacheElement
import it.polito.mad.group25.lab.utils.cache.ConcurrentCache
import org.apache.commons.jcs.engine.CompositeCacheAttributes
import org.apache.commons.jcs.engine.ElementAttributes

// Una cache per salvare gli id degli oggetti che sono appena stati inseriti nel db
// in modo da evitarli quando arriva la notifica del logo aggiornamento al listener.
// Si perderanno eventuali altre modifiche all'oggetto in quel range di tempo però è
// un buon compromesso.
class FirestoreJustInsertedCache(id: String) :
    ConcurrentCache<String, Byte>(CompositeCacheAttributes().apply {
        cacheName = FirestoreLiveMapPersistorDelegate::class.java.simpleName + id
        isUseDisk = false; isUseLateral = false; isUseRemote = false
    }) {
    fun add(key: String) {
        super.set(
            key,
            CacheElement(0,
                ElementAttributes().apply { isEternal = false; maxLife = 10 })
        )
    }

    fun contains(key: String) = get(key) != null
}