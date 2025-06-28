package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("TradeState Enum Tests")
@Subject(TradeState)
class TradeStateSpec extends Specification {

    def "should have exactly four enum values"() {
        expect: "TradeState enum has New, Processing, Settled, and Cancelled values"
        TradeState.values().length == 4
        TradeState.values().contains(TradeState.New)
        TradeState.values().contains(TradeState.Processing)
        TradeState.values().contains(TradeState.Settled)
        TradeState.values().contains(TradeState.Cancelled)
    }

    @Unroll
    def "should support valueOf for '#enumName'"() {
        when: "getting enum by name"
        def result = TradeState.valueOf(enumName)

        then: "correct enum value is returned"
        result == expectedValue

        where:
        enumName    | expectedValue
        "New"       | TradeState.New
        "Processing"| TradeState.Processing
        "Settled"   | TradeState.Settled
        "Cancelled" | TradeState.Cancelled
    }

    def "should throw IllegalArgumentException for invalid enum name"() {
        when: "getting enum with invalid name"
        TradeState.valueOf("Invalid")

        then: "IllegalArgumentException is thrown"
        thrown(IllegalArgumentException)
    }

    def "should support enum comparison"() {
        expect: "enum values can be compared"
        TradeState.New == TradeState.New
        TradeState.Processing == TradeState.Processing
        TradeState.Settled == TradeState.Settled
        TradeState.Cancelled == TradeState.Cancelled
        
        TradeState.New != TradeState.Processing
        TradeState.Processing != TradeState.Settled
        TradeState.Settled != TradeState.Cancelled
    }

    def "should support toString conversion"() {
        expect: "enum values convert to string correctly"
        TradeState.New.toString() == "New"
        TradeState.Processing.toString() == "Processing"
        TradeState.Settled.toString() == "Settled"
        TradeState.Cancelled.toString() == "Cancelled"
    }

    def "should support name() method"() {
        expect: "enum name() method returns correct values"
        TradeState.New.name() == "New"
        TradeState.Processing.name() == "Processing"
        TradeState.Settled.name() == "Settled"
        TradeState.Cancelled.name() == "Cancelled"
    }

    def "should support ordinal values"() {
        expect: "enum ordinal values are consistent"
        TradeState.New.ordinal() == 0
        TradeState.Processing.ordinal() == 1
        TradeState.Settled.ordinal() == 2
        TradeState.Cancelled.ordinal() == 3
    }

    def "should support business logic scenarios"() {
        given: "trade state values for business validation"
        def newTrade = TradeState.New
        def processingTrade = TradeState.Processing
        def settledTrade = TradeState.Settled
        def cancelledTrade = TradeState.Cancelled

        expect: "business logic can differentiate between states"
        newTrade != processingTrade
        processingTrade != settledTrade
        settledTrade != cancelledTrade
        
        and: "can be used in workflow logic"
        isActiveState(newTrade)
        isActiveState(processingTrade)
        !isActiveState(settledTrade)
        !isActiveState(cancelledTrade)
        
        and: "can be used in state transition validation"
        canTransitionTo(newTrade, processingTrade)
        canTransitionTo(processingTrade, settledTrade)
        canTransitionTo(processingTrade, cancelledTrade)
        !canTransitionTo(settledTrade, newTrade)
        !canTransitionTo(cancelledTrade, processingTrade)
    }

    def "should maintain enum immutability"() {
        given: "enum references"
        def new1 = TradeState.New
        def new2 = TradeState.New
        def processing1 = TradeState.Processing
        def processing2 = TradeState.Processing

        expect: "same enum values reference the same object"
        new1.is(new2)
        processing1.is(processing2)
        !new1.is(processing1)
    }

    def "should support collections operations"() {
        given: "a list of trade states"
        def tradeStates = [TradeState.New, TradeState.Processing, TradeState.Settled, TradeState.New]

        expect: "collection operations work correctly"
        tradeStates.size() == 4
        tradeStates.count { it == TradeState.New } == 2
        tradeStates.count { it == TradeState.Processing } == 1
        tradeStates.count { it == TradeState.Settled } == 1
        tradeStates.contains(TradeState.New)
        tradeStates.contains(TradeState.Processing)
        tradeStates.contains(TradeState.Settled)
        !tradeStates.contains(TradeState.Cancelled)
    }

    def "should support enum in maps"() {
        given: "a map with TradeState keys"
        def stateDescriptions = [
            (TradeState.New): "Trade order received",
            (TradeState.Processing): "Trade being processed",
            (TradeState.Settled): "Trade completed successfully",
            (TradeState.Cancelled): "Trade was cancelled"
        ]

        expect: "map operations work correctly"
        stateDescriptions[TradeState.New] == "Trade order received"
        stateDescriptions[TradeState.Processing] == "Trade being processed"
        stateDescriptions[TradeState.Settled] == "Trade completed successfully"
        stateDescriptions[TradeState.Cancelled] == "Trade was cancelled"
        stateDescriptions.size() == 4
    }

    def "should support state workflow scenarios"() {
        expect: "state workflow logic works correctly"
        def initialStates = getInitialStates()
        def finalStates = getFinalStates()
        def activeStates = getActiveStates()

        initialStates.contains(TradeState.New)
        initialStates.size() == 1

        finalStates.contains(TradeState.Settled)
        finalStates.contains(TradeState.Cancelled)
        finalStates.size() == 2

        activeStates.contains(TradeState.New)
        activeStates.contains(TradeState.Processing)
        activeStates.size() == 2
    }

    // Helper methods for business logic testing
    private boolean isActiveState(TradeState state) {
        return state in [TradeState.New, TradeState.Processing]
    }

    private boolean canTransitionTo(TradeState from, TradeState to) {
        def validTransitions = [
            (TradeState.New): [TradeState.Processing, TradeState.Cancelled],
            (TradeState.Processing): [TradeState.Settled, TradeState.Cancelled],
            (TradeState.Settled): [],
            (TradeState.Cancelled): []
        ]
        return to in validTransitions[from]
    }

    private List<TradeState> getInitialStates() {
        return [TradeState.New]
    }

    private List<TradeState> getFinalStates() {
        return [TradeState.Settled, TradeState.Cancelled]
    }

    private List<TradeState> getActiveStates() {
        return [TradeState.New, TradeState.Processing]
    }
}