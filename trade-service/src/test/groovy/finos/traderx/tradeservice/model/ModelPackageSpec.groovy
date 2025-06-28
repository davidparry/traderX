package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("Model Package Integration Tests")
@Subject([Account, Security, TradeOrder, TradeRequest, TradeResponse, TradeSide, TradeState])
class ModelPackageSpec extends Specification {

    def "should provide complete domain model for trading system"() {
        expect: "all core domain models are available"
        Account != null
        Security != null
        TradeOrder != null
        TradeRequest != null
        TradeResponse != null
        TradeSide != null
        TradeState != null
    }

    def "should maintain package consistency"() {
        expect: "all model classes are in the same package"
        def packageName = "finos.traderx.tradeservice.model"
        Account.getPackage().getName() == packageName
        Security.getPackage().getName() == packageName
        TradeOrder.getPackage().getName() == packageName
        TradeRequest.getPackage().getName() == packageName
        TradeResponse.getPackage().getName() == packageName
        TradeSide.getPackage().getName() == packageName
        TradeState.getPackage().getName() == packageName
    }

    def "should support complete trade lifecycle modeling"() {
        given: "domain objects for a complete trade"
        def account = new Account(1001, "John Doe Trading Account")
        def security = new Security("AAPL", "Apple Inc.")
        
        def tradeRequest = new TradeRequest()
        tradeRequest.setAccountId(account.getid())
        tradeRequest.setSecurity(security.getTicker())
        tradeRequest.setSide(TradeSide.Buy)
        tradeRequest.setQuantity(100)

        when: "creating trade order from request"
        def tradeOrder = new TradeOrder(
            "trade-${System.currentTimeMillis()}",
            tradeRequest.getAccountId(),
            tradeRequest.getSecurity(),
            tradeRequest.getSide(),
            tradeRequest.getQuantity()
        )

        then: "trade order should be properly created"
        tradeOrder.getAccountId() == account.getid()
        tradeOrder.getSecurity() == security.getTicker()
        tradeOrder.getSide() == TradeSide.Buy
        tradeOrder.getQuantity() == 100

        when: "creating successful trade response"
        def response = TradeResponse.success(tradeOrder.getId())

        then: "response should indicate success"
        response.isSuccess()
        response.getId() == tradeOrder.getId()
        response.getErrorMessage() == null
    }

    @Unroll
    def "should support different trading scenarios: #scenario"() {
        given: "account and security for trading"
        def account = new Account(accountId, accountName)
        def security = new Security(ticker, companyName)

        when: "creating trade request"
        def request = new TradeRequest()
        request.setAccountId(account.getid())
        request.setSecurity(security.getTicker())
        request.setSide(side)
        request.setQuantity(quantity)

        and: "creating trade order"
        def order = new TradeOrder("trade-${scenario}", account.getid(), security.getTicker(), side, quantity)

        then: "trade should be properly modeled"
        request.getAccountId() == accountId
        request.getSecurity() == ticker
        request.getSide() == side
        request.getQuantity() == quantity

        order.getAccountId() == accountId
        order.getSecurity() == ticker
        order.getSide() == side
        order.getQuantity() == quantity

        where:
        scenario              | accountId | accountName           | ticker | companyName        | side           | quantity
        "retail-buy"          | 1001      | "John Doe"           | "AAPL" | "Apple Inc."       | TradeSide.Buy  | 100
        "retail-sell"         | 1002      | "Jane Smith"         | "GOOGL"| "Alphabet Inc."    | TradeSide.Sell | 50
        "institutional-buy"   | 5001      | "Pension Fund ABC"   | "MSFT" | "Microsoft Corp."  | TradeSide.Buy  | 10000
        "institutional-sell"  | 5002      | "Hedge Fund XYZ"     | "TSLA" | "Tesla Inc."       | TradeSide.Sell | 5000
        "small-trade"         | 1003      | "Small Investor"     | "AMD"  | "AMD Inc."         | TradeSide.Buy  | 10
        "large-trade"         | 5003      | "Large Institution"  | "NVDA" | "NVIDIA Corp."     | TradeSide.Sell | 50000
    }

    def "should handle trade state transitions"() {
        expect: "all trade states are available"
        TradeState.values().contains(TradeState.New)
        TradeState.values().contains(TradeState.Processing)
        TradeState.values().contains(TradeState.Settled)
        TradeState.values().contains(TradeState.Cancelled)

        and: "trade states represent typical workflow"
        TradeState.values().length == 4
    }

    def "should handle trade sides correctly"() {
        expect: "both trade sides are available"
        TradeSide.values().contains(TradeSide.Buy)
        TradeSide.values().contains(TradeSide.Sell)
        TradeSide.values().length == 2
    }

    def "should support error handling in trade responses"() {
        given: "various error scenarios"
        def validationError = "Invalid quantity: must be positive"
        def accountError = "Account not found"
        def securityError = "Security not tradeable"

        when: "creating error responses"
        def validationResponse = TradeResponse.error(validationError)
        def accountResponse = TradeResponse.error(accountError)
        def securityResponse = TradeResponse.error(securityError)

        then: "error responses should be properly created"
        !validationResponse.isSuccess()
        validationResponse.getErrorMessage() == validationError
        validationResponse.getId() == null

        !accountResponse.isSuccess()
        accountResponse.getErrorMessage() == accountError

        !securityResponse.isSuccess()
        securityResponse.getErrorMessage() == securityError
    }

    def "should support account modeling for different account types"() {
        given: "different types of accounts"
        def retailAccount = new Account(1001, "John Doe")
        def corporateAccount = new Account(2001, "ABC Corporation")
        def institutionalAccount = new Account(5001, "Pension Fund XYZ")

        expect: "accounts should be properly modeled"
        retailAccount.getid() == 1001
        retailAccount.getdisplayName() == "John Doe"

        corporateAccount.getid() == 2001
        corporateAccount.getdisplayName() == "ABC Corporation"

        institutionalAccount.getid() == 5001
        institutionalAccount.getdisplayName() == "Pension Fund XYZ"
    }

    def "should support security modeling for different securities"() {
        given: "different types of securities"
        def techStock = new Security("AAPL", "Apple Inc.")
        def bankStock = new Security("JPM", "JPMorgan Chase & Co.")
        def etf = new Security("SPY", "SPDR S&P 500 ETF Trust")

        expect: "securities should be properly modeled"
        techStock.getTicker() == "AAPL"
        techStock.getcompanyName() == "Apple Inc."

        bankStock.getTicker() == "JPM"
        bankStock.getcompanyName() == "JPMorgan Chase & Co."

        etf.getTicker() == "SPY"
        etf.getcompanyName() == "SPDR S&P 500 ETF Trust"
    }

    def "should support trade order modeling with all required fields"() {
        given: "trade order with all fields"
        def tradeOrder = new TradeOrder("trade-123", 1001, "AAPL", TradeSide.Buy, 100)

        expect: "all fields should be accessible"
        tradeOrder.getId() == "trade-123"
        tradeOrder.getAccountId() == 1001
        tradeOrder.getSecurity() == "AAPL"
        tradeOrder.getSide() == TradeSide.Buy
        tradeOrder.getQuantity() == 100
        tradeOrder.getState() == null // Initially null
    }

    def "should support trade request validation scenarios"() {
        given: "trade requests with various data"
        def validRequest = new TradeRequest()
        validRequest.setAccountId(1001)
        validRequest.setSecurity("AAPL")
        validRequest.setSide(TradeSide.Buy)
        validRequest.setQuantity(100)

        def zeroQuantityRequest = new TradeRequest()
        zeroQuantityRequest.setAccountId(1001)
        zeroQuantityRequest.setSecurity("AAPL")
        zeroQuantityRequest.setSide(TradeSide.Buy)
        zeroQuantityRequest.setQuantity(0)

        expect: "requests should hold the data correctly"
        validRequest.getAccountId() == 1001
        validRequest.getSecurity() == "AAPL"
        validRequest.getSide() == TradeSide.Buy
        validRequest.getQuantity() == 100

        zeroQuantityRequest.getQuantity() == 0
    }

    def "should support complex trading workflows"() {
        given: "a complex trading scenario"
        def account = new Account(1001, "Active Trader")
        def security1 = new Security("AAPL", "Apple Inc.")
        def security2 = new Security("GOOGL", "Alphabet Inc.")

        when: "creating multiple trade requests"
        def buyApple = new TradeRequest()
        buyApple.setAccountId(account.getid())
        buyApple.setSecurity(security1.getTicker())
        buyApple.setSide(TradeSide.Buy)
        buyApple.setQuantity(100)

        def sellGoogle = new TradeRequest()
        sellGoogle.setAccountId(account.getid())
        sellGoogle.setSecurity(security2.getTicker())
        sellGoogle.setSide(TradeSide.Sell)
        sellGoogle.setQuantity(50)

        and: "creating corresponding trade orders"
        def appleOrder = new TradeOrder("trade-001", buyApple.getAccountId(), buyApple.getSecurity(), buyApple.getSide(), buyApple.getQuantity())
        def googleOrder = new TradeOrder("trade-002", sellGoogle.getAccountId(), sellGoogle.getSecurity(), sellGoogle.getSide(), sellGoogle.getQuantity())

        then: "all trades should be properly modeled"
        appleOrder.getAccountId() == account.getid()
        appleOrder.getSecurity() == security1.getTicker()
        appleOrder.getSide() == TradeSide.Buy

        googleOrder.getAccountId() == account.getid()
        googleOrder.getSecurity() == security2.getTicker()
        googleOrder.getSide() == TradeSide.Sell

        and: "responses can be created for both"
        def appleResponse = TradeResponse.success(appleOrder.getId())
        def googleResponse = TradeResponse.success(googleOrder.getId())

        appleResponse.isSuccess()
        googleResponse.isSuccess()
    }

    def "should handle edge cases in model objects"() {
        expect: "models handle edge cases gracefully"
        // Account with null values
        def nullAccount = new Account(null, null)
        nullAccount.getid() == null
        nullAccount.getdisplayName() == null

        // Security with empty strings
        def emptySecurity = new Security("", "")
        emptySecurity.getTicker() == ""
        emptySecurity.getcompanyName() == ""

        // Trade request with null security
        def nullSecurityRequest = new TradeRequest()
        nullSecurityRequest.setSecurity(null)
        nullSecurityRequest.getSecurity() == null
    }

    def "should support model object interactions"() {
        given: "related model objects"
        def account = new Account(1001, "Test Account")
        def security = new Security("TEST", "Test Company")

        when: "creating trade request using account and security data"
        def request = new TradeRequest()
        request.setAccountId(account.getid())
        request.setSecurity(security.getTicker())
        request.setSide(TradeSide.Buy)
        request.setQuantity(100)

        and: "creating trade order from request"
        def order = new TradeOrder(
            "trade-test",
            request.getAccountId(),
            request.getSecurity(),
            request.getSide(),
            request.getQuantity()
        )

        then: "objects should interact correctly"
        order.getAccountId() == account.getid()
        order.getSecurity() == security.getTicker()
        order.getSide() == request.getSide()
        order.getQuantity() == request.getQuantity()
    }

    def "should support business rule validation through model design"() {
        expect: "models support business validation"
        // Positive account IDs for valid accounts
        def validAccount = new Account(1001, "Valid Account")
        validAccount.getid() > 0

        // Non-empty security tickers
        def validSecurity = new Security("AAPL", "Apple Inc.")
        validSecurity.getTicker() != null
        validSecurity.getTicker().length() > 0

        // Positive quantities for valid trades
        def validRequest = new TradeRequest()
        validRequest.setQuantity(100)
        validRequest.getQuantity() > 0

        // Valid trade sides
        TradeSide.values().every { it != null }
        TradeSide.values().length == 2

        // Valid trade states
        TradeState.values().every { it != null }
        TradeState.values().length == 4
    }

    def "should maintain model consistency across package"() {
        given: "instances of all model classes"
        def account = new Account(1001, "Test Account")
        def security = new Security("TEST", "Test Security")
        def request = new TradeRequest()
        def order = new TradeOrder("test-order", 1001, "TEST", TradeSide.Buy, 100)
        def response = TradeResponse.success("test-order")

        expect: "all models work together consistently"
        account != null
        security != null
        request != null
        order != null
        response != null

        and: "models can be used in combination"
        request.setAccountId(account.getid())
        request.setSecurity(security.getTicker())
        request.setSide(TradeSide.Buy)
        request.setQuantity(100)

        request.getAccountId() == account.getid()
        request.getSecurity() == security.getTicker()
    }
}