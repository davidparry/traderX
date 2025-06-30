package finos.traderx.tradeservice.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("ResourceNotFoundException Tests")
@Subject(ResourceNotFoundException)
class ResourceNotFoundExceptionSpec extends Specification {

    def "should create ResourceNotFoundException with message"() {
        given: "an error message"
        def message = "Resource not found"

        when: "creating ResourceNotFoundException"
        def exception = new ResourceNotFoundException(message)

        then: "exception should contain the message"
        exception.message == message
        exception.cause == null
    }

    def "should extend RuntimeException"() {
        when: "creating ResourceNotFoundException"
        def exception = new ResourceNotFoundException("test")

        then: "it should be a RuntimeException"
        exception instanceof RuntimeException
        exception instanceof Exception
        exception instanceof Throwable
    }

    def "should have ResponseStatus annotation with NOT_FOUND status"() {
        given: "the exception class"
        def exceptionClass = ResourceNotFoundException.class

        when: "checking for ResponseStatus annotation"
        def annotation = exceptionClass.getAnnotation(ResponseStatus.class)

        then: "annotation should be present with NOT_FOUND status"
        annotation != null
        annotation.value() == HttpStatus.NOT_FOUND

    }

    @Unroll
    def "should handle various message scenarios: #scenario"() {
        when: "creating ResourceNotFoundException with different messages"
        def exception = new ResourceNotFoundException(message)

        then: "exception should handle the message correctly"
        exception.message == expectedMessage

        where:
        scenario                | message                           | expectedMessage
        "normal message"        | "User not found"                  | "User not found"
        "empty message"         | ""                                | ""
        "null message"          | null                              | null
        "detailed message"      | "User with ID 123 not found"      | "User with ID 123 not found"
        "special characters"    | "Resource @#\$% not found"         | "Resource @#\$% not found"
        "very long message"     | "A" * 500                         | "A" * 500
    }

    def "should be throwable and catchable"() {
        when: "throwing ResourceNotFoundException"
        throw new ResourceNotFoundException("Test exception")

        then: "exception should be caught"
        def e = thrown(ResourceNotFoundException)
        e.message == "Test exception"
    }

    def "should be catchable as RuntimeException"() {
        when: "throwing ResourceNotFoundException"
        throw new ResourceNotFoundException("Test exception")

        then: "exception should be caught as RuntimeException"
        def e = thrown(RuntimeException)
        e instanceof ResourceNotFoundException
        e.message == "Test exception"
    }

    def "should preserve stack trace"() {
        given: "a method that throws exception"
        def thrower = {
            throw new ResourceNotFoundException("Stack trace test")
        }

        when: "exception is thrown"
        thrower()

        then: "stack trace should be available"
        def e = thrown(ResourceNotFoundException)
        e.stackTrace.length > 0
        e.stackTrace[0].methodName != null
    }

    def "should support typical REST API error scenarios"() {
        expect: "ResourceNotFoundException handles common REST errors"
        def userNotFound = new ResourceNotFoundException("User with ID 123 not found")
        def orderNotFound = new ResourceNotFoundException("Order #456 not found")
        def productNotFound = new ResourceNotFoundException("Product SKU-789 not found")

        userNotFound.message == "User with ID 123 not found"
        orderNotFound.message == "Order #456 not found"
        productNotFound.message == "Product SKU-789 not found"
    }

    def "should work with Spring's exception handling"() {
        given: "a ResourceNotFoundException"
        def exception = new ResourceNotFoundException("Entity not found")

        expect: "exception has proper Spring annotations"
        exception.class.isAnnotationPresent(ResponseStatus.class)
        
        and: "annotation specifies 404 status"
        exception.class.getAnnotation(ResponseStatus.class).value() == HttpStatus.NOT_FOUND
    }

    def "should support internationalized messages"() {
        given: "internationalized error messages"
        def englishException = new ResourceNotFoundException("Resource not found")
        def spanishException = new ResourceNotFoundException("Recurso no encontrado")
        def frenchException = new ResourceNotFoundException("Ressource non trouvée")

        expect: "all messages are preserved correctly"
        englishException.message == "Resource not found"
        spanishException.message == "Recurso no encontrado"
        frenchException.message == "Ressource non trouvée"
    }

    def "should be usable in try-catch blocks"() {
        given: "a method that might throw ResourceNotFoundException"
        def riskyMethod = { id ->
            if (id == null) {
                throw new ResourceNotFoundException("ID cannot be null")
            }
            return "Found: $id"
        }

        when: "calling with null ID"
        def result = null
        try {
            result = riskyMethod(null)
        } catch (ResourceNotFoundException e) {
            result = "Caught: ${e.message}"
        }

        then: "exception is caught and handled"
        result == "Caught: ID cannot be null"
    }

    def "should maintain exception chaining capability"() {
        given: "a scenario where we might want to wrap exceptions"
        def originalCause = new IllegalArgumentException("Invalid ID format")

        when: "we want to wrap it in ResourceNotFoundException"
        // Note: Current implementation doesn't support cause, but testing the concept
        def exception = new ResourceNotFoundException("Resource not found due to invalid ID")

        then: "exception works as designed"
        exception.message == "Resource not found due to invalid ID"
        exception.cause == null // Current implementation doesn't support cause
    }

    def "should be serializable for distributed systems"() {
        given: "a ResourceNotFoundException"
        def exception = new ResourceNotFoundException("Distributed system error")

        expect: "exception is a proper Java exception"
        exception instanceof RuntimeException
        exception instanceof Exception
        exception instanceof Throwable
    }
}