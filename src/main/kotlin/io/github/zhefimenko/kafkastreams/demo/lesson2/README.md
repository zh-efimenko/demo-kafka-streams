## Commands for producer

```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson2_source
```

## Commands for consumer

```bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --property "print.key=true" --topic lesson2_target
```

## Java settings

```bash
java -XshowSettings
```

## Additional properties
```java
Properties props = new Properties();
// Enable record cache of size 10 MB.
props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
// Set commit interval to 1 second.
props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
// The number of standby replicas. Standby replicas are shadow copies of local state stores.
props.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 1);
```
