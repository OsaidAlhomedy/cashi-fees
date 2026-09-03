# Cashi Fees Service

RESTful fees workflow. Kotlin + Spring Boot + Restate.

Note : I interpreted the charge operation as applying and recording the state change.
I didn't implement an actual wallet debit because the API contract doesn't expose a wallet or account to debit

## Run

```bash
docker compose up --build
```

then call the api with a transaction body

```bash
curl -X POST http://localhost:8080/transaction/fee \
  -H 'content-type: application/json' \
  -d '{
    "transaction_id": "txn_001",
    "amount": 1000,
    "asset": "USD",
    "asset_type": "FIAT",
    "type": "Mobile Top Up",
    "state": "SETTLED - PENDING FEE",
    "created_at": "2023-08-30 15:42:17.610059"
  }'
```

You can also use OpenAPI Swagger http://localhost:8080/swagger-ui/index.html

the body is already populated with default values

Restate admin UI is available on http://localhost:9070

### Configurations

You can add more transaction types rules in the **application.yml** file

for the sake of the demo I kept it like this, in a real production system I think the better approach
is to define them in the database and cache them
and maybe a CDC update event would invalidate the cache.

```yml
cashi:
  fees:
    rules:
      "[Mobile Top Up]":
        type: PERCENTAGE
        rate: 0.0015
        description: "Standard fee rate of 0.15%"

      "[Bill Payment]":
        type: PERCENTAGE
        rate: 0.0020
        description: "Standard fee rate of 0.20%"
```

## Tests

    ./gradlew test

## Open Questions
1- are amounts represented as minor or major units ?

2- can a transaction amount be zero ?? in other words do I need to validate it my assumption is yes

3- what do I mean by charging step ??

## Resources

[Bealdung for Kotlin](https://www.baeldung.com/kotlin/)

[Restate Docs](https://restate.dev/)