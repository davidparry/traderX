package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("TradeResponse Domain Model Tests")
@Subject(TradeResponse)
class TradeResponseSpec extends Specification {

    def "should create TradeResponse with default constructor"() {
        when: "creating TradeResponse with default constructor"
        def tradeResponse = new TradeResponse()

        then: "all fields should have default values"
        !tradeResponse.success
        tradeResponse.id == null
        tradeResponse.errorMessage == null
    }

    @Unroll
    def "should set and get success flag correctly for value #successValue"() {
        given: "a TradeResponse instance"
        def tradeResponse = new TradeResponse()

        when: "setting success flag"
        tradeResponse.setSuccess(successValue)

        then: "success flag should be retrievable"
        tradeResponse.isSuccess() == successValue

        where:
        successValue << [true, false]
    }

    @Unroll
    def "should set and get id correctly for value '#idValue'"() {
        given: "a TradeResponse instance"
        def tradeResponse = new TradeResponse()

        when: "setting id"
        tradeResponse.setId(idValue)

        then: "id should be retrievable"
        tradeResponse.getId() == idValue

        where:
        idValue << [
            "4e7d4734-52eb-4390-bbde-441585a92bd7",
            "trade-001",
            "simple-id",
            "",
            null,
            "very-long-trade-identifier-with-many-characters"
        ]
    }

    @Unroll
    def "should set and get errorMessage correctly for value '#errorMessage'"() {
        given: "a TradeResponse instance"
        def tradeResponse = new TradeResponse()

        when: "setting errorMessage"
        tradeResponse.setErrorMessage(errorMessage)

        then: "errorMessage should be retrievable"
        tradeResponse.getErrorMessage() == errorMessage

        where:
        errorMessage << [
            "Invalid account",
            "Security not found",
            "Insufficient funds",
            "",
            null,
            "A very long error message that describes in detail what went wrong with the trade processing"
        ]
    }

    def "should create successful TradeResponse using static factory method"() {
        given: "a trade id"
        def tradeId = "4e7d4734-52eb-4390-bbde-441585a92bd7"

        when: "creating successful response"
        def response = TradeResponse.success(tradeId)

        then: "response should be configured for success"
        response.isSuccess()
        response.getId() == tradeId
        response.getErrorMessage() == null
    }

    def "should create error TradeResponse using static factory method"() {
        given: "an error message"
        def errorMessage = "Account not found"

        when: "creating error response"
        def response = TradeResponse.error(errorMessage)

        then: "response should be configured for error"
        !response.isSuccess()
        response.getErrorMessage() == errorMessage
        response.getId() == null
    }

    @Unroll
    def "should create successful responses with various ids using static factory"() {
        when: "creating successful response with id"
        def response = TradeResponse.success(tradeId)

        then: "response should be successful with correct id"
        response.isSuccess()
        response.getId() == tradeId
        response.getErrorMessage() == null

        where:
        tradeId << [
            "trade-001",
            "4e7d4734-52eb-4390-bbde-441585a92bd7",
            "simple",
            "",
            null
        ]
    }

    @Unroll
    def "should create error responses with various messages using static factory"() {
        when: "creating error response with message"
        def response = TradeResponse.error(errorMessage)

        then: "response should be error with correct message"
        !response.isSuccess()
        response.getErrorMessage() == errorMessage
        response.getId() == null

        where:
        errorMessage << [
            "Invalid account",
            "Security not found in Reference data service.",
            "Insufficient funds for trade",
            "Trade validation failed",
            "",
            null
        ]
    }

    def "should handle complete manual configuration"() {
        given: "a TradeResponse instance"
        def tradeResponse = new TradeResponse()

        when: "manually configuring all properties"
        tradeResponse.setSuccess(true)
        tradeResponse.setId("manual-trade-001")
        tradeResponse.setErrorMessage("This should be null for success")

        then: "all properties should be set as configured"
        tradeResponse.isSuccess()
        tradeResponse.getId() == "manual-trade-001"
        tradeResponse.getErrorMessage() == "This should be null for success"
    }

    def "should support business scenarios for successful trades"() {
        expect: "successful trade responses are properly configured"
        def buyOrderResponse = TradeResponse.success("buy-order-123")
        def sellOrderResponse = TradeResponse.success("sell-order-456")

        buyOrderResponse.isSuccess()
        buyOrderResponse.getId() == "buy-order-123"
        buyOrderResponse.getErrorMessage() == null

        sellOrderResponse.isSuccess()
        sellOrderResponse.getId() == "sell-order-456"
        sellOrderResponse.getErrorMessage() == null
    }

    def "should support business scenarios for failed trades"() {
        expect: "failed trade responses are properly configured"
        def accountNotFoundResponse = TradeResponse.error("Account 1001 not found in Account service.")
        def securityNotFoundResponse = TradeResponse.error("INVALID not found in Reference data service.")
        def validationFailedResponse = TradeResponse.error("Trade validation failed")

        !accountNotFoundResponse.isSuccess()
        accountNotFoundResponse.getErrorMessage() == "Account 1001 not found in Account service."
        accountNotFoundResponse.getId() == null

        !securityNotFoundResponse.isSuccess()
        securityNotFoundResponse.getErrorMessage() == "INVALID not found in Reference data service."
        securityNotFoundResponse.getId() == null

        !validationFailedResponse.isSuccess()
        validationFailedResponse.getErrorMessage() == "Trade validation failed"
        validationFailedResponse.getId() == null
    }

    def "should maintain independence between instances"() {
        given: "two TradeResponse instances"
        def response1 = TradeResponse.success("trade-001")
        def response2 = TradeResponse.error("Some error")

        expect: "instances should maintain their own state"
        response1.isSuccess()
        response1.getId() == "trade-001"
        response1.getErrorMessage() == null

        !response2.isSuccess()
        response2.getId() == null
        response2.getErrorMessage() == "Some error"
    }

    def "should allow property modification after creation"() {
        given: "a successful TradeResponse"
        def response = TradeResponse.success("original-id")

        when: "modifying properties"
        response.setSuccess(false)
        response.setId("modified-id")
        response.setErrorMessage("Modified error message")

        then: "properties should be updated"
        !response.isSuccess()
        response.getId() == "modified-id"
        response.getErrorMessage() == "Modified error message"
    }

    def "should handle edge cases in static factory methods"() {
        expect: "static factory methods handle edge cases"
        def nullIdSuccess = TradeResponse.success(null)
        def emptyIdSuccess = TradeResponse.success("")
        def nullMessageError = TradeResponse.error(null)
        def emptyMessageError = TradeResponse.error("")

        nullIdSuccess.isSuccess()
        nullIdSuccess.getId() == null

        emptyIdSuccess.isSuccess()
        emptyIdSuccess.getId() == ""

        !nullMessageError.isSuccess()
        nullMessageError.getErrorMessage() == null

        !emptyMessageError.isSuccess()
        emptyMessageError.getErrorMessage() == ""
    }
}