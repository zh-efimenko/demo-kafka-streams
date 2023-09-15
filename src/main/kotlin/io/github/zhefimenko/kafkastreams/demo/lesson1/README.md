## Kafka Streams

### Commands for producer

```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson1_source
```

### Commands for consumer

```bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --property "print.key=true" --topic lesson1_target
```

## KsqlDB

### Commands for producer

```bash
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --property "parse.key=true" --property "key.separator=:" --topic lesson1_source
```

### Commands for consumer

```bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --property "print.key=true" --topic lesson1_target_ksql
```

### CLI

```bash
docker exec -it ksqldb-cli ksql http://ksqldb-server:8088
```

### Commands of KsqlDB

```bash
SHOW streams;
SHOW PROPERTIES;
DROP STREAM {name_stream}
```

### Java client

```java

String sql = "CREATE TABLE ORDERS_BY_USER AS "
               + "SELECT USER_ID, COUNT(*) as COUNT "
               + "FROM ORDERS GROUP BY USER_ID EMIT CHANGES;";
Map<String, Object> properties = Collections.singletonMap("auto.offset.reset", "earliest");
ExecuteStatementResult result = client.executeStatement(sql, properties).get();
System.out.println("Query ID: " + result.queryId().orElse("<null>"));
```
