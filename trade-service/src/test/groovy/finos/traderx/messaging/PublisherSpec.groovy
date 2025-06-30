package finos.traderx.messaging

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Title("Publisher Interface Tests")
@Subject(Publisher)
class PublisherSpec extends Specification {

    def "should be an interface"() {
        expect: "Publisher is an interface"
        Publisher.class.isInterface()
    }

    def "should be generic interface"() {
        expect: "Publisher supports generic types"
        Publisher.class.typeParameters.length == 1
        Publisher.class.typeParameters[0].name == "T"
    }

    def "should define required methods"() {
        given: "the Publisher interface"
        def methods = Publisher.class.declaredMethods

        expect: "all required methods are defined"
        methods.find { it.name == "publish" && it.parameterCount == 1 }
        methods.find { it.name == "publish" && it.parameterCount == 2 }
        methods.find { it.name == "isConnected" && it.returnType == boolean.class }
        methods.find { it.name == "connect" }
        methods.find { it.name == "disconnect" }
    }

    def "should declare PubSubException in method signatures"() {
        given: "Publisher methods"
        def publishMethod1 = Publisher.class.getMethod("publish", Object.class)
        def publishMethod2 = Publisher.class.getMethod("publish", String.class, Object.class)
        def connectMethod = Publisher.class.getMethod("connect")
        def disconnectMethod = Publisher.class.getMethod("disconnect")

        expect: "methods declare PubSubException"
        publishMethod1.exceptionTypes.contains(PubSubException.class)
        publishMethod2.exceptionTypes.contains(PubSubException.class)
        connectMethod.exceptionTypes.contains(PubSubException.class)
        disconnectMethod.exceptionTypes.contains(PubSubException.class)
    }

    def "should support implementation by concrete classes"() {
        given: "a concrete implementation"
        def publisher = new TestPublisher()

        expect: "implementation works correctly"
        publisher instanceof Publisher
        !publisher.isConnected()
    }

    def "should support basic publish operations"() {
        given: "a connected publisher"
        def publisher = new TestPublisher()
        publisher.connect()

        when: "publishing messages"
        publisher.publish("test message")
        publisher.publish("/topic", "topic message")

        then: "messages are published"
        publisher.publishedMessages.size() == 2
        publisher.publishedMessages[0].message == "test message"
        publisher.publishedMessages[0].topic == null
        publisher.publishedMessages[1].message == "topic message"
        publisher.publishedMessages[1].topic == "/topic"
    }

    def "should handle connection lifecycle"() {
        given: "a publisher"
        def publisher = new TestPublisher()

        expect: "initial state is disconnected"
        !publisher.isConnected()

        when: "connecting"
        publisher.connect()

        then: "publisher is connected"
        publisher.isConnected()

        when: "disconnecting"
        publisher.disconnect()

        then: "publisher is disconnected"
        !publisher.isConnected()
    }

    def "should throw exception when publishing while disconnected"() {
        given: "a disconnected publisher"
        def publisher = new TestPublisher()

        when: "attempting to publish"
        publisher.publish("message")

        then: "exception is thrown"
        thrown(PubSubException)
    }

    def "should support different message types"() {
        given: "a connected publisher"
        def publisher = new TestPublisher<Object>()
        publisher.connect()

        when: "publishing different message types"
        publisher.publish("string message")
        publisher.publish(123)
        publisher.publish([key: "value"])
        publisher.publish(new Date())

        then: "all message types are handled"
        publisher.publishedMessages.size() == 4
        publisher.publishedMessages[0].message instanceof String
        publisher.publishedMessages[1].message instanceof Integer
        publisher.publishedMessages[2].message instanceof Map
        publisher.publishedMessages[3].message instanceof Date
    }

    def "should support topic-based publishing"() {
        given: "a connected publisher"
        def publisher = new TestPublisher()
        publisher.connect()

        when: "publishing to different topics"
        publisher.publish("/trades", "trade message")
        publisher.publish("/orders", "order message")
        publisher.publish("/notifications", "notification message")

        then: "messages are published to correct topics"
        publisher.publishedMessages.size() == 3
        publisher.publishedMessages.find { it.topic == "/trades" }.message == "trade message"
        publisher.publishedMessages.find { it.topic == "/orders" }.message == "order message"
        publisher.publishedMessages.find { it.topic == "/notifications" }.message == "notification message"
    }

    def "should handle null messages gracefully"() {
        given: "a connected publisher"
        def publisher = new TestPublisher()
        publisher.connect()

        when: "publishing null message"
        publisher.publish(null)
        publisher.publish("/topic", null)

        then: "null messages are handled"
        publisher.publishedMessages.size() == 2
        publisher.publishedMessages[0].message == null
        publisher.publishedMessages[1].message == null
    }

    def "should support error handling during publish"() {
        given: "a publisher that fails on specific messages"
        def publisher = new TestPublisher()
        publisher.connect()
        publisher.shouldFailOnMessage = "fail"

        when: "publishing a message that causes failure"
        publisher.publish("fail")

        then: "exception is thrown"
        thrown(PubSubException)
    }

    def "should work with generic type constraints"() {
        given: "typed publishers"
        def stringPublisher = new TestPublisher<String>()
        def integerPublisher = new TestPublisher<Integer>()

        when: "connecting publishers"
        stringPublisher.connect()
        integerPublisher.connect()

        and: "publishing typed messages"
        stringPublisher.publish("string message")
        integerPublisher.publish(42)

        then: "messages are published correctly"
        stringPublisher.publishedMessages[0].message == "string message"
        integerPublisher.publishedMessages[0].message == 42
    }

    def "should support publisher chaining patterns"() {
        given: "multiple publishers"
        def publisher1 = new TestPublisher()
        def publisher2 = new TestPublisher()
        def publisher3 = new TestPublisher()

        when: "connecting all publishers"
        [publisher1, publisher2, publisher3].each { it.connect() }

        and: "publishing through chain"
        def message = "chain message"
        publisher1.publish(message)
        publisher2.publish("/relay", publisher1.publishedMessages[0].message)
        publisher3.publish("/final", publisher2.publishedMessages[0].message)

        then: "message flows through chain"
        publisher1.publishedMessages[0].message == message
        publisher2.publishedMessages[0].message == message
        publisher3.publishedMessages[0].message == message
    }

    def "should handle concurrent publishing"() {
        given: "a connected publisher"
        def publisher = new TestPublisher()
        publisher.connect()
        def threads = []

        when: "publishing concurrently"
        (1..10).each { i ->
            threads << Thread.start {
                publisher.publish("message $i")
            }
        }
        threads.each { it.join() }

        then: "all messages are published"
        publisher.publishedMessages.size() == 10
        (1..10).each { i ->
            assert publisher.publishedMessages.find { it.message == "message $i" }
        }
    }

    def "should support publisher lifecycle events"() {
        given: "a publisher with lifecycle tracking"
        def publisher = new TestPublisher()

        when: "going through lifecycle"
        publisher.connect()
        publisher.publish("message")
        publisher.disconnect()

        then: "lifecycle events are tracked"
        publisher.connectCalled
        publisher.publishedMessages.size() == 1
        publisher.disconnectCalled
    }

    // Test implementation
    static class TestPublisher<T> implements Publisher<T> {
        boolean connected = false
        List<PublishedMessage> publishedMessages = []
        String shouldFailOnMessage = null
        boolean connectCalled = false
        boolean disconnectCalled = false

        @Override
        void publish(T message) throws PubSubException {
            publish(null, message)
        }

        @Override
        void publish(String topic, T message) throws PubSubException {
            if (!connected) {
                throw new PubSubException("Not connected")
            }
            if (shouldFailOnMessage != null && message == shouldFailOnMessage) {
                throw new PubSubException("Simulated failure")
            }
            publishedMessages << new PublishedMessage(topic: topic, message: message)
        }

        @Override
        boolean isConnected() {
            return connected
        }

        @Override
        void connect() throws PubSubException {
            connected = true
            connectCalled = true
        }

        @Override
        void disconnect() throws PubSubException {
            connected = false
            disconnectCalled = true
        }

        static class PublishedMessage {
            String topic
            Object message
        }
    }
}