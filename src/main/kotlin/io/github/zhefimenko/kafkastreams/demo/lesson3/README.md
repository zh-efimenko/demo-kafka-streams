## Commands for producer

```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson3_source
```

## Commands for consumer

```bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --property "print.key=true" --topic lesson3_target
```

## Jsons

```json
{"id":"1","name":"Zhenya"}
```
```json
{"id":"2","name":"Nastya"}
```
