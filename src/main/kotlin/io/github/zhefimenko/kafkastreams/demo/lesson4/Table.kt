package io.github.zhefimenko.kafkastreams.demo.lesson4

import io.github.zhefimenko.kafkastreams.demo.lesson3.User
import io.github.zhefimenko.kafkastreams.demo.lesson3.utils.JsonDeserializer
import io.github.zhefimenko.kafkastreams.demo.lesson3.utils.JsonSerializer
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.ValueJoiner
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import java.util.*

/**
 * @author Yauheni Yefimenka
 */

private val log = KotlinLogging.logger {}

@Serializable
data class FullUser(val id: String, val name: String, val order: String)

fun main() {
    val props = Properties()
        .also {
            it[StreamsConfig.APPLICATION_ID_CONFIG] = "lesson4-table-group"
            it[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
            it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
            it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
            it[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 5000
            it[StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG] = 10 * 1024 * 1024L
        }

    val store = Materialized.`as`<String, User, KeyValueStore<Bytes, ByteArray>>("lesson4-users")
    val userSerde = Serdes.serdeFrom(JsonSerializer(User.serializer()), JsonDeserializer(User.serializer()))
    val fullUserSerde = Serdes.serdeFrom(JsonSerializer(FullUser.serializer()), JsonDeserializer(FullUser.serializer()))

    val builder = StreamsBuilder()
        .also {
            val orderStream = it.stream<String, String>("lesson4_orders_source")
            val userTable = it.table("lesson4_users_source", Consumed.with(Serdes.String(), userSerde), store)
            val userJoiner = ValueJoiner<String, User, FullUser> { order, user -> FullUser(user.id, user.name, order) }

            orderStream
                .join(
                    userTable,
                    userJoiner
                )
                .to("lesson4_users_target", Produced.valueSerde(fullUserSerde))
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
                val keyValueStore = streams.store<ReadOnlyKeyValueStore<String, String>>(
                    StoreQueryParameters.fromNameAndType(
                        "lesson4-users",
                        QueryableStoreTypes.keyValueStore()
                    )
                )

                val keyValueIterator = keyValueStore.all()
                while (keyValueIterator.hasNext()) {
                    val next = keyValueIterator.next()
                    System.err.println("${next.key} : ${next.value}")
                }
                println()
            } catch (_: Exception) {
            }
        }
    }

    timer.scheduleAtFixedRate(task, 0, 10_000)
}
