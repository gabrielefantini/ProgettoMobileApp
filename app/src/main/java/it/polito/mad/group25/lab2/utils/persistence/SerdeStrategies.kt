package it.polito.mad.group25.lab2.utils.persistence

import kotlin.reflect.KClass

/**
 * A function which takes the instance to serialize,
 * the id to be used to serialize and the storage where to save. It has the duty to serialize
 * on the given storage, with the given id, the given instance.
 */
@FunctionalInterface
interface SerializationStrategy<T : Any, S> {
    fun serialize(instance: T, id: String, storage: S)
    operator fun invoke(instance: T, id: String, storage: S) = serialize(instance, id, storage)
    fun serializedType(): KClass<T>
    fun <Q> canHandle(typo: Class<out Q>) = serializedType().java.isAssignableFrom(typo)
}

/**
 * A function which takes
 * the id to be used to deserialize and the storage from which the data has to be load.
 * It has the duty to deserialize from the given storage, with the given id, an instance of the given type.
 */
@FunctionalInterface
interface DeserializationStrategy<T : Any, S> {
    fun deserialize(id: String, storage: S): T?
    operator fun invoke(id: String, storage: S): T? = deserialize(id, storage)
    fun deserializedType(): KClass<T>
    fun <Q> canProvide(typo: Class<out Q>) = typo.isAssignableFrom(deserializedType().java)
}

interface SerdeStrategy<T : Any, S> : SerializationStrategy<T, S>, DeserializationStrategy<T, S>

/**
 * Serialization and deserialization strategy which has different storage target (param ST)
 * and load target (param LT)
 */
interface DifferentiatedSerdeStrategy<T : Any, LT, ST> : SerializationStrategy<T, ST>,
    DeserializationStrategy<T, LT>