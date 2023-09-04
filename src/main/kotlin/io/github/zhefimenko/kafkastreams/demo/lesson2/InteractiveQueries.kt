package io.github.zhefimenko.kafkastreams.demo.lesson2

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore

/**
 * @author Yauheni Yefimenka
 */

object InteractiveQueries {
    fun print(streams: KafkaStreams) {
        val keyValueStore = streams.store<ReadOnlyKeyValueStore<String, String>>(
            StoreQueryParameters.fromNameAndType(
                "lesson2-store",
                QueryableStoreTypes.keyValueStore()
            )
        )

        val keyValueIterator = keyValueStore.all()
        while (keyValueIterator.hasNext()) {
            val next = keyValueIterator.next()
            System.err.println("${next.key} : ${next.value}")
        }
    }
}
