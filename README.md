## KModel Mapper 

KModel Mapper is an open source library used to map simple to deep object nesting with similar object data structure without the need of accessors or mutators

## Gradle Dependency
```groovy
 implementation "de.johnsoneyo:kmodel-mapper:1.0"
```

### Assumptions
- Mapping of Data fields between two objects that have homogeneous fields and data type happens out of the box with zero configuration
- Custom field mapping is targeted at intentional naming or chosen by the user or client
- Destination classes
    - Has a non argument constructor and doesn't require immutable creation, required for no argument class object construction.
    - Has getter fields for returning the values atleast ( no mandatory )

### How to use 

#### With Homogeneous Class
```kotlin

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

```


## With Adapter Classes 
```kotlin
      // given

        val requesterId = "a0200f66-f5b2-4cc7-accd-9810f1b1471f"
        val request = Request(requesterId)

        // when
        val requestDto: RequestDto = mapEntry(request, RequestDto::class)

        // then
        assertThat(requestDto.requesterId).isEqualTo(UUID.fromString(requesterId))


```