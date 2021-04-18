package it.polito.mad.group25.lab.utils.persistence

/**
 * A function which takes the instance to serialize,
 * the id to be used to serialize and the storage where to save. It has the duty to serialize
 * on the given storage, with the given id, the given instance.
 */
@FunctionalInterface
interface SerializationStrategy<T, S> {
    fun serialize(instance: T, id: String, storage: S)
    operator fun invoke(instance: T, id: String, storage: S) = serialize(instance, id, storage)
}

/**
 * A function which takes
 * the id to be used to deserialize and the storage from which the data has to be load.
 * It has the duty to deserialize from the given storage, with the given id, an instance of the given type.
 */
@FunctionalInterface
interface DeserializationStrategy<T, S> {
    fun deserialize(id: String, storage: S): T?
    operator fun invoke(id: String, storage: S): T? = deserialize(id, storage)
}

interface SerdeStrategy<T, S> : SerializationStrategy<T, S>, DeserializationStrategy<T, S>

/**
 * Serialization and deserialization strategy which has different storage target (param ST)
 * and load target (param LT)
 */
interface DifferentiatedSerdeStrategy<T, LT, ST> : SerializationStrategy<T, ST>,
    DeserializationStrategy<T, LT>