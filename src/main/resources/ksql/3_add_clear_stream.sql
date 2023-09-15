CREATE STREAM clean AS
SELECT sensor,
       reading,
       UCASE(location) AS location
FROM readings
         EMIT CHANGES;
