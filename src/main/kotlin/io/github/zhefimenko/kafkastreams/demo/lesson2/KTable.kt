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
            it[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 5000
            it[StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG] = 10 * 1024 * 1024L
        }

    val store = Materialized.`as`<String, String?, KeyValueStore<Bytes, ByteArray>>("lesson2-store")
//            .withCachingDisabled()

    val builder = StreamsBuilder()
        .also {
            it.stream<String, String>("lesson2_source")
                .filter { _, value -> value.length <= 5 }
                .mapValues { value -> value.lowercase() }
                .toTable(store)
                .toStream()
                .to("lesson2_target")
        }

    val streams = KafkaStreams(builder.build(), props).also {
        it.setUncaughtExceptionHandler { ex: Throwable ->
            log.error(ex.message, ex)
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
        }
        it.start()
    }

    printTable(streams)

    // Add shutdown hook to stop the Kafka Streams threads.
    // You can optionally provide a timeout to `close`.
    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}

private fun printTable(streams: KafkaStreams) {
    val timer = Timer()

    val task = object : TimerTask() {
        override fun run() {
            try {
                InteractiveQueries.print(streams)
                println()
            } catch (_: Exception) {
            }
        }
    }

    timer.scheduleAtFixedRate(task, 0, 10_000)
}
