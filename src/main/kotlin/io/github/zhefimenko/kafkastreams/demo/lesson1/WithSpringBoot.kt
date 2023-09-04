package io.github.zhefimenko.kafkastreams.demo.lesson1

import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.KStream
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener

/**
 * @author Yauheni Yefimenka
 */
@SpringBootApplication
class WithSpringBoot {

    private val log = KotlinLogging.logger {}

    @Bean
    fun streamsBuilderFactoryBean(meterRegistry: MeterRegistry): StreamsBuilderFactoryBean {
        val props = mutableMapOf<String, Any>(
            StreamsConfig.APPLICATION_ID_CONFIG to "lesson1-group",
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name
        )
        return StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
            .also {
                it.addListener(KafkaStreamsMicrometerListener(meterRegistry))
                it.setStreamsUncaughtExceptionHandler { ex: Throwable ->
                    log.error(ex.message, ex)
                    StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
                }
            }
    }

    @Bean
    fun kStream(streamsBuilder: StreamsBuilder): KStream<String, String> {
        return streamsBuilder.stream<String, String>("lesson1_source")
            .also {
                it.mapValues { value -> value.lowercase() }
                    .to("lesson1_target")
            }
    }
}

fun main(args: Array<String>) {
    runApplication<WithSpringBoot>(*args)
}
