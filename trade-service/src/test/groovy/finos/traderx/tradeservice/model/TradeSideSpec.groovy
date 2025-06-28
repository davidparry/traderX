package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("TradeSide Enum Tests")
@Subject(TradeSide)
class TradeSideSpec extends Specification {

    def "should have exactly two enum values"() {
        expect: "TradeSide enum has Buy and Sell values"
        TradeSide.values().length == 2
        TradeSide.values().contains(TradeSide.Buy)
        TradeSide.values().contains(TradeSide.Sell)
    }

    @Unroll
    def "should support valueOf for '#enumName'"() {
        when: "getting enum by name"
        def result = TradeSide.valueOf(enumName)

        then: "correct enum value is returned"
        result == expectedValue

        where:
        enumName | expectedValue
        "Buy"    | TradeSide.Buy
        "Sell"   | TradeSide.Sell
    }

    def "should throw IllegalArgumentException for invalid enum name"() {
        when: "getting enum with invalid name"
        TradeSide.valueOf("Invalid")

        then: "IllegalArgumentException is thrown"
        thrown(IllegalArgumentException)
    }

    def "should support enum comparison"() {
        expect: "enum values can be compared"
        TradeSide.Buy == TradeSide.Buy
        TradeSide.Sell == TradeSide.Sell
        TradeSide.Buy != TradeSide.Sell
        TradeSide.Sell != TradeSide.Buy
    }

    def "should support toString conversion"() {
        expect: "enum values convert to string correctly"
        TradeSide.Buy.toString() == "Buy"
        TradeSide.Sell.toString() == "Sell"
    }

    def "should support name() method"() {
        expect: "enum name() method returns correct values"
        TradeSide.Buy.name() == "Buy"
        TradeSide.Sell.name() == "Sell"
    }

    def "should support ordinal values"() {
        expect: "enum ordinal values are consistent"
        TradeSide.Buy.ordinal() == 0
        TradeSide.Sell.ordinal() == 1
    }

    def "should support business logic scenarios"() {
        given: "trade side values for business validation"
        def buyOrder = TradeSide.Buy
        def sellOrder = TradeSide.Sell

        expect: "business logic can differentiate between buy and sell"
        buyOrder != sellOrder
        
        and: "can be used in conditional logic"
        buyOrder == TradeSide.Buy
        sellOrder == TradeSide.Sell
        
        and: "can be used in switch-like scenarios"
        getBuySellDescription(buyOrder) == "Purchase order"
        getBuySellDescription(sellOrder) == "Sale order"
    }

    def "should maintain enum immutability"() {
        given: "enum references"
        def buy1 = TradeSide.Buy
        def buy2 = TradeSide.Buy
        def sell1 = TradeSide.Sell
        def sell2 = TradeSide.Sell

        expect: "same enum values reference the same object"
        buy1.is(buy2)
        sell1.is(sell2)
        !buy1.is(sell1)
    }

    def "should support collections operations"() {
        given: "a list of trade sides"
        def tradeSides = [TradeSide.Buy, TradeSide.Sell, TradeSide.Buy]

        expect: "collection operations work correctly"
        tradeSides.size() == 3
        tradeSides.count { it == TradeSide.Buy } == 2
        tradeSides.count { it == TradeSide.Sell } == 1
        tradeSides.contains(TradeSide.Buy)
        tradeSides.contains(TradeSide.Sell)
    }

    def "should support enum in maps"() {
        given: "a map with TradeSide keys"
        def tradeDescriptions = [
            (TradeSide.Buy): "Buying securities",
            (TradeSide.Sell): "Selling securities"
        ]

        expect: "map operations work correctly"
        tradeDescriptions[TradeSide.Buy] == "Buying securities"
        tradeDescriptions[TradeSide.Sell] == "Selling securities"
        tradeDescriptions.size() == 2
    }

    // Helper method for business logic testing
    private String getBuySellDescription(TradeSide side) {
        switch (side) {
            case TradeSide.Buy:
                return "Purchase order"
            case TradeSide.Sell:
                return "Sale order"
            default:
                return "Unknown order type"
        }
    }
}