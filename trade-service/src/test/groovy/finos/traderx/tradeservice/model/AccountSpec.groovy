package finos.traderx.tradeservice.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("Account Domain Model Tests")
@Subject(Account)
class AccountSpec extends Specification {

    def "should create Account with default constructor"() {
        when: "creating Account with default constructor"
        def account = new Account()

        then: "all fields should be null"
        account.getid() == null
        account.getdisplayName() == null
    }

    @Unroll
    def "should create Account with parameterized constructor for id #id and displayName '#displayName'"() {
        when: "creating Account with parameterized constructor"
        def account = new Account(id, displayName)

        then: "all fields should be set correctly"
        account.getid() == id
        account.getdisplayName() == displayName

        where:
        id   | displayName
        1    | "John Doe"
        100  | "Jane Smith"
        9999 | "Corporate Account"
        0    | "Test Account"
        -1   | "Invalid Account"
        null | null
        1001 | ""
        2002 | "Account with Special Characters !@#\$%"
    }

    def "should handle null values in constructor gracefully"() {
        when: "creating Account with null values"
        def account = new Account(null, null)

        then: "object should be created with null values"
        account.getid() == null
        account.getdisplayName() == null
    }

    @Unroll
    def "should provide correct getter values for account #testCase"() {
        given: "an Account with specific values"
        def account = new Account(id, displayName)

        expect: "getters return correct values"
        account.getid() == id
        account.getdisplayName() == displayName

        where:
        testCase              | id   | displayName
        "personal account"    | 1001 | "John Doe"
        "business account"    | 2002 | "ABC Corporation"
        "premium account"     | 3003 | "Premium Customer"
        "test account"        | 9999 | "Test Account"
        "empty display name"  | 1234 | ""
        "null display name"   | 5678 | null
        "zero id"            | 0    | "Zero ID Account"
        "negative id"        | -1   | "Negative ID Account"
    }

    def "should handle edge case values"() {
        given: "Account with edge case values"
        def account = new Account(Integer.MAX_VALUE, "Max Value Account")

        expect: "edge values are handled correctly"
        account.getid() == Integer.MAX_VALUE
        account.getdisplayName() == "Max Value Account"
    }

    def "should maintain immutability of constructor-set values"() {
        given: "an Account created with specific values"
        def originalId = 1001
        def originalDisplayName = "Original Name"
        def account = new Account(originalId, originalDisplayName)

        when: "attempting to modify the original variables"
        originalId = 9999
        originalDisplayName = "Modified Name"

        then: "Account values remain unchanged"
        account.getid() == 1001
        account.getdisplayName() == "Original Name"
    }

    def "should support business validation scenarios"() {
        given: "various Account scenarios for business validation"
        def validPersonalAccount = new Account(1001, "John Doe")
        def validCorporateAccount = new Account(2002, "ABC Corp")
        def invalidAccount = new Account(-1, "Invalid Account")
        def zeroIdAccount = new Account(0, "Zero ID")

        expect: "accounts maintain their business-relevant properties"
        validPersonalAccount.getid() > 0
        validPersonalAccount.getdisplayName() != null
        validPersonalAccount.getdisplayName().length() > 0

        validCorporateAccount.getid() > 0
        validCorporateAccount.getdisplayName() != null
        validCorporateAccount.getdisplayName().contains("Corp")

        invalidAccount.getid() < 0
        zeroIdAccount.getid() == 0
    }

    def "should handle various display name formats"() {
        expect: "Account handles different display name formats"
        def shortName = new Account(1, "A")
        def longName = new Account(2, "Very Long Account Display Name With Many Words")
        def specialChars = new Account(3, "Account!@#\$%^&*()")
        def numbersInName = new Account(4, "Account123")
        def spacesName = new Account(5, "   Spaces   ")

        shortName.getdisplayName() == "A"
        longName.getdisplayName().length() > 20
        specialChars.getdisplayName().contains("!@#")
        numbersInName.getdisplayName().contains("123")
        spacesName.getdisplayName().contains("   ")
    }

    def "should maintain independence between instances"() {
        given: "two Account instances"
        def account1 = new Account(1001, "Account One")
        def account2 = new Account(2002, "Account Two")

        expect: "instances should maintain their own values"
        account1.getid() == 1001
        account1.getdisplayName() == "Account One"

        account2.getid() == 2002
        account2.getdisplayName() == "Account Two"

        and: "modifying one should not affect the other"
        account1.getid() != account2.getid()
        account1.getdisplayName() != account2.getdisplayName()
    }

    def "should support typical business account scenarios"() {
        expect: "Account supports common business scenarios"
        def retailAccount = new Account(1001, "John Smith")
        def institutionalAccount = new Account(5001, "Pension Fund ABC")
        def testAccount = new Account(9999, "Test Account")

        // Retail account validation
        retailAccount.getid() >= 1000 && retailAccount.getid() < 5000
        retailAccount.getdisplayName().split(" ").length >= 2

        // Institutional account validation
        institutionalAccount.getid() >= 5000
        institutionalAccount.getdisplayName().length() > 10

        // Test account validation
        testAccount.getid() == 9999
        testAccount.getdisplayName().toLowerCase().contains("test")
    }

    def "should handle constructor parameter variations"() {
        expect: "different constructor parameter combinations work correctly"
        def positiveId = new Account(1, "Positive")
        def zeroId = new Account(0, "Zero")
        def negativeId = new Account(-1, "Negative")
        def maxId = new Account(Integer.MAX_VALUE, "Max")
        def minId = new Account(Integer.MIN_VALUE, "Min")

        positiveId.getid() == 1
        zeroId.getid() == 0
        negativeId.getid() == -1
        maxId.getid() == Integer.MAX_VALUE
        minId.getid() == Integer.MIN_VALUE
    }
}