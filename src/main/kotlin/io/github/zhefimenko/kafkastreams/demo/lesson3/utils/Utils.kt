package io.github.zhefimenko.kafkastreams.demo.lesson3.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

/**
 * @author Yauheni Yefimenka
 */

class JsonSerializer<T>(
    private val serializer: KSerializer<T>
) : Serializer<T> {

    override fun serialize(topic: String, data: T): ByteArray = Json.encodeToString(serializer, data).toByteArray()
}

class JsonDeserializer<T>(
    private val serializer: KSerializer<T>
) : Deserializer<T> {

    override fun deserialize(topic: String, bytes: ByteArray): T = Json.decodeFromString(serializer, String(bytes))
}
