package io.github.zhefimenko.kafkastreams.demo.lesson6

import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.state.WindowStore
import java.time.Duration
import java.util.*

/**
 * @author Yauheni Yefimenka
 */

private val log = KotlinLogging.logger {}

fun main() {
    val props = Properties()
        .also {
            it[StreamsConfig.APPLICATION_ID_CONFIG] = "lesson6-group"
            it[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
            it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
            it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
            it[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 5000
        }

    val builder = StreamsBuilder()
        .also {
            it.stream<String, String>("lesson6_source")
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
                .count(Materialized.`as`<String, Long, WindowStore<Bytes, ByteArray>>("lesson6_store"))
                .toStream()
                .map { key, value -> KeyValue.pair(key.key(), value.toString()) }
                .to("lesson6_target")
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
