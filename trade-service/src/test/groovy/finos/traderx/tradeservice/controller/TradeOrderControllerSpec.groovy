package finos.traderx.tradeservice.controller

import finos.traderx.messaging.PubSubException
import finos.traderx.messaging.Publisher
import finos.traderx.tradeservice.exceptions.ResourceNotFoundException
import finos.traderx.tradeservice.model.Account
import finos.traderx.tradeservice.model.Security
import finos.traderx.tradeservice.model.TradeOrder
import finos.traderx.tradeservice.model.TradeSide
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("TradeOrderController Tests")
@Subject(TradeOrderController)
class TradeOrderControllerSpec extends Specification {

    TradeOrderController controller
    Publisher<TradeOrder> mockPublisher
    RestTemplate mockRestTemplate

    def setup() {
        controller = new TradeOrderController()
        mockPublisher = Mock(Publisher)
        mockRestTemplate = Mock(RestTemplate)
        
        // Inject mocks using reflection
        ReflectionTestUtils.setField(controller, "tradePublisher", mockPublisher)
        ReflectionTestUtils.setField(controller, "restTemplate", mockRestTemplate)
        ReflectionTestUtils.setField(controller, "referenceDataServiceAddress", "http://ref-data-service")
        ReflectionTestUtils.setField(controller, "accountServiceAddress", "http://account-service")
    }

    def "should create valid trade order successfully"() {
        given: "a valid trade order"
        def tradeOrder = new TradeOrder("trade-123", 1001, "AAPL", TradeSide.Buy, 100)
        
        and: "reference data service returns valid security"
        def security = new Security("AAPL", "Apple Inc.")
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/AAPL", Security.class) >> 
            new ResponseEntity<>(security, HttpStatus.OK)
        
        and: "account service returns valid account"
        def account = new Account(1001, "John Doe")
        mockRestTemplate.getForEntity("http://account-service//account/1001", Account.class) >> 
            new ResponseEntity<>(account, HttpStatus.OK)

        when: "creating trade order"
        def response = controller.createTradeOrder(tradeOrder)

        then: "trade should be published"
        1 * mockPublisher.publish("/trades", tradeOrder)
        
        and: "response should be OK with trade order"
        response.statusCode == HttpStatus.OK
        response.body == tradeOrder
    }

    def "should throw ResourceNotFoundException when security not found"() {
        given: "a trade order with invalid security"
        def tradeOrder = new TradeOrder("trade-123", 1001, "INVALID", TradeSide.Buy, 100)
        
        and: "reference data service returns 404"
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/INVALID", Security.class) >> 
            { throw new HttpClientErrorException(HttpStatus.NOT_FOUND) }

        when: "creating trade order"
        controller.createTradeOrder(tradeOrder)

        then: "ResourceNotFoundException should be thrown"
        def exception = thrown(ResourceNotFoundException)
        exception.message == "INVALID not found in Reference data service."
        
        and: "trade should not be published"
        0 * mockPublisher.publish(_, _)
    }

    def "should throw ResourceNotFoundException when account not found"() {
        given: "a trade order with invalid account"
        def tradeOrder = new TradeOrder("trade-123", 9999, "AAPL", TradeSide.Buy, 100)
        
        and: "reference data service returns valid security"
        def security = new Security("AAPL", "Apple Inc.")
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/AAPL", Security.class) >> 
            new ResponseEntity<>(security, HttpStatus.OK)
        
        and: "account service returns 404"
        mockRestTemplate.getForEntity("http://account-service//account/9999", Account.class) >> 
            { throw new HttpClientErrorException(HttpStatus.NOT_FOUND) }

        when: "creating trade order"
        controller.createTradeOrder(tradeOrder)

        then: "ResourceNotFoundException should be thrown"
        def exception = thrown(ResourceNotFoundException)
        exception.message == "9999 not found in Account service."
        
        and: "trade should not be published"
        0 * mockPublisher.publish(_, _)
    }

    def "should handle PubSubException when publishing fails"() {
        given: "a valid trade order"
        def tradeOrder = new TradeOrder("trade-123", 1001, "AAPL", TradeSide.Buy, 100)
        
        and: "services return valid data"
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/AAPL", Security.class) >> 
            new ResponseEntity<>(new Security("AAPL", "Apple Inc."), HttpStatus.OK)
        mockRestTemplate.getForEntity("http://account-service//account/1001", Account.class) >> 
            new ResponseEntity<>(new Account(1001, "John Doe"), HttpStatus.OK)
        
        and: "publisher throws exception"
        mockPublisher.publish("/trades", tradeOrder) >> { throw new PubSubException("Connection failed") }

        when: "creating trade order"
        controller.createTradeOrder(tradeOrder)

        then: "RuntimeException should be thrown"
        def exception = thrown(RuntimeException)
        exception.message == "Failed to publish trade order"
        exception.cause instanceof PubSubException
    }

    @Unroll
    def "should handle different HTTP errors for security validation: #statusCode"() {
        given: "a trade order"
        def tradeOrder = new TradeOrder("trade-123", 1001, "AAPL", TradeSide.Buy, 100)
        
        and: "reference data service returns error"
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/AAPL", Security.class) >> 
            { throw new HttpClientErrorException(statusCode) }

        when: "creating trade order"
        controller.createTradeOrder(tradeOrder)

        then: "ResourceNotFoundException should be thrown"
        thrown(ResourceNotFoundException)

        where:
        statusCode << [HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_REQUEST]
    }

    @Unroll
    def "should handle different HTTP errors for account validation: #statusCode"() {
        given: "a trade order"
        def tradeOrder = new TradeOrder("trade-123", 1001, "AAPL", TradeSide.Buy, 100)
        
        and: "reference data service returns valid security"
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/AAPL", Security.class) >> 
            new ResponseEntity<>(new Security("AAPL", "Apple Inc."), HttpStatus.OK)
        
        and: "account service returns error"
        mockRestTemplate.getForEntity("http://account-service//account/1001", Account.class) >> 
            { throw new HttpClientErrorException(statusCode) }

        when: "creating trade order"
        controller.createTradeOrder(tradeOrder)

        then: "ResourceNotFoundException should be thrown"
        thrown(ResourceNotFoundException)

        where:
        statusCode << [HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_REQUEST]
    }

    def "should handle null trade order"() {
        when: "creating trade order with null"
        controller.createTradeOrder(null)

        then: "NullPointerException should be thrown"
        thrown(NullPointerException)
    }

    def "should validate ticker with special characters"() {
        given: "a trade order with special ticker"
        def tradeOrder = new TradeOrder("trade-123", 1001, "BRK.B", TradeSide.Buy, 100)
        
        and: "reference data service returns valid security"
        def security = new Security("BRK.B", "Berkshire Hathaway")
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/BRK.B", Security.class) >> 
            new ResponseEntity<>(security, HttpStatus.OK)
        
        and: "account service returns valid account"
        def account = new Account(1001, "John Doe")
        mockRestTemplate.getForEntity("http://account-service//account/1001", Account.class) >> 
            new ResponseEntity<>(account, HttpStatus.OK)

        when: "creating trade order"
        def response = controller.createTradeOrder(tradeOrder)

        then: "trade should be published successfully"
        1 * mockPublisher.publish("/trades", tradeOrder)
        response.statusCode == HttpStatus.OK
    }

    def "should handle both sell and buy orders"() {
        given: "sell and buy orders"
        def buyOrder = new TradeOrder("buy-123", 1001, "AAPL", TradeSide.Buy, 100)
        def sellOrder = new TradeOrder("sell-123", 1001, "AAPL", TradeSide.Sell, 50)
        
        and: "services return valid data"
        mockRestTemplate.getForEntity(_, Security.class) >> 
            new ResponseEntity<>(new Security("AAPL", "Apple Inc."), HttpStatus.OK)
        mockRestTemplate.getForEntity(_, Account.class) >> 
            new ResponseEntity<>(new Account(1001, "John Doe"), HttpStatus.OK)

        when: "creating buy order"
        def buyResponse = controller.createTradeOrder(buyOrder)
        
        and: "creating sell order"
        def sellResponse = controller.createTradeOrder(sellOrder)

        then: "both orders should be published"
        1 * mockPublisher.publish("/trades", buyOrder)
        1 * mockPublisher.publish("/trades", sellOrder)
        
        and: "both responses should be OK"
        buyResponse.statusCode == HttpStatus.OK
        sellResponse.statusCode == HttpStatus.OK
    }

    def "should handle large quantity orders"() {
        given: "a large quantity order"
        def tradeOrder = new TradeOrder("trade-123", 1001, "AAPL", TradeSide.Buy, Integer.MAX_VALUE)
        
        and: "services return valid data"
        mockRestTemplate.getForEntity("http://ref-data-service//stocks/AAPL", Security.class) >> 
            new ResponseEntity<>(new Security("AAPL", "Apple Inc."), HttpStatus.OK)
        mockRestTemplate.getForEntity("http://account-service//account/1001", Account.class) >> 
            new ResponseEntity<>(new Account(1001, "John Doe"), HttpStatus.OK)

        when: "creating trade order"
        def response = controller.createTradeOrder(tradeOrder)

        then: "trade should be published"
        1 * mockPublisher.publish("/trades", tradeOrder)
        response.statusCode == HttpStatus.OK
    }

    def "should handle service URLs with different configurations"() {
        given: "different service URLs"
        ReflectionTestUtils.setField(controller, "referenceDataServiceAddress", "https://prod-ref-data")
        ReflectionTestUtils.setField(controller, "accountServiceAddress", "https://prod-accounts")
        
        and: "a trade order"
        def tradeOrder = new TradeOrder("trade-123", 1001, "AAPL", TradeSide.Buy, 100)
        
        and: "services return valid data"
        mockRestTemplate.getForEntity("https://prod-ref-data//stocks/AAPL", Security.class) >> 
            new ResponseEntity<>(new Security("AAPL", "Apple Inc."), HttpStatus.OK)
        mockRestTemplate.getForEntity("https://prod-accounts//account/1001", Account.class) >> 
            new ResponseEntity<>(new Account(1001, "John Doe"), HttpStatus.OK)

        when: "creating trade order"
        def response = controller.createTradeOrder(tradeOrder)

        then: "trade should be published"
        1 * mockPublisher.publish("/trades", tradeOrder)
        response.statusCode == HttpStatus.OK
    }
}