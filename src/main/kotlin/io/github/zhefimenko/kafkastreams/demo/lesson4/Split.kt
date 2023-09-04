package io.github.zhefimenko.kafkastreams.demo.lesson4

import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.Branched
import org.apache.kafka.streams.kstream.Named
import java.util.*

/**
 * @author Yauheni Yefimenka
 */

private val log = KotlinLogging.logger {}

private const val PREFIX = "LESSON4-PREFIX"
private const val TARGET_TOPIC_1 = "lesson4_target_1"
private const val TARGET_TOPIC_5 = "lesson4_target_5"
private const val TARGET_TOPIC_DEFAULT = "lesson4_target_default"

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
            it.stream<String, String>("lesson4_source")

                .split(Named.`as`(PREFIX))
                .branch({ key, _ -> key == "1" }, Branched.`as`(TARGET_TOPIC_1))
                .branch({ key, _ -> key == "5" }, Branched.`as`(TARGET_TOPIC_5))
                .defaultBranch(Branched.`as`(TARGET_TOPIC_DEFAULT))

                .forEach { (name, stream) ->
                    run {
                        val topicName = name.replaceFirst(PREFIX, "")
                        stream.to(topicName)
                    }
                }
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
