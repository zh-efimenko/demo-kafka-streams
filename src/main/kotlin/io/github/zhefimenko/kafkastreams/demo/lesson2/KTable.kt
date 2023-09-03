package io.github.zhefimenko.kafkastreams.demo.lesson2

import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import java.util.*

/**
 * @author Yauheni Yefimenka
 */

private val log = KotlinLogging.logger {}

fun main() {
    val props = Properties()
            .also {
                it[StreamsConfig.APPLICATION_ID_CONFIG] = "lesson2-group"
                it[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
                it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
                it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
            }

    val store = Materialized.`as`<String, String?, KeyValueStore<Bytes, ByteArray>>("lesson2-store")
//            .withCachingDisabled()

    val builder = StreamsBuilder()
            .also {
                it.table("lesson2_source", store)
                        .filter { _, value -> value.length <= 5 }
                        .mapValues { value -> value.lowercase() }
                        .toStream()
                        .peek { key, value -> System.err.println("key:$key, value:$value") }
                        .to("lesson2_target")
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
