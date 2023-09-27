#!/bin/bash

echo "Waiting for Kafka to start..."
while ! nc -z kafka 29092; do
  sleep 1
done
echo "Kafka is running, creating topics...!"

topics=(
  lesson1_source
  lesson1_target
  lesson1_target_ksql

  lesson2_source

  lesson3_source
  lesson3_target

  lesson4_split_source
  lesson4_left_source
  lesson4_right_source
  lesson4_target

  lesson4_orders_source
  lesson4_users_target

  lesson5_source
  lesson5_target

  lesson6_source
  lesson6_target
)

for topic in "${topics[@]}"; do
  kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic "$topic"
done

kafka-topics --bootstrap-server kafka:29092 --create --partitions 3 --if-not-exists --topic lesson4_users_source

echo "All topics were successfully created!"
