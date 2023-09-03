## Commands for producer

```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson1_source
```

## Commands for consumer

```bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --property "print.key=true" --topic lesson1_target
```
