package de.johnsoneyo.mapper

import de.johnsoneyo.mapper.decorators.ClassFieldMapping
import de.johnsoneyo.mapper.decorators.SourceFieldMapping
import de.johnsoneyo.mapper.decorators.StringToUUIDTypeAdapter
import de.johnsoneyo.mapper.decorators.TransformToType
import java.util.*

class PersonDto  (
    var name: String?,
    var age: Int?,
    var sex: String?,
    var addresses: List<AddressDto>?,
    var attributes: Map<String, String>? ) {

    constructor() : this(null, null, null, null, null)
}

class AddressDto (
    var streetName: String?,
    var zipCode: String?,
    var extraInfo: ExtraInfoDto?
) {
    constructor() : this(null, null ,null)
}

class ExtraInfoDto (  var coordinates: String?

) {
    constructor() : this(null)
}

class Extra(var value: Long?) {
    constructor() : this(null)
}

class ExtraDto ( var value: String?) {
    constructor(): this(null)
}

class Request {
    var requesterId: String? = null
    var id: String? = null

    constructor(): this(null)

    constructor(requesterId: String?, id: String?) {
        this.requesterId = requesterId
        this.id = id
    }

    constructor(requesterId: String?) {
        this.requesterId = requesterId
    }
}

class RequestDto {
    @TransformToType<String, UUID>(typeAdapter = StringToUUIDTypeAdapter::class)
    var requesterId: UUID? = null

    @ClassFieldMapping(fields = [SourceFieldMapping(sourceField = "id")])
    var identifier: String? = null
}

/**
 *
 */

class Person(
    var name: String,
    var age: Int,
    var sex: String,
    var addresses: List<Address>,
    var attributes: Map<String, String>
)


class ExtraInfo(var coordinates: String)

class Address(var streetName: String, var zipCode: String, var extraInfo: ExtraInfo)