## KModel Mapper 

KModel Mapper is an open source library used to map simple to deep object nesting with similar object data structure without the need of accessors or mutators

## Gradle Dependency
```xml

```

### Assumptions
- Mapping of Data fields between two objects that have homogeneous fields and data type happens out of the box with zero configuration
- Custom field mapping is targeted at intentional naming or chosen by the user or client
- Destination classes
    - Has a non argument constructor and doesn't require immutable creation, required for no argument class object construction.
    - Has getter fields for returning the values atleast ( no mandatory )

### Limitations
- 