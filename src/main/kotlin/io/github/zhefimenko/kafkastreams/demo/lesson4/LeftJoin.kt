package io.github.zhefimenko.kafkastreams.demo.lesson4

import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.kstream.ValueJoiner
import java.time.Duration
import java.util.*

/**
 * @author Yauheni Yefimenka
 */

private val log = KotlinLogging.logger {}

fun main() {
    val props = Properties()
            .also {
                it[StreamsConfig.APPLICATION_ID_CONFIG] = "lesson4-group"
                it[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
                it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
                it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
            }

    val builder = StreamsBuilder()
            .also {
                val leftStream = it.stream<String, String>("lesson4_left_source")
                val rightStream = it.stream<String, String>("lesson4_right_source")
                val valueJoiner = ValueJoiner<String, String?, String> { leftValue, rightValue -> leftValue + rightValue }

                leftStream
                        .leftJoin(
                                rightStream,
                                valueJoiner,
                                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(10))
                        )
                        .peek { key, value -> System.err.println("key:$key, value:$value") }
                        .mapValues { value -> value.lowercase() }
                        .to("lesson4_target")
            }

    val streams = KafkaStreams(builder.build(), props).also {
        it.setUncaughtExceptionHandler { ex: Throwable ->
            log.error(ex.message, ex)
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
        }
        it.start()
    }

    // Add shutdown hook to stop the Kafka Streams threads.
    // You can optionally provide a timeout to `close`.
    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}
