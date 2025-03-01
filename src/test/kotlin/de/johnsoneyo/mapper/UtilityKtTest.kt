package de.johnsoneyo.mapper

import de.johnsoneyo.mapper.exceptions.KModelMapperException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Condition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
            name, age, sex, listOf(
                Address(
                    "test-street-1", "test-zipcode-1",
                    ExtraInfo("11000011.000111")
                ),
                Address("test-street-2", "test-zipcode-2", ExtraInfo("test-extra-info"))
            ),
            mapOf("key" to "value")
        )


        //when
        val actual: PersonDto = mapEntry(expected, PersonDto::class)

        // then
        assertThat(actual)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", name).hasFieldOrPropertyWithValue("age", age)
            .hasFieldOrPropertyWithValue("sex", sex)

        val addressDtoCondition: Condition<AddressDto> = Condition(
            { addressDto -> addressDto.extraInfo != null && addressDto.extraInfo?.coordinates === "11000011.000111" },
            "extra info is not null"
        )
        assertThat(actual.addresses)
            .isNotEmpty()
            .hasSize(2)
            .extracting("streetName")
            .contains("test-street-1", "test-street-2")

    }

    @Test
    fun map_ShouldThrowException_WhenMappingSameFieldOfDifferentJavaType() {
        // given

        val expected = Extra(1L)

        // then
        assertThatThrownBy { mapEntry(expected, ExtraDto::class) }
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
        val requestDto: RequestDto = mapEntry(request, RequestDto::class)

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
        val requestDto: RequestDto = mapEntry(request, RequestDto::class)

        // then
        assertThat(requestDto.requesterId).isEqualTo(UUID.fromString(requesterId))
        assertThat(requestDto.identifier).isEqualTo(id)
    }


}