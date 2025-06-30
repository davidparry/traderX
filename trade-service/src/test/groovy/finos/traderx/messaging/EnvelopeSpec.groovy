package finos.traderx.messaging

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Title("Envelope Interface Tests")
@Subject(Envelope)
class EnvelopeSpec extends Specification {

    def "should be an interface"() {
        expect: "Envelope is an interface"
        Envelope.class.isInterface()
    }

    def "should define required methods"() {
        given: "the Envelope interface"
        def methods = Envelope.class.declaredMethods

        expect: "all required methods are defined"
        methods.find { it.name == "getType" && it.returnType == String.class }
        methods.find { it.name == "getTopic" && it.returnType == String.class }
        methods.find { it.name == "getPayload" && it.returnType == Object.class }
        methods.find { it.name == "getDate" && it.returnType == Date.class }
        methods.find { it.name == "getFrom" && it.returnType == String.class }
    }

    def "should be generic interface"() {
        expect: "Envelope supports generic types"
        Envelope.class.typeParameters.length == 1
        Envelope.class.typeParameters[0].name == "T"
    }

    def "should support implementation by concrete classes"() {
        given: "a concrete implementation"
        def envelope = new TestEnvelope()

        expect: "implementation works correctly"
        envelope instanceof Envelope
        envelope.type == "TestType"
        envelope.topic == "/test"
        envelope.payload == "test payload"
        envelope.date != null
        envelope.from == "test sender"
    }

    def "should support generic payload types"() {
        given: "envelopes with different payload types"
        def stringEnvelope = new TestEnvelope<String>()
        def integerEnvelope = new TestEnvelope<Integer>()
        def listEnvelope = new TestEnvelope<List>()

        expect: "generic types work correctly"
        stringEnvelope instanceof Envelope
        integerEnvelope instanceof Envelope
        listEnvelope instanceof Envelope
    }

    def "should be usable in messaging patterns"() {
        given: "a message processing function"
        def processMessage = { Envelope<?> envelope ->
            return [
                type: envelope.type,
                topic: envelope.topic,
                hasPayload: envelope.payload != null,
                timestamp: envelope.date,
                sender: envelope.from
            ]
        }

        when: "processing an envelope"
        def envelope = new TestEnvelope()
        def result = processMessage(envelope)

        then: "envelope data is accessible"
        result.type == "TestType"
        result.topic == "/test"
        result.hasPayload == true
        result.timestamp != null
        result.sender == "test sender"
    }

    def "should support null values in contract"() {
        given: "an envelope with null values"
        def envelope = new TestEnvelopeWithNulls()

        expect: "null values are handled"
        envelope.type == null
        envelope.topic == null
        envelope.payload == null
        envelope.date == null
        envelope.from == null
    }

    def "should be suitable for serialization frameworks"() {
        given: "envelope methods for serialization"
        def methods = Envelope.class.declaredMethods

        expect: "all methods are getter methods suitable for serialization"
        methods.every { method ->
            method.name.startsWith("get") && 
            method.parameterCount == 0 &&
            method.returnType != void.class
        }
    }

    def "should support inheritance hierarchy"() {
        given: "an extended envelope interface"
        def extendedEnvelope = new ExtendedTestEnvelope()

        expect: "inheritance works correctly"
        extendedEnvelope instanceof Envelope
        extendedEnvelope instanceof ExtendedEnvelope
        extendedEnvelope.type == "Extended"
        extendedEnvelope.priority == "HIGH"
    }

    def "should work with collections"() {
        given: "a collection of envelopes"
        def envelopes = [
            new TestEnvelope(),
            new TestEnvelopeWithNulls(),
            new ExtendedTestEnvelope()
        ] as List<Envelope<?>>

        expect: "collection operations work"
        envelopes.size() == 3
        envelopes.every { it instanceof Envelope }
        envelopes.findAll { it.type != null }.size() == 2
    }

    def "should support functional programming patterns"() {
        given: "a list of envelopes"
        def envelopes = [
            new TestEnvelope(),
            new TestEnvelopeWithNulls(),
            new ExtendedTestEnvelope()
        ] as List<Envelope<?>>

        when: "using functional operations"
        def topics = envelopes
            .findAll { it.topic != null }
            .collect { it.topic }
            .unique()

        then: "functional operations work correctly"
        topics.size() == 2
        topics.contains("/test")
        topics.contains("/extended")
    }

    // Test implementations
    static class TestEnvelope<T> implements Envelope<T> {
        @Override
        String getType() { return "TestType" }

        @Override
        String getTopic() { return "/test" }

        @Override
        T getPayload() { return "test payload" as T }

        @Override
        Date getDate() { return new Date() }

        @Override
        String getFrom() { return "test sender" }
    }

    static class TestEnvelopeWithNulls implements Envelope<Object> {
        @Override
        String getType() { return null }

        @Override
        String getTopic() { return null }

        @Override
        Object getPayload() { return null }

        @Override
        Date getDate() { return null }

        @Override
        String getFrom() { return null }
    }

    static interface ExtendedEnvelope<T> extends Envelope<T> {
        String getPriority()
    }

    static class ExtendedTestEnvelope implements ExtendedEnvelope<String> {
        @Override
        String getType() { return "Extended" }

        @Override
        String getTopic() { return "/extended" }

        @Override
        String getPayload() { return "extended payload" }

        @Override
        Date getDate() { return new Date() }

        @Override
        String getFrom() { return "extended sender" }

        @Override
        String getPriority() { return "HIGH" }
    }
}