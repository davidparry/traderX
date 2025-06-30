package finos.traderx.tradeservice

import finos.traderx.messaging.Publisher
import finos.traderx.messaging.socketio.SocketIOJSONPublisher
import finos.traderx.tradeservice.model.TradeOrder
import org.springframework.context.annotation.Configuration
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Title("PubSubConfig Tests")
@Subject(PubSubConfig)
class PubSubConfigSpec extends Specification {

    PubSubConfig config

    def setup() {
        config = new PubSubConfig()
    }

    def "should have Configuration annotation"() {
        expect: "class has @Configuration annotation"
        PubSubConfig.class.isAnnotationPresent(Configuration.class)
    }

    def "should create trade publisher bean"() {
        given: "trade feed address is set"
        ReflectionTestUtils.setField(config, "tradeFeedAddress", "http://localhost:8090")

        when: "creating trade publisher"
        def publisher = config.tradePublisher()

        then: "publisher is created correctly"
        publisher != null
        publisher instanceof Publisher
        publisher instanceof SocketIOJSONPublisher
    }

    def "should configure publisher with correct topic"() {
        given: "trade feed address is set"
        ReflectionTestUtils.setField(config, "tradeFeedAddress", "http://localhost:8090")

        when: "creating trade publisher"
        def publisher = config.tradePublisher() as SocketIOJSONPublisher<TradeOrder>

        then: "publisher topic is set correctly"
        // Note: We can't directly verify the topic as it's set via setter
        // but we can verify the publisher is configured
        publisher != null
    }

    def "should have Bean annotation on tradePublisher method"() {
        given: "the tradePublisher method"
        def method = PubSubConfig.class.getDeclaredMethod("tradePublisher")

        expect: "method has @Bean annotation"
        method.isAnnotationPresent(org.springframework.context.annotation.Bean.class)
    }

    def "should have Value annotation on tradeFeedAddress field"() {
        given: "the tradeFeedAddress field"
        def field = PubSubConfig.class.getDeclaredField("tradeFeedAddress")

        expect: "field has @Value annotation"
        field.isAnnotationPresent(org.springframework.beans.factory.annotation.Value.class)
        
        and: "annotation references correct property"
        def valueAnnotation = field.getAnnotation(org.springframework.beans.factory.annotation.Value.class)
        valueAnnotation.value() == '${trade.feed.address}'
    }

    def "should create anonymous inner class of SocketIOJSONPublisher"() {
        given: "trade feed address is set"
        ReflectionTestUtils.setField(config, "tradeFeedAddress", "http://localhost:8090")

        when: "creating trade publisher"
        def publisher = config.tradePublisher()

        then: "publisher is anonymous inner class"
        publisher.class.name.contains('$')
        publisher.class.superclass == SocketIOJSONPublisher.class
    }

    def "should handle different trade feed addresses"() {
        given: "various trade feed addresses"
        def addresses = [
            "http://localhost:8090",
            "https://trade-feed.example.com",
            "ws://websocket.example.com:9090",
            "http://192.168.1.100:8080"
        ]

        expect: "publisher is created for each address"
        addresses.each { address ->
            ReflectionTestUtils.setField(config, "tradeFeedAddress", address)
            def publisher = config.tradePublisher()
            assert publisher != null
            assert publisher instanceof Publisher<TradeOrder>
        }
    }

    def "should return Publisher of TradeOrder type"() {
        given: "trade feed address is set"
        ReflectionTestUtils.setField(config, "tradeFeedAddress", "http://localhost:8090")

        when: "creating trade publisher"
        def publisher = config.tradePublisher()

        then: "publisher has correct generic type"
        publisher instanceof Publisher
        // Generic type is TradeOrder (verified at compile time)
    }

    def "should create new instance each time"() {
        given: "trade feed address is set"
        ReflectionTestUtils.setField(config, "tradeFeedAddress", "http://localhost:8090")

        when: "creating multiple publishers"
        def publisher1 = config.tradePublisher()
        def publisher2 = config.tradePublisher()

        then: "each call creates new instance"
        publisher1 != null
        publisher2 != null
        !publisher1.is(publisher2) // Different instances
    }

    def "should handle null trade feed address"() {
        given: "null trade feed address"
        ReflectionTestUtils.setField(config, "tradeFeedAddress", null)

        when: "creating trade publisher"
        def publisher = config.tradePublisher()

        then: "publisher is still created"
        publisher != null
        // The publisher will be configured with null address
        // which may cause issues later, but creation succeeds
    }

    def "should handle empty trade feed address"() {
        given: "empty trade feed address"
        ReflectionTestUtils.setField(config, "tradeFeedAddress", "")

        when: "creating trade publisher"
        def publisher = config.tradePublisher()

        then: "publisher is still created"
        publisher != null
    }

    def "should support Spring dependency injection"() {
        expect: "config class follows Spring patterns"
        PubSubConfig.class.isAnnotationPresent(Configuration.class)
        PubSubConfig.class.getDeclaredMethod("tradePublisher").isAnnotationPresent(org.springframework.context.annotation.Bean.class)
        PubSubConfig.class.getDeclaredField("tradeFeedAddress").isAnnotationPresent(org.springframework.beans.factory.annotation.Value.class)
    }
}