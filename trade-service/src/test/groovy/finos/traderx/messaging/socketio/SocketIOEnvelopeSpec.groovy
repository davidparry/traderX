package finos.traderx.messaging.socketio

import finos.traderx.messaging.Envelope
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("SocketIOEnvelope Tests")
@Subject(SocketIOEnvelope)
class SocketIOEnvelopeSpec extends Specification {

    def "should implement Envelope interface"() {
        expect: "SocketIOEnvelope implements Envelope"
        Envelope.class.isAssignableFrom(SocketIOEnvelope.class)
    }

    def "should create envelope with default constructor"() {
        when: "creating envelope with default constructor"
        def envelope = new SocketIOEnvelope()

        then: "all fields should have default values"
        envelope.topic == null
        envelope.payload == null
        envelope.date != null // Date is initialized
        envelope.from == null
        envelope.type == null
    }

    def "should create envelope with parameterized constructor"() {
        given: "test data"
        def topic = "/trades"
        def payload = new TestPayload(id: 123, name: "Test Trade")

        when: "creating envelope with topic and payload"
        def envelope = new SocketIOEnvelope(topic, payload)

        then: "fields should be set correctly"
        envelope.topic == topic
        envelope.payload == payload
        envelope.date != null
        envelope.from == null
        envelope.type == "TestPayload"
    }

    @Unroll
    def "should handle different payload types: #payloadType"() {
        given: "various payload types"
        def topic = "/test"

        when: "creating envelope with different payloads"
        def envelope = new SocketIOEnvelope(topic, payload)

        then: "type should be set to simple class name"
        envelope.type == expectedType
        envelope.payload == payload

        where:
        payload                          | payloadType      | expectedType
        "String payload"                 | "String"         | "String"
        123                             | "Integer"        | "Integer"
        new ArrayList()                 | "ArrayList"      | "ArrayList"
        new HashMap()                   | "HashMap"        | "HashMap"
        new TestPayload(id: 1)          | "TestPayload"    | "TestPayload"
    }

    def "should handle null payload in constructor"() {
        when: "creating envelope with null payload"
        def envelope = new SocketIOEnvelope("/topic", null)

        then: "should handle gracefully"
        thrown(NullPointerException) // payload.getClass() will throw NPE
    }

    def "should set and get type correctly"() {
        given: "an envelope"
        def envelope = new SocketIOEnvelope()

        when: "setting type"
        envelope.setType("CustomType")

        then: "type should be updated"
        envelope.getType() == "CustomType"
    }

    def "should set and get payload correctly"() {
        given: "an envelope"
        def envelope = new SocketIOEnvelope()
        def payload = new TestPayload(id: 456, name: "Test")

        when: "setting payload"
        envelope.setPayload(payload)

        then: "payload should be updated"
        envelope.getPayload() == payload
    }

    def "should set and get topic correctly"() {
        given: "an envelope"
        def envelope = new SocketIOEnvelope()

        when: "setting topic"
        envelope.setTopic("/new-topic")

        then: "topic should be updated"
        envelope.getTopic() == "/new-topic"
    }

    def "should set and get from correctly"() {
        given: "an envelope"
        def envelope = new SocketIOEnvelope()

        when: "setting from"
        envelope.setFrom("sender-123")

        then: "from should be updated"
        envelope.getFrom() == "sender-123"
    }

    def "should have date initialized on creation"() {
        given: "current time before creation"
        def beforeCreation = new Date()
        Thread.sleep(10) // Small delay to ensure different timestamps

        when: "creating envelope"
        def envelope = new SocketIOEnvelope()
        def afterCreation = new Date()

        then: "date should be set and recent"
        envelope.getDate() != null
        !envelope.getDate().before(beforeCreation)
        !envelope.getDate().after(afterCreation)
    }

    def "should maintain date from creation time"() {
        given: "an envelope created earlier"
        def envelope = new SocketIOEnvelope()
        def creationDate = envelope.getDate()
        Thread.sleep(100) // Wait to ensure time passes

        when: "accessing date later"
        def laterDate = envelope.getDate()

        then: "date should remain the same"
        laterDate == creationDate
    }

    @Unroll
    def "should handle edge cases for setters: #scenario"() {
        given: "an envelope"
        def envelope = new SocketIOEnvelope()

        when: "setting values"
        envelope.setType(type)
        envelope.setTopic(topic)
        envelope.setFrom(from)
        envelope.setPayload(payload)

        then: "values should be set correctly"
        envelope.getType() == type
        envelope.getTopic() == topic
        envelope.getFrom() == from
        envelope.getPayload() == payload

        where:
        scenario                | type    | topic    | from    | payload
        "all null values"       | null    | null     | null    | null
        "empty strings"         | ""      | ""       | ""      | ""
        "whitespace strings"    | "  "    | "  "     | "  "    | "  "
        "special characters"    | "@#\$%"  | "//@#"   | "<<>>"  | "!@#"
        "very long strings"     | "x"*100 | "y"*100  | "z"*100 | "a"*100
    }

    def "should support generic types"() {
        given: "envelopes with different generic types"
        def stringEnvelope = new SocketIOEnvelope<String>("/string", "Hello")
        def listEnvelope = new SocketIOEnvelope<List>("/list", [1, 2, 3])
        def mapEnvelope = new SocketIOEnvelope<Map>("/map", [key: "value"])

        expect: "each envelope maintains its type"
        stringEnvelope.getPayload() instanceof String
        listEnvelope.getPayload() instanceof List
        mapEnvelope.getPayload() instanceof Map
    }

    def "should be usable as Envelope interface"() {
        given: "an envelope as interface type"
        Envelope<String> envelope = new SocketIOEnvelope<String>("/test", "payload")

        expect: "all interface methods work"
        envelope.getTopic() == "/test"
        envelope.getPayload() == "payload"
        envelope.getType() == "String"
        envelope.getDate() != null
        envelope.getFrom() == null
    }

    def "should handle complex nested objects"() {
        given: "a complex nested object"
        def complexPayload = [
            id: 1,
            nested: [
                level2: [
                    level3: "deep value"
                ]
            ],
            list: [1, 2, 3]
        ]

        when: "creating envelope with complex payload"
        def envelope = new SocketIOEnvelope("/complex", complexPayload)

        then: "payload is preserved correctly"
        envelope.getPayload() == complexPayload
        envelope.getType() == "LinkedHashMap"
    }

    def "should support builder pattern usage"() {
        given: "an envelope"
        def envelope = new SocketIOEnvelope()

        when: "using setters in chain (simulated builder pattern)"
        envelope.setTopic("/builder")
        envelope.setType("BuilderType")
        envelope.setFrom("builder-sender")
        envelope.setPayload("builder payload")

        then: "all values are set"
        envelope.getTopic() == "/builder"
        envelope.getType() == "BuilderType"
        envelope.getFrom() == "builder-sender"
        envelope.getPayload() == "builder payload"
    }

    // Helper class for testing
    static class TestPayload {
        int id
        String name
    }
}