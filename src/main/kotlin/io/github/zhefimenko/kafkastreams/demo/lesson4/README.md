## Join

## Commands for producer

### lesson4_left_source
```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson4_left_source
```

### lesson4_right_source
```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson4_right_source
```

## Commands for consumer

```bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --property "print.key=true" --topic lesson4_target
```

## Table

### lesson4_orders_source
```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson4_orders_source
```

### lesson4_users_source
```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson4_users_source
```

## Commands for consumer

```bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --property "print.key=true" --topic lesson4_users_target
```

## Jsons

```json
{"id":"1","name":"Zhenya"}
```
```json
{"id":"2","name":"Nastya"}
```
```json
{"id":"3","name":"Alex"}
```


## Java settings

```bash
java -XshowSettings
```

## Additional properties
```java
Properties props = new Properties();
// Enable record cache of size 10 MB.
props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 10 * 1024 * 1024L);
// Set commit interval to 1 second.
props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
// The number of standby replicas. Standby replicas are shadow copies of local state stores.
props.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 1);
```
