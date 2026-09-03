# Cashi Fees Service

RESTful fees workflow. Kotlin + Spring Boot + Restate.

## Run

TODO

## Tests

    ./gradlew test

## Open Questions
1- are amounts represented as minor or major units ?
2- can a transaction amount be zero ??
3- why do restate need the classes to be srializble (BidDecimal has no serializer, it needs custom)

## Resources

[Bealdung for Kotlin](https://www.baeldung.com/kotlin/)

[Restate Docs](https://restate.dev/)