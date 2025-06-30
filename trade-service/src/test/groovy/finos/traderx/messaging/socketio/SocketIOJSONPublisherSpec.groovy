package finos.traderx.messaging.socketio

import com.fasterxml.jackson.databind.ObjectMapper
import finos.traderx.messaging.PubSubException
import finos.traderx.messaging.Publisher
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import org.springframework.beans.factory.InitializingBean
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("SocketIOJSONPublisher Tests")
@Subject(SocketIOJSONPublisher)
class SocketIOJSONPublisherSpec extends Specification {

    TestSocketIOJSONPublisher publisher
    Socket mockSocket

    def setup() {
        publisher = new TestSocketIOJSONPublisher()
        mockSocket = Mock(Socket)
        publisher.mockSocket = mockSocket
    }

    def "should implement Publisher and InitializingBean interfaces"() {
        expect: "SocketIOJSONPublisher implements required interfaces"
        Publisher.class.isAssignableFrom(SocketIOJSONPublisher.class)
        InitializingBean.class.isAssignableFrom(SocketIOJSONPublisher.class)
    }

    def "should initialize with default values"() {
        given: "a new publisher"
        def newPublisher = new TestSocketIOJSONPublisher()

        expect: "default values are set"
        !newPublisher.isConnected()
        newPublisher.socket == null
        ReflectionTestUtils.getField(newPublisher, "socketAddress") == "http://localhost:3000"
        ReflectionTestUtils.getField(newPublisher, "topic") == "/default"
    }

    def "should set socket address"() {
        when: "setting socket address"
        publisher.setSocketAddress("http://example.com:8080")

        then: "address is updated"
        ReflectionTestUtils.getField(publisher, "socketAddress") == "http://example.com:8080"
    }

    def "should set topic"() {
        when: "setting topic"
        publisher.setTopic("/custom-topic")

        then: "topic is updated"
        ReflectionTestUtils.getField(publisher, "topic") == "/custom-topic"
    }

    def "should publish to default topic"() {
        given: "publisher is connected"
        ReflectionTestUtils.setField(publisher, "connected", true)
        publisher.socket = mockSocket
        def message = new TestMessage(id: 1, data: "test")

        when: "publishing message"
        publisher.publish(message)

        then: "message is sent to default topic"
        1 * mockSocket.emit("publish", _)
    }

    def "should publish to specific topic"() {
        given: "publisher is connected"
        ReflectionTestUtils.setField(publisher, "connected", true)
        publisher.socket = mockSocket
        def message = new TestMessage(id: 2, data: "specific")

        when: "publishing to specific topic"
        publisher.publish("/specific-topic", message)

        then: "message is sent to specific topic"
        1 * mockSocket.emit("publish", _)
    }

    def "should throw exception when publishing while disconnected"() {
        given: "publisher is not connected"
        ReflectionTestUtils.setField(publisher, "connected", false)
        def message = new TestMessage(id: 3, data: "fail")

        when: "attempting to publish"
        publisher.publish(message)

        then: "exception is thrown"
        def ex = thrown(PubSubException)
        ex.message.contains("not connected")
    }

    def "should throw exception when publishing to topic while disconnected"() {
        given: "publisher is not connected"
        ReflectionTestUtils.setField(publisher, "connected", false)
        def message = new TestMessage(id: 4, data: "fail")

        when: "attempting to publish to specific topic"
        publisher.publish("/topic", message)

        then: "exception is thrown"
        def ex = thrown(PubSubException)
        ex.message.contains("not connected")
        ex.message.contains("/topic")
    }

    def "should handle serialization errors gracefully"() {
        given: "publisher is connected with a problematic message"
        ReflectionTestUtils.setField(publisher, "connected", true)
        publisher.socket = mockSocket
        def problematicMessage = new ProblematicMessage()

        when: "publishing message that fails serialization"
        publisher.publish(problematicMessage)

        then: "error is handled (currently just prints stack trace)"
        notThrown(Exception)
        0 * mockSocket.emit(_, _) // No emit should happen due to serialization failure
    }

    def "should disconnect when connected"() {
        given: "publisher is connected"
        ReflectionTestUtils.setField(publisher, "connected", true)
        publisher.socket = mockSocket

        when: "disconnecting"
        publisher.disconnect()

        then: "socket is disconnected and nullified"
        1 * mockSocket.disconnect()
        publisher.socket == null
    }

    def "should handle disconnect when already disconnected"() {
        given: "publisher is not connected"
        ReflectionTestUtils.setField(publisher, "connected", false)
        publisher.socket = null

        when: "disconnecting"
        publisher.disconnect()

        then: "no errors occur"
        notThrown(Exception)
    }

    def "should handle disconnect when socket exists but not connected"() {
        given: "publisher has socket but is not connected"
        ReflectionTestUtils.setField(publisher, "connected", false)
        publisher.socket = mockSocket

        when: "disconnecting"
        publisher.disconnect()

        then: "socket is nullified without disconnect call"
        0 * mockSocket.disconnect()
        publisher.socket == null
    }

    def "should connect successfully"() {
        given: "publisher is not connected"
        def socketAddress = "http://test.com:3000"
        publisher.setSocketAddress(socketAddress)

        when: "connecting"
        publisher.connect()

        then: "socket is created with correct URI"
        publisher.socket == mockSocket
        publisher.connectCalledWith.toString() == socketAddress
    }

    def "should disconnect existing socket before reconnecting"() {
        given: "publisher has existing socket"
        publisher.socket = mockSocket

        when: "connecting again"
        publisher.connect()

        then: "existing socket is disconnected first"
        1 * mockSocket.disconnect()
        publisher.socket == mockSocket // New mock socket from test implementation
    }

    def "should throw PubSubException on connection failure"() {
        given: "publisher that will fail to connect"
        publisher.shouldFailConnection = true

        when: "attempting to connect"
        publisher.connect()

        then: "exception is thrown"
        def ex = thrown(PubSubException)
        ex.message.contains("Cannot socket connection")
        ex.cause instanceof RuntimeException
    }

    def "should setup event listeners after properties set"() {
        given: "mock socket with event registration"
        def listeners = [:]
        mockSocket.on(_ as String, _ as Emitter.Listener) >> { String event, Emitter.Listener listener ->
            listeners[event] = listener
            mockSocket
        }
        mockSocket.connect() >> mockSocket

        when: "afterPropertiesSet is called"
        publisher.afterPropertiesSet()

        then: "all event listeners are registered"
        listeners.containsKey(Socket.EVENT_CONNECT)
        listeners.containsKey(Socket.EVENT_DISCONNECT)
        listeners.containsKey(Socket.EVENT_CONNECT_ERROR)
        1 * mockSocket.connect()
    }

    def "should handle connect event"() {
        given: "event listeners map"
        def listeners = [:]
        mockSocket.on(_ as String, _ as Emitter.Listener) >> { String event, Emitter.Listener listener ->
            listeners[event] = listener
            mockSocket
        }
        mockSocket.connect() >> mockSocket
        publisher.afterPropertiesSet()

        when: "connect event is triggered"
        listeners[Socket.EVENT_CONNECT].call("connected")

        then: "publisher is marked as connected"
        publisher.isConnected()
    }

    def "should handle disconnect event"() {
        given: "connected publisher with event listeners"
        def listeners = [:]
        mockSocket.on(_ as String, _ as Emitter.Listener) >> { String event, Emitter.Listener listener ->
            listeners[event] = listener
            mockSocket
        }
        mockSocket.connect() >> mockSocket
        publisher.afterPropertiesSet()
        ReflectionTestUtils.setField(publisher, "connected", true)

        when: "disconnect event is triggered"
        listeners[Socket.EVENT_DISCONNECT].call("disconnected")

        then: "publisher is marked as disconnected"
        !publisher.isConnected()
    }

    def "should handle connection error event"() {
        given: "publisher with event listeners"
        def listeners = [:]
        mockSocket.on(_ as String, _ as Emitter.Listener) >> { String event, Emitter.Listener listener ->
            listeners[event] = listener
            mockSocket
        }
        mockSocket.connect() >> mockSocket
        publisher.afterPropertiesSet()
        ReflectionTestUtils.setField(publisher, "connected", true)

        when: "connection error event is triggered"
        listeners[Socket.EVENT_CONNECT_ERROR].call("error")

        then: "publisher is marked as disconnected"
        !publisher.isConnected()
    }

    def "should return default IO options"() {
        when: "getting IO options"
        def options = publisher.getIOOptions()

        then: "default options are returned"
        options != null
        options instanceof IO.Options
    }

    @Unroll
    def "should handle various message types: #messageType"() {
        given: "publisher is connected"
        ReflectionTestUtils.setField(publisher, "connected", true)
        publisher.socket = mockSocket

        when: "publishing different message types"
        publisher.publish(message)

        then: "message is serialized correctly"
        1 * mockSocket.emit("publish", _)

        where:
        message                          | messageType      | expectedType
        "String message"                 | "String"         | "String"
        123                             | "Integer"        | "Integer"
        [1, 2, 3]                       | "List"           | "ArrayList"
        [key: "value"]                  | "Map"            | "LinkedHashMap"
    }

    def "should handle null values in envelope correctly"() {
        given: "publisher is connected"
        ReflectionTestUtils.setField(publisher, "connected", true)
        publisher.socket = mockSocket
        def message = new TestMessage(id: null, data: null)

        when: "publishing message with null fields"
        publisher.publish(message)

        then: "null values are handled in serialization"
        1 * mockSocket.emit("publish", _)
    }

    def "should maintain ObjectMapper configuration"() {
        given: "the static ObjectMapper"
        def objectMapper = ReflectionTestUtils.getField(SocketIOJSONPublisher.class, "objectMapper") as ObjectMapper

        expect: "ObjectMapper is configured correctly"
        objectMapper != null
        objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion() == com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
    }

    // Test implementation of abstract class
    static class TestSocketIOJSONPublisher extends SocketIOJSONPublisher<TestMessage> {
        Socket mockSocket
        URI connectCalledWith
        boolean shouldFailConnection = false

        @Override
        protected Socket internalConnect(URI uri) throws Exception {
            connectCalledWith = uri
            if (shouldFailConnection) {
                throw new RuntimeException("Connection failed")
            }
            return mockSocket
        }
    }

    // Test message class
    static class TestMessage {
        Integer id
        String data
    }

    // Problematic message for testing serialization errors
    static class ProblematicMessage {
        def getCircularReference() {
            return this // Circular reference causes serialization issues
        }
    }
}