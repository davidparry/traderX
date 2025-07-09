package finos.traderx.messaging

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll
import java.util.Collections

@Title("PubSubException Tests")
@Subject(PubSubException)
class PubSubExceptionSpec extends Specification {

    def "should extend Exception"() {
        expect: "PubSubException extends Exception"
        Exception.class.isAssignableFrom(PubSubException.class)
    }

    def "should create exception with message"() {
        given: "an error message"
        def message = "Connection failed"

        when: "creating exception with message"
        def exception = new PubSubException(message)

        then: "exception is created with correct message"
        exception.message == message
        exception.cause == null
    }

    def "should create exception with message and cause"() {
        given: "an error message and cause"
        def message = "Connection failed"
        def cause = new RuntimeException("Network error")

        when: "creating exception with message and cause"
        def exception = new PubSubException(message, cause)

        then: "exception is created with correct message and cause"
        exception.message == message
        exception.cause == cause
    }

    def "should create exception with cause only"() {
        given: "a cause exception"
        def cause = new RuntimeException("Network error")

        when: "creating exception with cause only"
        def exception = new PubSubException(cause)

        then: "exception is created with cause"
        exception.cause == cause
        exception.message == "java.lang.RuntimeException: Network error"
    }

    def "should handle null message"() {
        when: "creating exception with null message"
        def exception = new PubSubException(null as String)

        then: "exception is created correctly"
        exception.message == null
    }

    @Unroll
    def "should handle various message types: #scenario"() {
        when: "creating exception with different message types"
        def exception = new PubSubException(message)

        then: "exception is created correctly"
        exception.message == message

        where:
        scenario              | message
        "empty message"       | ""
        "whitespace message"  | "   "
        "special characters"  | "Error: @#\$%^&*()"
        "long message"        | "x" * 1000
        "unicode message"     | "Error: 测试 🚀"
    }

    @Unroll
    def "should handle various cause types: #scenario"() {
        given: "different types of causes"
        def message = "PubSub operation failed"

        when: "creating exception with different causes"
        def exception = new PubSubException(message, cause)

        then: "exception is created with correct cause"
        exception.message == message
        exception.cause == cause

        where:
        scenario                    | cause
        "RuntimeException"          | new RuntimeException("Runtime error")
        "IllegalArgumentException"  | new IllegalArgumentException("Invalid argument")
        "IOException"              | new IOException("IO error")
        "NullPointerException"     | new NullPointerException("Null pointer")
        "null cause"               | null
    }

    def "should be throwable and catchable"() {
        given: "a PubSubException"
        def message = "Test exception"

        when: "throwing the exception"
        throw new PubSubException(message)

        then: "exception is caught correctly"
        def caughtException = thrown(PubSubException)
        caughtException.message == message
    }

    def "should be catchable as Exception"() {
        given: "a PubSubException"
        def message = "Test exception"

        when: "throwing PubSubException and catching as Exception"
        def caughtException = null
        try {
            throw new PubSubException(message)
        } catch (Exception e) {
            caughtException = e
        }

        then: "exception is caught as Exception"
        caughtException instanceof PubSubException
        caughtException.message == message
    }

    def "should support exception chaining"() {
        given: "a chain of exceptions"
        def rootCause = new RuntimeException("Root cause")
        def intermediateCause = new IllegalStateException("Intermediate", rootCause)
        def pubSubException = new PubSubException("PubSub error", intermediateCause)

        expect: "exception chain is preserved"
        pubSubException.cause == intermediateCause
        pubSubException.cause.cause == rootCause
        pubSubException.message == "PubSub error"
    }

    def "should handle stack trace correctly"() {
        given: "a PubSubException"
        def exception = new PubSubException("Stack trace test")

        when: "getting stack trace"
        def stackTrace = exception.stackTrace

        then: "stack trace is available"
        stackTrace != null
        stackTrace.length > 0
        // Stack trace should contain test class somewhere in the trace
        stackTrace.any { it.className.contains("PubSubExceptionSpec") }
    }

    def "should support suppressed exceptions"() {
        given: "a PubSubException with suppressed exceptions"
        def mainException = new PubSubException("Main error")
        def suppressedException1 = new RuntimeException("Suppressed 1")
        def suppressedException2 = new IllegalStateException("Suppressed 2")

        when: "adding suppressed exceptions"
        mainException.addSuppressed(suppressedException1)
        mainException.addSuppressed(suppressedException2)

        then: "suppressed exceptions are available"
        mainException.suppressed.length == 2
        mainException.suppressed[0] == suppressedException1
        mainException.suppressed[1] == suppressedException2
    }

    def "should be serializable"() {
        given: "a PubSubException"
        def originalException = new PubSubException("Serialization test", new RuntimeException("Cause"))

        when: "serializing and deserializing"
        def baos = new ByteArrayOutputStream()
        def oos = new ObjectOutputStream(baos)
        oos.writeObject(originalException)
        oos.close()

        def bais = new ByteArrayInputStream(baos.toByteArray())
        def ois = new ObjectInputStream(bais)
        def deserializedException = ois.readObject() as PubSubException
        ois.close()

        then: "deserialized exception matches original"
        deserializedException.message == originalException.message
        deserializedException.cause.message == originalException.cause.message
        deserializedException.class == originalException.class
    }

    def "should work in messaging context scenarios"() {
        given: "messaging operation scenarios"
        def scenarios = [
            [operation: "connect", error: "Connection timeout"],
            [operation: "publish", error: "Message serialization failed"],
            [operation: "subscribe", error: "Topic not found"],
            [operation: "disconnect", error: "Socket already closed"]
        ]

        expect: "PubSubException works for all messaging scenarios"
        scenarios.each { scenario ->
            def exception = new PubSubException("${scenario.operation} failed: ${scenario.error}")
            assert exception.message.contains(scenario.operation)
            assert exception.message.contains(scenario.error)
        }
    }

    def "should handle concurrent exception creation"() {
        given: "multiple threads creating exceptions"
        def exceptions = Collections.synchronizedList([])
        def threads = []

        when: "creating exceptions concurrently"
        (1..10).each { i ->
            def thread = Thread.start {
                synchronized(exceptions) {
                    exceptions << new PubSubException("Concurrent exception $i")
                }
            }
            threads << thread
        }

        threads.each { it.join() }

        then: "all exceptions are created correctly"
        exceptions.size() == 10
        exceptions.each { exception ->
            assert exception instanceof PubSubException
            assert exception.message.startsWith("Concurrent exception")
        }
    }
}