package de.johnsoneyo.mapper.decorators

import java.util.*
import kotlin.reflect.KClass


annotation class SourceFieldMapping(val sourceField: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ClassFieldMapping(val fields: Array<SourceFieldMapping> = [])

/**
 * This class is public and available for extension when decorating a field to transform [TransformToType] <INPUT></INPUT> to be mapped to an <OUTPUT>
 * @see IntegerToStringTypeAdapter
 *
 * @see StringToUUIDTypeAdapter
</OUTPUT> */
fun interface TypeAdapter<in INPUT, out OUTPUT> {
    /**
     *
     * @param input source
     * @return destination
     */
    fun convert(input: INPUT): OUTPUT
}


class IntegerToStringTypeAdapter : TypeAdapter<Int, String> {
    override fun convert(input: Int) = input.toString()
}

class StringToUUIDTypeAdapter : TypeAdapter<String, UUID> {
    override fun convert(input: String): UUID = UUID.fromString(input)
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class TransformToType<I, O>(val typeAdapter: KClass<out TypeAdapter<I, O>>)



