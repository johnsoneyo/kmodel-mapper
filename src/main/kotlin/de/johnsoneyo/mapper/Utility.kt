package de.johnsoneyo.mapper

import de.johnsoneyo.mapper.decorators.ClassFieldMapping
import de.johnsoneyo.mapper.decorators.SourceFieldMapping
import de.johnsoneyo.mapper.decorators.TransformToType
import de.johnsoneyo.mapper.decorators.TypeAdapter
import de.johnsoneyo.mapper.exceptions.KModelMapperException
import org.slf4j.LoggerFactory
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


const val GEN_ERROR_MESSAGE = "error occurred while mapping entity"
var LOG: org.slf4j.Logger = LoggerFactory.getLogger("Utility")

/**
 * @param object      source object to be converted
 * @param outputClass destination class instance to map object
 *
 *
 * It is mandatory that the class has a no arg constructor, setters and getters are not necessary
 * as it uses reflection to set the fields
 *
 * @param <INPUT></INPUT>     source input param
 * @param <OUTPUT>    destination output param
 * @return mapped object
 * @throws KModelMapperException when matching fields are not of same data type
 * @see KModelMapperException.getCause
</OUTPUT> */
fun  camapEntry(obj: Any, outputClass: KClass<Any>) :Any {

    try {
        val output : Any = outputClass::class.createInstance()
        map(obj, output)
        return output
    } catch (throwable: Throwable) {
        throw KModelMapperException(GEN_ERROR_MESSAGE, throwable.cause)
    }
}


/**
 * @param obj
 * @param output
 */
fun  map(obj: Any, output: Any) {
    try {
        // break chain when object is null
        if (obj == null) {
            return
        }

        // break chain when class is a java runtime class
        if (obj.javaClass.packageName.startsWith("java")) {
            return
        }

        val fields: Array<Field> = obj.javaClass.declaredFields
        if (fields.isEmpty()) {
            // break chain if there are no more fields in the class
            return
        }

        // iterate through fields in the bean class
        for (field in fields) {
            val obj = field[obj]

            // check if field is a collection type
            if (obj is Collection<*>) {
                val collection = obj
                for (o in collection) {
                    if (o == null) {
                        continue
                    }
                    if (obj.javaClass.packageName.startsWith("java")) {
                        continue
                    }

                    val folds: Array<Field> = obj.javaClass.declaredFields
                    if (folds.isEmpty()) {
                        continue
                    }

                    val outputField = getField(output, field.name)

                    var oput_: Any? = null
                    if (outputField != null && outputField.type == MutableList::class.java) {
                        val collectionType = outputField.genericType as ParameterizedType

                        oput_ = Class.forName(collectionType.actualTypeArguments[0].typeName).getDeclaredConstructor()
                            .newInstance()
                        val clctn = outputField[output] as MutableCollection<Any>

                        clctn.add(oput_)
                    }

                    if (oput_ == null) continue
                    map(o, oput_)
                }
            } else {
                val outputField = getField(output, field.name)

                if (outputField == null && obj != null) {
                    if (obj.javaClass.packageName.startsWith("java")) {
                        for (destinationField in output.javaClass.getDeclaredFields()) {
                            val annotations = destinationField.declaredAnnotations

                            if (isNotEmpty<Annotation>(*annotations)) {
                                for (annotation in annotations) {
                                    if (annotation is ClassFieldMapping) {
                                        val classFieldMapping: ClassFieldMapping = annotation
                                        val sourceFieldMapping: Array<SourceFieldMapping> = classFieldMapping.fields
                                        for (sfm in sourceFieldMapping) {
                                            if (sfm.sourceField == field.name) {
                                                destinationField[output] = obj
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                var customJavaObject: Any? = null
                if (outputField != null) {
                    outputField.isAccessible = true
                    if (obj!!.javaClass.packageName.startsWith("java")) {
                        val updatedObj = updateField(outputField, obj)
                        // sets a java field in the object
                        outputField[output] = updatedObj
                    } else {
                        customJavaObject = outputField.type.getDeclaredConstructor().newInstance()
                        // set a bean field in the object
                        outputField[output] = customJavaObject
                    }
                }

                // send a custom java object for further division
                if (customJavaObject != null) {
                    map(obj, customJavaObject)
                } else  // end iteration of jre object
                    map(obj, output)
            }
        }
    } catch (exception: Exception) {
        LOG.error("error occurred mapping entity", exception)
        throw KModelMapperException(GEN_ERROR_MESSAGE, exception)
    }
}

fun <T> isNotEmpty(vararg t: T): Boolean {
    return t.size > 0
}

private fun updateField(outputField: Field, obj: Any?): Any? {
    if (outputField.type == obj!!.javaClass) return obj

    val annotations = outputField.declaredAnnotations

    if (isNotEmpty<Annotation>(*annotations)) {
        for (annot in annotations) {
            if (annot is TransformToType<*, *>) {

                val transformToType: KClass<out TypeAdapter<*,*>> = annot.typeAdapter
                val convertMethod = transformToType.declaredMembers.first()

                try {
                    val typeAdapterInstance: Any = transformToType.createInstance()
                    convertMethod.isAccessible = true
                    return convertMethod.call(typeAdapterInstance, obj)
                } catch (e: InstantiationException) {
                    LOG.error("updateField: error occurred in updating transforming field object", e)
                    throw KModelMapperException("error occurred updating object in type adapter", e)
                } catch (e: IllegalAccessException) {
                    LOG.error("updateField: error occurred in updating transforming field object", e)
                    throw KModelMapperException("error occurred updating object in type adapter", e)
                } catch (e: InvocationTargetException) {
                    LOG.error("updateField: error occurred in updating transforming field object", e)
                    throw KModelMapperException("error occurred updating object in type adapter", e)
                } catch (e: NoSuchMethodException) {
                    LOG.error("updateField: error occurred in updating transforming field object", e)
                    throw KModelMapperException("error occurred updating object in type adapter", e)
                }
            }
        }
    }

    return obj
}


private fun  getField(obj: Any, fieldName: String): Field? {
    try {
        return obj.javaClass.getDeclaredField(fieldName)
    } catch (e: NoSuchFieldException) {
        LOG.warn("field {} not found", fieldName)
        return null
    }
}


/**
 * Defines a class collection instance in the factory method
 */
object ImmutableCollectionFactory {
    /**
     * @param <T>
     * @return lazy initialization of new immutable collection to be created
    </T> */
    fun <T> collectionFactory(): Map<Class<*>, Supplier<MutableCollection<T?>>> {
        return java.util.Map.of(
            MutableList::class.java,
            Supplier { ArrayList() },
            MutableSet::class.java,
            Supplier { HashSet() },
            LinkedList::class.java,
            Supplier { LinkedList() })
    }
}
