package it.polito.mad.group25.lab2.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

fun Field.isGenericType(): Boolean = type.typeParameters.isNotEmpty()

@RequiresApi(Build.VERSION_CODES.O)
@Suppress("UNCHECKED_CAST")
fun <T> Class<T>.tryInstantiate(vararg args: Any?): T {
    val destinationConstructors = constructors
    val emptyConstructor =
        destinationConstructors.filter { it.parameterCount == 0 }.map { it.isAccessible = true; it }

    if (emptyConstructor.isNotEmpty()) return emptyConstructor[0].newInstance() as T

    val childConstructor = destinationConstructors.filter {
        it.parameterCount == args.size && it.parameterTypes.contentEquals(args.map { a -> a?.javaClass }
            .toTypedArray())
    }.map { it.isAccessible = true; it }

    if (childConstructor.isNotEmpty()) return childConstructor[0].newInstance(*args) as T

    throw NoSuchMethodException()
}

@RequiresApi(Build.VERSION_CODES.O)
fun <T : Any> KClass<T>.tryInstantiate(vararg args: Any?): T = java.tryInstantiate(*args)

fun <T : Iterable<Annotation>> T.containsAnnotation(annotationClass: KClass<out Annotation>): Boolean =
    any { annotationClass.java.isAssignableFrom(it::class.java) }

@Suppress("UNCHECKED_CAST")
fun <A : Annotation, T : Iterable<Annotation>> T.getAnnotation(annotationClass: KClass<A>): A? =
    find { annotationClass.java.isAssignableFrom(it::class.java) } as A?

fun KParameter.parameterType() = type.classifier as KClass<*>


fun <T : Any> Class<T>.distanceFrom(sourceClass: Class<*>): Int? {
    if (this == sourceClass) return 0
    var actualParent = sourceClass.superclass
    var i = 1
    while (actualParent != Any::class.java) {
        if (actualParent == this) return i
        i++
        actualParent = actualParent.superclass
    }
    return if (this == Any::class) i else null
}
fun <T : Any> KClass<T>.distanceFrom(sourceClass: KClass<*>): Int? = java.distanceFrom(sourceClass.java)
