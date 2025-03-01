package de.johnsoneyo.mapper.exceptions

class KModelMapperException
/**
 *
 * @param message
 * @param cause
 */
    (message: String?, cause: Throwable?) : RuntimeException(message, cause) {


        constructor(cause: Throwable?) : this(null, cause)
    }
