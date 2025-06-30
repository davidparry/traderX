package finos.traderx.messaging

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Title("Subscriber Interface Tests")
@Subject(Subscriber)
class SubscriberSpec extends Specification {

    def "should be an interface"() {
        expect: "Subscriber is an interface"
        Subscriber.class.isInterface()
    }

    def "should be generic interface"() {
        expect: "Subscriber supports generic types"
        Subscriber.class.typeParameters.length == 1
        Subscriber.class.typeParameters[0].name == "T"
    }

    def "should define required methods"() {
        given: "the Subscriber interface"
        def methods = Subscriber.class.declaredMethods

        expect: "all required methods are defined"
        methods.find { it.name == "subscribe" && it.parameterCount == 1 }
        methods.find { it.name == "unsubscribe" && it.parameterCount == 1 }
        methods.find { it.name == "onMessage" && it.parameterCount == 2 }
        methods.find { it.name == "isConnected" && it.returnType == boolean.class }
        methods.find { it.name == "connect" }
        methods.find { it.name == "disconnect" }
    }

    def "should declare PubSubException in method signatures"() {
        given: "Subscriber methods"
        def subscribeMethod = Subscriber.class.getMethod("subscribe", String.class)
        def unsubscribeMethod = Subscriber.class.getMethod("unsubscribe", String.class)
        def connectMethod = Subscriber.class.getMethod("connect")
        def disconnectMethod = Subscriber.class.getMethod("disconnect")

        expect: "methods declare PubSubException"
        subscribeMethod.exceptionTypes.contains(PubSubException.class)
        unsubscribeMethod.exceptionTypes.contains(PubSubException.class)
        connectMethod.exceptionTypes.contains(PubSubException.class)
        disconnectMethod.exceptionTypes.contains(PubSubException.class)
    }

    def "should support implementation by concrete classes"() {
        given: "a concrete implementation"
        def subscriber = new TestSubscriber()

        expect: "implementation works correctly"
        subscriber instanceof Subscriber
        !subscriber.isConnected()
    }

    def "should support subscription lifecycle"() {
        given: "a connected subscriber"
        def subscriber = new TestSubscriber()
        subscriber.connect()

        when: "subscribing to topics"
        subscriber.subscribe("/trades")
        subscriber.subscribe("/orders")

        then: "subscriptions are tracked"
        subscriber.subscriptions.contains("/trades")
        subscriber.subscriptions.contains("/orders")

        when: "unsubscribing from topic"
        subscriber.unsubscribe("/trades")

        then: "subscription is removed"
        !subscriber.subscriptions.contains("/trades")
        subscriber.subscriptions.contains("/orders")
    }

    def "should handle connection lifecycle"() {
        given: "a subscriber"
        def subscriber = new TestSubscriber()

        expect: "initial state is disconnected"
        !subscriber.isConnected()

        when: "connecting"
        subscriber.connect()

        then: "subscriber is connected"
        subscriber.isConnected()

        when: "disconnecting"
        subscriber.disconnect()

        then: "subscriber is disconnected"
        !subscriber.isConnected()
    }

    def "should handle message reception"() {
        given: "a connected subscriber"
        def subscriber = new TestSubscriber()
        subscriber.connect()
        subscriber.subscribe("/test")

        when: "receiving messages"
        def envelope1 = new TestEnvelope("/test", "message1")
        def envelope2 = new TestEnvelope("/test", "message2")
        
        subscriber.onMessage(envelope1, "message1")
        subscriber.onMessage(envelope2, "message2")

        then: "messages are received"
        subscriber.receivedMessages.size() == 2
        subscriber.receivedMessages[0].message == "message1"
        subscriber.receivedMessages[1].message == "message2"
    }

    def "should support different message types"() {
        given: "a connected subscriber"
        def subscriber = new TestSubscriber<Object>()
        subscriber.connect()
        subscriber.subscribe("/mixed")

        when: "receiving different message types"
        subscriber.onMessage(new TestEnvelope("/mixed", "string"), "string")
        subscriber.onMessage(new TestEnvelope("/mixed", 123), 123)
        subscriber.onMessage(new TestEnvelope("/mixed", [key: "value"]), [key: "value"])

        then: "all message types are handled"
        subscriber.receivedMessages.size() == 3
        subscriber.receivedMessages[0].message instanceof String
        subscriber.receivedMessages[1].message instanceof Integer
        subscriber.receivedMessages[2].message instanceof Map
    }

    def "should handle multiple topic subscriptions"() {
        given: "a connected subscriber"
        def subscriber = new TestSubscriber()
        subscriber.connect()

        when: "subscribing to multiple topics"
        subscriber.subscribe("/trades")
        subscriber.subscribe("/orders")
        subscriber.subscribe("/notifications")

        then: "all subscriptions are active"
        subscriber.subscriptions.size() == 3
        subscriber.subscriptions.containsAll(["/trades", "/orders", "/notifications"])
    }

    def "should handle subscription errors"() {
        given: "a subscriber that fails on specific topics"
        def subscriber = new TestSubscriber()
        subscriber.connect()
        subscriber.shouldFailOnTopic = "/fail"

        when: "subscribing to failing topic"
        subscriber.subscribe("/fail")

        then: "exception is thrown"
        thrown(PubSubException)
    }

    def "should handle null topics gracefully"() {
        given: "a connected subscriber"
        def subscriber = new TestSubscriber()
        subscriber.connect()

        when: "subscribing to null topic"
        subscriber.subscribe(null)

        then: "null topic is handled"
        subscriber.subscriptions.contains(null)
    }

    def "should support envelope metadata access"() {
        given: "a connected subscriber"
        def subscriber = new TestSubscriber()
        subscriber.connect()
        subscriber.subscribe("/metadata")

        when: "receiving message with envelope metadata"
        def envelope = new TestEnvelope("/metadata", "payload")
        envelope.type = "TestMessage"
        envelope.from = "sender123"
        envelope.date = new Date()
        
        subscriber.onMessage(envelope, "payload")

        then: "envelope metadata is accessible"
        def received = subscriber.receivedMessages[0]
        received.envelope.topic == "/metadata"
        received.envelope.type == "TestMessage"
        received.envelope.from == "sender123"
        received.envelope.date != null
    }

    def "should support message filtering by topic"() {
        given: "a subscriber with topic filtering"
        def subscriber = new TestSubscriber()
        subscriber.connect()
        subscriber.subscribe("/trades")

        when: "receiving messages for different topics"
        subscriber.onMessage(new TestEnvelope("/trades", "trade1"), "trade1")
        subscriber.onMessage(new TestEnvelope("/orders", "order1"), "order1")
        subscriber.onMessage(new TestEnvelope("/trades", "trade2"), "trade2")

        then: "all messages are received (filtering is implementation-specific)"
        subscriber.receivedMessages.size() == 3
    }

    def "should handle concurrent message reception"() {
        given: "a connected subscriber"
        def subscriber = new TestSubscriber()
        subscriber.connect()
        subscriber.subscribe("/concurrent")
        def threads = []
        def latch = new java.util.concurrent.CountDownLatch(10)

        when: "receiving messages concurrently"
        (1..10).each { i ->
            threads << Thread.start {
                subscriber.onMessage(new TestEnvelope("/concurrent", "message$i"), "message$i")
                latch.countDown()
            }
        }
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)

        then: "all messages are received"
        subscriber.receivedMessages.size() == 10
        (1..10).each { i ->
            assert subscriber.receivedMessages.find { it.message == "message$i" }
        }
    }

    def "should support unsubscribe from all topics"() {
        given: "a subscriber with multiple subscriptions"
        def subscriber = new TestSubscriber()
        subscriber.connect()
        subscriber.subscribe("/topic1")
        subscriber.subscribe("/topic2")
        subscriber.subscribe("/topic3")

        when: "unsubscribing from all topics"
        def topics = new ArrayList(subscriber.subscriptions)
        topics.each { topic ->
            subscriber.unsubscribe(topic)
        }

        then: "all subscriptions are removed"
        subscriber.subscriptions.isEmpty()
    }

    def "should support subscriber lifecycle events"() {
        given: "a subscriber with lifecycle tracking"
        def subscriber = new TestSubscriber()

        when: "going through lifecycle"
        subscriber.connect()
        subscriber.subscribe("/test")
        subscriber.onMessage(new TestEnvelope("/test", "msg"), "msg")
        subscriber.unsubscribe("/test")
        subscriber.disconnect()

        then: "lifecycle events are tracked"
        subscriber.connectCalled
        subscriber.subscriptions.isEmpty()
        subscriber.receivedMessages.size() == 1
        subscriber.disconnectCalled
    }

    def "should work with generic type constraints"() {
        given: "typed subscribers"
        def stringSubscriber = new TestSubscriber<String>()
        def integerSubscriber = new TestSubscriber<Integer>()

        when: "connecting and subscribing"
        stringSubscriber.connect()
        integerSubscriber.connect()
        stringSubscriber.subscribe("/strings")
        integerSubscriber.subscribe("/integers")

        and: "receiving typed messages"
        stringSubscriber.onMessage(new TestEnvelope("/strings", "hello"), "hello")
        integerSubscriber.onMessage(new TestEnvelope("/integers", 42), 42)

        then: "messages are received correctly"
        stringSubscriber.receivedMessages[0].message == "hello"
        integerSubscriber.receivedMessages[0].message == 42
    }

    // Test implementations
    static class TestSubscriber<T> implements Subscriber<T> {
        boolean connected = false
        Set<String> subscriptions = Collections.synchronizedSet(new HashSet<>())
        List<ReceivedMessage> receivedMessages = Collections.synchronizedList([])
        String shouldFailOnTopic = null
        boolean connectCalled = false
        boolean disconnectCalled = false

        @Override
        void subscribe(String topic) throws PubSubException {
            if (shouldFailOnTopic != null && topic == shouldFailOnTopic) {
                throw new PubSubException("Simulated subscription failure")
            }
            subscriptions.add(topic)
        }

        @Override
        void unsubscribe(String topic) throws PubSubException {
            subscriptions.remove(topic)
        }

        @Override
        void onMessage(Envelope<?> envelope, T message) {
            receivedMessages << new ReceivedMessage(envelope: envelope, message: message)
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

        static class ReceivedMessage {
            Envelope<?> envelope
            Object message
        }
    }

    static class TestEnvelope implements Envelope<Object> {
        String topic
        Object payload
        String type
        String from
        Date date = new Date()

        TestEnvelope(String topic, Object payload) {
            this.topic = topic
            this.payload = payload
        }

        @Override
        String getType() { return type }

        @Override
        String getTopic() { return topic }

        @Override
        Object getPayload() { return payload }

        @Override
        Date getDate() { return date }

        @Override
        String getFrom() { return from }
    }
}