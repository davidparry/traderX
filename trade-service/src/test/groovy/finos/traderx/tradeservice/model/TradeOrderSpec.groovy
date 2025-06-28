package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("TradeOrder Domain Model Tests")
@Subject(TradeOrder)
class TradeOrderSpec extends Specification {

    def "should create TradeOrder with default constructor"() {
        when: "creating TradeOrder with default constructor"
        def tradeOrder = new TradeOrder()

        then: "all fields should be null or default values"
        tradeOrder.id == null
        tradeOrder.state == null
        tradeOrder.security == null
        tradeOrder.quantity == null
        tradeOrder.accountId == null
        tradeOrder.side == null
    }

    @Unroll
    def "should create TradeOrder with parameterized constructor for #side trade"() {
        when: "creating TradeOrder with all parameters"
        def tradeOrder = new TradeOrder(id, accountId, security, side, quantity)

        then: "all fields should be set correctly"
        tradeOrder.id == id
        tradeOrder.accountId == accountId
        tradeOrder.security == security
        tradeOrder.side == side
        tradeOrder.quantity == quantity
        tradeOrder.state == null // state is not set in constructor

        where:
        id                                    | accountId | security | side           | quantity
        "trade-001"                          | 1001      | "AAPL"   | TradeSide.Buy  | 100
        "trade-002"                          | 2002      | "GOOGL"  | TradeSide.Sell | 50
        "4e7d4734-52eb-4390-bbde-441585a92bd7" | 3003      | "MSFT"   | TradeSide.Buy  | 200
    }

    def "should handle null values in constructor gracefully"() {
        when: "creating TradeOrder with null values"
        def tradeOrder = new TradeOrder(null, 0, null, null, 0)

        then: "object should be created with null/zero values"
        tradeOrder.id == null
        tradeOrder.accountId == 0
        tradeOrder.security == null
        tradeOrder.side == null
        tradeOrder.quantity == 0
    }

    @Unroll
    def "should provide correct getter values for #testCase"() {
        given: "a TradeOrder with specific values"
        def tradeOrder = new TradeOrder(id, accountId, security, side, quantity)

        expect: "getters return correct values"
        tradeOrder.getId() == id
        tradeOrder.getAccountId() == accountId
        tradeOrder.getSecurity() == security
        tradeOrder.getSide() == side
        tradeOrder.getQuantity() == quantity
        tradeOrder.getState() == null

        where:
        testCase        | id          | accountId | security | side           | quantity
        "buy order"     | "buy-001"   | 1001      | "AAPL"   | TradeSide.Buy  | 100
        "sell order"    | "sell-002"  | 2002      | "GOOGL"  | TradeSide.Sell | 75
        "large order"   | "large-003" | 3003      | "TSLA"   | TradeSide.Buy  | 1000
        "small order"   | "small-004" | 4004      | "AMZN"   | TradeSide.Sell | 1
    }

    def "should handle edge case values"() {
        given: "TradeOrder with edge case values"
        def tradeOrder = new TradeOrder("", Integer.MAX_VALUE, "", TradeSide.Buy, Integer.MAX_VALUE)

        expect: "edge values are handled correctly"
        tradeOrder.getId() == ""
        tradeOrder.getAccountId() == Integer.MAX_VALUE
        tradeOrder.getSecurity() == ""
        tradeOrder.getQuantity() == Integer.MAX_VALUE
    }

    def "should maintain immutability of constructor-set values"() {
        given: "a TradeOrder created with specific values"
        def originalId = "immutable-test"
        def originalAccountId = 9999
        def originalSecurity = "TEST"
        def originalSide = TradeSide.Buy
        def originalQuantity = 500

        def tradeOrder = new TradeOrder(originalId, originalAccountId, originalSecurity, originalSide, originalQuantity)

        when: "attempting to modify the original variables"
        originalId = "modified"
        originalAccountId = 0
        originalSecurity = "MODIFIED"
        originalQuantity = 0

        then: "TradeOrder values remain unchanged"
        tradeOrder.getId() == "immutable-test"
        tradeOrder.getAccountId() == 9999
        tradeOrder.getSecurity() == "TEST"
        tradeOrder.getSide() == TradeSide.Buy
        tradeOrder.getQuantity() == 500
    }

    def "should handle different TradeSide enum values"() {
        expect: "TradeOrder correctly stores different TradeSide values"
        def buyOrder = new TradeOrder("buy", 1, "STOCK", TradeSide.Buy, 100)
        def sellOrder = new TradeOrder("sell", 2, "STOCK", TradeSide.Sell, 100)

        buyOrder.getSide() == TradeSide.Buy
        sellOrder.getSide() == TradeSide.Sell
    }

    def "should support business validation scenarios"() {
        given: "various TradeOrder scenarios for business validation"
        def validBuyOrder = new TradeOrder("valid-buy", 1001, "AAPL", TradeSide.Buy, 100)
        def validSellOrder = new TradeOrder("valid-sell", 1002, "GOOGL", TradeSide.Sell, 50)
        def zeroQuantityOrder = new TradeOrder("zero-qty", 1003, "MSFT", TradeSide.Buy, 0)

        expect: "orders maintain their business-relevant properties"
        validBuyOrder.getQuantity() > 0
        validSellOrder.getQuantity() > 0
        zeroQuantityOrder.getQuantity() == 0

        and: "security symbols are preserved"
        validBuyOrder.getSecurity() == "AAPL"
        validSellOrder.getSecurity() == "GOOGL"
        zeroQuantityOrder.getSecurity() == "MSFT"
    }
}