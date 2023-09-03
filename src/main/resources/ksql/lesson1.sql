CREATE STREAM lesson1_source (
    key STRING KEY,
    value STRING
) WITH (
    kafka_topic='lesson1_source',
    value_format='DELIMITED',
    partitions=4
);

CREATE STREAM lesson1_target WITH (kafka_topic = 'lesson1_target_ksql') AS
SELECT key, LCASE(value) as `value`
FROM lesson1_source EMIT CHANGES;
