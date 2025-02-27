package de.johnsoneyo.mapper

import de.johnsoneyo.mapper.decorators.ClassFieldMapping
import de.johnsoneyo.mapper.decorators.SourceFieldMapping
import de.johnsoneyo.mapper.decorators.StringToUUIDTypeAdapter
import de.johnsoneyo.mapper.decorators.TransformToType
import de.johnsoneyo.mapper.exceptions.KModelMapperException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Condition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@ExtendWith(MockitoExtension::class)
class UtilityKtTest {


    @Test
    fun map_ShouldNotThrowException_WhenMappingNestedObjects() {
        // given

        val name = "test-name"
        val age = 1
        val sex = "test-sex"
        val expected = Person(
            name, age, sex, java.util.List.of(
                Person.Address(
                    "test-street-1", "test-zipcode-1",
                    Person.Address.ExtraInfo("11000011.000111")
                ),
                Person.Address("test-street-2", "test-zipcode-2", Person.Address.ExtraInfo("test-extra-info"))
            ),
            java.util.Map.of<String, String>("key", "value")
        )

        //when

        val actual: PersonDto = mapEntry(expected, PersonDto::class)

        // then
        assertThat(actual)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", name).hasFieldOrPropertyWithValue("age", age)
            .hasFieldOrPropertyWithValue("sex", sex)

        val addressDtoCondition: Condition<PersonDto.AddressDto> = Condition(
            { addressDto -> addressDto.extraInfo != null && addressDto.extraInfo?.coordinates === "11000011.000111" },
            "extra info is not null"
        )
        assertThat(actual.addresses)
            .isNotEmpty()
            .hasSize(2)
//            .satisfies(({ addressDto -> addressDto
//                assertThat(addressDto)
//                    .hasFieldOrPropertyWithValue("streetName", "test-street-1").has(addressDtoCondition)
//            }), Index.atIndex(0))
    }

    @Test
    fun map_ShouldThrowException_WhenMappingSameFieldOfDifferentJavaType() {
        // given

        val expected = Extra(1L)

        // then
        assertThatThrownBy { modelMapper.map(expected, ExtraDto::class.java) }
            .isInstanceOf(KModelMapperException::class.java)
            .hasMessage("error occurred while mapping entity")
            .hasCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun map_ShouldMapWithoutException_WhenStringToUUIDAdapterUsed() {
        // given

        val requesterId = "a0200f66-f5b2-4cc7-accd-9810f1b1471f"
        val request = Request(requesterId)

        // when
        val requestDto: RequestDto = modelMapper.map(request, RequestDto::class.java)

        // then
        assertThat(requestDto.requesterId).isEqualTo(UUID.fromString(requesterId))
    }

    @Test
    fun map_ShouldMapWithoutException_WhenCustomFieldMapIsUsed() {
        // given

        val requesterId = "a0200f66-f5b2-4cc7-accd-9810f1b1471f"
        val id = "test-id"
        val request = Request(requesterId, id)

        // when
        val requestDto: RequestDto = modelMapper.map(request, RequestDto::class.java)

        // then
        assertThat(requestDto.requesterId).isEqualTo(UUID.fromString(requesterId))
        assertThat(requestDto.identifier).isEqualTo(id)
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
    ) {
        class Address(var streetName: String, var zipCode: String, var extraInfo: ExtraInfo) {
            class ExtraInfo(var coordinates: String)
        }
    }


    class PersonDto {
        var name: String? = null
        var age: Int? = null
        var sex: String? = null
        var addresses: List<AddressDto>? = null
        var attributes: Map<String, String>? = null


        override fun toString(): String {
            return "PersonDto{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", sex='" + sex + '\'' +
                    ", addresses=" + addresses +
                    ", attributes=" + attributes +
                    '}'
        }

        class AddressDto {
            var streetName: String? = null
            var zipCode: String? = null

            var extraInfo: ExtraInfoDto? = null

            override fun toString(): String {
                return "AddressDto{" +
                        "streetName='" + streetName + '\'' +
                        ", zipCode='" + zipCode + '\'' +
                        '}'
            }

            class ExtraInfoDto {
                var coordinates: String? = null
            }
        }
    }


    class Extra(var value: Long)

    class ExtraDto {
        var value: String? = null
    }

    class Request {
        var requesterId: String
        var id: String? = null

        constructor(requesterId: String, id: String?) {
            this.requesterId = requesterId
            this.id = id
        }

        constructor(requesterId: String) {
            this.requesterId = requesterId
        }
    }


    class RequestDto {
        @TransformToType<String, UUID>(typeAdapter = StringToUUIDTypeAdapter::class)
        var requesterId: UUID? = null

        @ClassFieldMapping(fields = [SourceFieldMapping(sourceField = "id")])
        var identifier: String? = null
    }
}