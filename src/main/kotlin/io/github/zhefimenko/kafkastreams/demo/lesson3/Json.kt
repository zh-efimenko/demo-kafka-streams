package io.github.zhefimenko.kafkastreams.demo.lesson3

import io.github.zhefimenko.kafkastreams.demo.lesson3.utils.JsonDeserializer
import io.github.zhefimenko.kafkastreams.demo.lesson3.utils.JsonSerializer
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import java.util.*

/**
 * @author Yauheni Yefimenka
 */

private val log = KotlinLogging.logger {}

@Serializable
data class Value(val id: String, val name: String)

fun main() {
    val props = Properties()
            .also {
                it[StreamsConfig.APPLICATION_ID_CONFIG] = "lesson3-group"
                it[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
                it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
                it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
            }

    val valueSerde = Serdes.serdeFrom(JsonSerializer(Value.serializer()), JsonDeserializer(Value.serializer()))

    val builder = StreamsBuilder()
            .also {
                it.stream("lesson3_source", Consumed.with(Serdes.String(), valueSerde))
                        .peek { key, value -> System.err.println("key:$key, value:$value") }
                        .mapValues { value -> value.copy(name = value.name.uppercase()) }
                        .to("lesson3_target", Produced.valueSerde(valueSerde))
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