package io.github.zhefimenko.kafkastreams.demo.lesson1

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

/**
 * @author Yauheni Yefimenka
 */
@ContextConfiguration(classes = [WithSpringBoot::class, WithSpringBootTest.Config::class])
@ExtendWith(SpringExtension::class)
class WithSpringBootTest {

    @TestConfiguration
    class Config {
        @MockBean
        private lateinit var streamsBuilderFactoryBean: StreamsBuilderFactoryBean

        @Bean
        fun streamsBuilder(): StreamsBuilder = StreamsBuilder()
    }

    @Autowired
    private lateinit var streamsBuilder: StreamsBuilder

    private lateinit var testDriver: TopologyTestDriver
    private lateinit var inTopic: TestInputTopic<String, String>
    private lateinit var outTopic: TestOutputTopic<String, String>

    @BeforeEach
    fun beforeEach() {
        val props = Properties()
        props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
        props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
        testDriver = TopologyTestDriver(streamsBuilder.build(), props)

        inTopic = testDriver.createInputTopic("lesson1_source", StringSerializer(), StringSerializer())
        outTopic = testDriver.createOutputTopic("lesson1_target", StringDeserializer(), StringDeserializer())
    }

    @AfterEach
    fun afterEach() {
        testDriver.close()
    }

    @Test
    fun kStreamTest() {
        inTopic.pipeInput("1", "VALUE")

        val readRecord = outTopic.readRecord()

        Assertions.assertEquals("1", readRecord.key)
        Assertions.assertEquals("value", readRecord.value)
    }
}
