package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("Security Domain Model Tests")
@Subject(Security)
class SecuritySpec extends Specification {

    def "should create Security with default constructor"() {
        when: "creating Security with default constructor"
        def security = new Security()

        then: "all fields should be null"
        security.getTicker() == null
        security.getcompanyName() == null
    }

    @Unroll
    def "should create Security with parameterized constructor for ticker '#ticker' and company '#companyName'"() {
        when: "creating Security with parameterized constructor"
        def security = new Security(ticker, companyName)

        then: "all fields should be set correctly"
        security.getTicker() == ticker
        security.getcompanyName() == companyName

        where:
        ticker           | companyName
        "AAPL"           | "Apple Inc."
        "GOOGL"          | "Alphabet Inc."
        "MSFT"           | "Microsoft Corporation"
        "TSLA"           | "Tesla, Inc."
        "AMZN"           | "Amazon.com, Inc."
        "A"              | "Single Letter Company"
        "VERYLONGTICKER" | "Very Long Company Name With Many Words"
        ""               | ""
        null             | null
        "TEST"           | "Test Company !@#\$%"
    }

    def "should handle null values in constructor gracefully"() {
        when: "creating Security with null values"
        def security = new Security(null, null)

        then: "object should be created with null values"
        security.getTicker() == null
        security.getcompanyName() == null
    }

    @Unroll
    def "should provide correct getter values for #testCase"() {
        given: "a Security with specific values"
        def security = new Security(ticker, companyName)

        expect: "getters return correct values"
        security.getTicker() == ticker
        security.getcompanyName() == companyName

        where:
        testCase           | ticker  | companyName
        "tech stock"       | "AAPL"  | "Apple Inc."
        "search engine"    | "GOOGL" | "Alphabet Inc."
        "cloud provider"   | "MSFT"  | "Microsoft Corporation"
        "electric vehicle" | "TSLA"  | "Tesla, Inc."
        "e-commerce"       | "AMZN"  | "Amazon.com, Inc."
        "short ticker"     | "A"     | "Agilent Technologies"
        "empty values"     | ""      | ""
        "null values"      | null    | null
    }

    def "should handle edge case values"() {
        given: "Security with edge case values"
        def security = new Security("EDGE", "Edge Case Company Name With Very Long Description")

        expect: "edge values are handled correctly"
        security.getTicker() == "EDGE"
        security.getcompanyName().length() > 30
    }

    def "should maintain immutability of constructor-set values"() {
        given: "a Security created with specific values"
        def originalTicker = "IMMUTABLE"
        def originalCompanyName = "Immutable Company"
        def security = new Security(originalTicker, originalCompanyName)

        when: "attempting to modify the original variables"
        originalTicker = "MODIFIED"
        originalCompanyName = "Modified Company"

        then: "Security values remain unchanged"
        security.getTicker() == "IMMUTABLE"
        security.getcompanyName() == "Immutable Company"
    }

    def "should support business validation scenarios"() {
        given: "various Security scenarios for business validation"
        def validTechStock = new Security("AAPL", "Apple Inc.")
        def validFinanceStock = new Security("JPM", "JPMorgan Chase & Co.")
        def invalidEmptyTicker = new Security("", "Company Without Ticker")
        def invalidNullTicker = new Security(null, "Company With Null Ticker")

        expect: "securities maintain their business-relevant properties"
        validTechStock.getTicker() != null
        validTechStock.getTicker().length() > 0
        validTechStock.getcompanyName() != null
        validTechStock.getcompanyName().contains("Inc.")

        validFinanceStock.getTicker() == "JPM"
        validFinanceStock.getcompanyName().contains("Chase")

        invalidEmptyTicker.getTicker() == ""
        invalidNullTicker.getTicker() == null
    }

    def "should handle various ticker formats"() {
        expect: "Security handles different ticker formats"
        def shortTicker = new Security("A", "Agilent Technologies")
        def longTicker = new Security("VERYLONGTICKER", "Very Long Ticker Company")
        def numericTicker = new Security("123", "Numeric Ticker Company")
        def specialCharTicker = new Security("A-B", "Hyphenated Ticker Company")

        shortTicker.getTicker() == "A"
        longTicker.getTicker().length() > 10
        numericTicker.getTicker() == "123"
        specialCharTicker.getTicker().contains("-")
    }

    def "should handle various company name formats"() {
        expect: "Security handles different company name formats"
        def shortName = new Security("SHORT", "ABC")
        def longName = new Security("LONG", "Very Long Company Name With Multiple Words And Descriptions")
        def specialChars = new Security("SPECIAL", "Company & Co., Inc.")
        def numbersInName = new Security("NUMBERS", "Company123 Ltd.")

        shortName.getcompanyName() == "ABC"
        longName.getcompanyName().length() > 50
        specialChars.getcompanyName().contains("&")
        numbersInName.getcompanyName().contains("123")
    }

    def "should maintain independence between instances"() {
        given: "two Security instances"
        def security1 = new Security("AAPL", "Apple Inc.")
        def security2 = new Security("GOOGL", "Alphabet Inc.")

        expect: "instances should maintain their own values"
        security1.getTicker() == "AAPL"
        security1.getcompanyName() == "Apple Inc."

        security2.getTicker() == "GOOGL"
        security2.getcompanyName() == "Alphabet Inc."

        and: "modifying one should not affect the other"
        security1.getTicker() != security2.getTicker()
        security1.getcompanyName() != security2.getcompanyName()
    }

    def "should support typical stock market scenarios"() {
        expect: "Security supports common stock market scenarios"
        def techStock = new Security("AAPL", "Apple Inc.")
        def financeStock = new Security("JPM", "JPMorgan Chase & Co.")
        def etf = new Security("SPY", "SPDR S&P 500 ETF Trust")
        def cryptocurrency = new Security("BTC", "Bitcoin")

        // Tech stock validation
        techStock.getTicker().length() <= 5
        techStock.getcompanyName().contains("Inc.")

        // Finance stock validation
        financeStock.getTicker() == "JPM"
        financeStock.getcompanyName().contains("Chase")

        // ETF validation
        etf.getTicker() == "SPY"
        etf.getcompanyName().contains("ETF")

        // Cryptocurrency validation
        cryptocurrency.getTicker() == "BTC"
        cryptocurrency.getcompanyName() == "Bitcoin"
    }

    def "should handle constructor parameter edge cases"() {
        expect: "different constructor parameter combinations work correctly"
        def emptyTicker = new Security("", "Empty Ticker Company")
        def emptyCompany = new Security("EMPTY", "")
        def bothEmpty = new Security("", "")
        def bothNull = new Security(null, null)
        def mixedNull = new Security("TICKER", null)

        emptyTicker.getTicker() == ""
        emptyCompany.getcompanyName() == ""
        bothEmpty.getTicker() == ""
        bothEmpty.getcompanyName() == ""
        bothNull.getTicker() == null
        bothNull.getcompanyName() == null
        mixedNull.getTicker() == "TICKER"
        mixedNull.getcompanyName() == null
    }

    def "should support real-world stock examples"() {
        expect: "Security works with real stock examples"
        def stocks = [new Security("AAPL", "Apple Inc."),
                      new Security("GOOGL", "Alphabet Inc."),
                      new Security("MSFT", "Microsoft Corporation"),
                      new Security("AMZN", "Amazon.com, Inc."),
                      new Security("TSLA", "Tesla, Inc."),
                      new Security("META", "Meta Platforms, Inc."),
                      new Security("NVDA", "NVIDIA Corporation"),
                      new Security("NFLX", "Netflix, Inc.")]

        stocks.every { stock -> stock.getTicker() != null && stock.getTicker().length() >= 1 && stock.getTicker().length() <= 5 && stock.getcompanyName() != null && stock.getcompanyName().length() > 0
        }
    }
}