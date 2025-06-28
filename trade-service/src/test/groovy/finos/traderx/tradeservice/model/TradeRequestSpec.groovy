package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("TradeRequest Domain Model Tests")
@Subject(TradeRequest)
class TradeRequestSpec extends Specification {

    def "should create TradeRequest with default constructor"() {
        when: "creating TradeRequest with default constructor"
        def tradeRequest = new TradeRequest()

        then: "all fields should have default values"
        tradeRequest.accountId == 0
        tradeRequest.security == null
        tradeRequest.side == null
        tradeRequest.quantity == null
    }

    @Unroll
    def "should set and get accountId correctly for value #accountId"() {
        given: "a TradeRequest instance"
        def tradeRequest = new TradeRequest()

        when: "setting accountId"
        tradeRequest.setAccountId(accountId)

        then: "accountId should be retrievable"
        tradeRequest.getAccountId() == accountId

        where:
        accountId << [1, 100, 9999, Integer.MAX_VALUE, 0, -1]
    }

    @Unroll
    def "should set and get security correctly for value '#security'"() {
        given: "a TradeRequest instance"
        def tradeRequest = new TradeRequest()

        when: "setting security"
        tradeRequest.setSecurity(security)

        then: "security should be retrievable"
        tradeRequest.getSecurity() == security

        where:
        security << ["AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "", null, "A", "VERYLONGSECURITYNAME"]
    }

    @Unroll
    def "should set and get side correctly for #side"() {
        given: "a TradeRequest instance"
        def tradeRequest = new TradeRequest()

        when: "setting side"
        tradeRequest.setSide(side)

        then: "side should be retrievable"
        tradeRequest.getSide() == side

        where:
        side << [TradeSide.Buy, TradeSide.Sell, null]
    }

    @Unroll
    def "should set and get quantity correctly for value #quantity"() {
        given: "a TradeRequest instance"
        def tradeRequest = new TradeRequest()

        when: "setting quantity"
        tradeRequest.setQuantity(quantity)

        then: "quantity should be retrievable"
        tradeRequest.getQuantity() == quantity

        where:
        quantity << [1, 100, 1000, Integer.MAX_VALUE, 0, -1, null]
    }

    @Unroll
    def "should handle complete trade request for #testCase"() {
        given: "a TradeRequest instance"
        def tradeRequest = new TradeRequest()

        when: "setting all properties"
        tradeRequest.setAccountId(accountId)
        tradeRequest.setSecurity(security)
        tradeRequest.setSide(side)
        tradeRequest.setQuantity(quantity)

        then: "all properties should be correctly set"
        tradeRequest.getAccountId() == accountId
        tradeRequest.getSecurity() == security
        tradeRequest.getSide() == side
        tradeRequest.getQuantity() == quantity

        where:
        testCase           | accountId | security | side           | quantity
        "typical buy"      | 1001      | "AAPL"   | TradeSide.Buy  | 100
        "typical sell"     | 2002      | "GOOGL"  | TradeSide.Sell | 50
        "large buy order"  | 3003      | "MSFT"   | TradeSide.Buy  | 10000
        "small sell order" | 4004      | "TSLA"   | TradeSide.Sell | 1
        "edge case"        | 0         | ""       | null           | null
    }

    def "should support fluent-style property setting"() {
        given: "a TradeRequest instance"
        def tradeRequest = new TradeRequest()

        when: "setting properties in fluent style"
        tradeRequest.setAccountId(1001)
        tradeRequest.setSecurity("AAPL")
        tradeRequest.setSide(TradeSide.Buy)
        tradeRequest.setQuantity(100)

        then: "all properties should be set correctly"
        with(tradeRequest) {
            accountId == 1001
            security == "AAPL"
            side == TradeSide.Buy
            quantity == 100
        }
    }

    def "should handle null values gracefully"() {
        given: "a TradeRequest with null values"
        def tradeRequest = new TradeRequest()

        when: "setting null values"
        tradeRequest.setAccountId(0) // primitive int cannot be null
        tradeRequest.setSecurity(null)
        tradeRequest.setSide(null)
        tradeRequest.setQuantity(null)

        then: "null values should be handled correctly"
        tradeRequest.getAccountId() == 0
        tradeRequest.getSecurity() == null
        tradeRequest.getSide() == null
        tradeRequest.getQuantity() == null
    }

    def "should support business validation scenarios"() {
        expect: "TradeRequest supports various business scenarios"
        def buyRequest = new TradeRequest()
        buyRequest.setAccountId(1001)
        buyRequest.setSecurity("AAPL")
        buyRequest.setSide(TradeSide.Buy)
        buyRequest.setQuantity(100)

        def sellRequest = new TradeRequest()
        sellRequest.setAccountId(1002)
        sellRequest.setSecurity("GOOGL")
        sellRequest.setSide(TradeSide.Sell)
        sellRequest.setQuantity(50)

        // Validate buy request
        buyRequest.getAccountId() > 0
        buyRequest.getSecurity() != null && !buyRequest.getSecurity().isEmpty()
        buyRequest.getSide() == TradeSide.Buy
        buyRequest.getQuantity() > 0

        // Validate sell request
        sellRequest.getAccountId() > 0
        sellRequest.getSecurity() != null && !sellRequest.getSecurity().isEmpty()
        sellRequest.getSide() == TradeSide.Sell
        sellRequest.getQuantity() > 0
    }

    def "should maintain independence between instances"() {
        given: "two TradeRequest instances"
        def request1 = new TradeRequest()
        def request2 = new TradeRequest()

        when: "setting different values on each instance"
        request1.setAccountId(1001)
        request1.setSecurity("AAPL")
        request1.setSide(TradeSide.Buy)
        request1.setQuantity(100)

        request2.setAccountId(2002)
        request2.setSecurity("GOOGL")
        request2.setSide(TradeSide.Sell)
        request2.setQuantity(200)

        then: "instances should maintain their own values"
        request1.getAccountId() == 1001
        request1.getSecurity() == "AAPL"
        request1.getSide() == TradeSide.Buy
        request1.getQuantity() == 100

        request2.getAccountId() == 2002
        request2.getSecurity() == "GOOGL"
        request2.getSide() == TradeSide.Sell
        request2.getQuantity() == 200
    }

    def "should handle property overwriting"() {
        given: "a TradeRequest with initial values"
        def tradeRequest = new TradeRequest()
        tradeRequest.setAccountId(1001)
        tradeRequest.setSecurity("AAPL")
        tradeRequest.setSide(TradeSide.Buy)
        tradeRequest.setQuantity(100)

        when: "overwriting the values"
        tradeRequest.setAccountId(2002)
        tradeRequest.setSecurity("GOOGL")
        tradeRequest.setSide(TradeSide.Sell)
        tradeRequest.setQuantity(200)

        then: "new values should be set"
        tradeRequest.getAccountId() == 2002
        tradeRequest.getSecurity() == "GOOGL"
        tradeRequest.getSide() == TradeSide.Sell
        tradeRequest.getQuantity() == 200
    }
}