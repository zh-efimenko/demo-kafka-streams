CREATE STREAM high_readings AS
SELECT sensor, reading, location
FROM clean
WHERE reading > 41
    EMIT CHANGES;
