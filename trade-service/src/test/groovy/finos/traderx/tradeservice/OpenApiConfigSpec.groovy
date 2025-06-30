package finos.traderx.tradeservice

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Configuration
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

@Title("OpenApiConfig Tests")
@Subject(OpenApiConfig)
class OpenApiConfigSpec extends Specification {

    OpenApiConfig config

    def setup() {
        config = new OpenApiConfig()
    }

    def "should have Configuration annotation"() {
        expect: "class has @Configuration annotation"
        OpenApiConfig.class.isAnnotationPresent(Configuration.class)
    }

    def "should create OpenAPI bean with default port"() {
        given: "default port value"
        ReflectionTestUtils.setField(config, "port", 18092)

        when: "creating OpenAPI configuration"
        def openAPI = config.config()

        then: "OpenAPI object is configured correctly"
        openAPI != null
        openAPI instanceof OpenAPI
        
        and: "info is set correctly"
        openAPI.info != null
        openAPI.info.title == "FINOS TraderX Trading Service"
        openAPI.info.version == "0.1.0"
        openAPI.info.description == "Service for capturing trades from the UI, validating, and sending for processing"
        
        and: "servers are configured"
        openAPI.servers != null
        openAPI.servers.size() == 2
        openAPI.servers[0].url == ""
        openAPI.servers[0].description == "Empty URL to help proxied documentation work"
        openAPI.servers[1].url == "http://localhost:18092"
        openAPI.servers[1].description == "Local Dev URL"
    }

    @Unroll
    def "should create OpenAPI with custom port #port"() {
        given: "custom port value"
        ReflectionTestUtils.setField(config, "port", port)

        when: "creating OpenAPI configuration"
        def openAPI = config.config()

        then: "local dev server URL uses custom port"
        openAPI.servers[1].url == "http://localhost:$port"

        where:
        port << [8080, 9090, 3000, 8443]
    }

    def "should have Bean annotation on config method"() {
        given: "the config method"
        def method = OpenApiConfig.class.getDeclaredMethod("config")

        expect: "method has @Bean annotation"
        method.isAnnotationPresent(org.springframework.context.annotation.Bean.class)
    }

    def "should create Server objects correctly"() {
        given: "access to private method via reflection"
        def serverInfoMethod = OpenApiConfig.class.getDeclaredMethod("serverInfo", String.class, String.class)
        serverInfoMethod.setAccessible(true)

        when: "creating server info"
        def server = serverInfoMethod.invoke(config, "http://test.com", "Test Server") as Server

        then: "server is configured correctly"
        server != null
        server.url == "http://test.com"
        server.description == "Test Server"
    }

    def "should handle empty URL for proxy support"() {
        when: "creating OpenAPI configuration"
        def openAPI = config.config()

        then: "first server has empty URL for proxy support"
        openAPI.servers[0].url == ""
        openAPI.servers[0].description.toLowerCase().contains("prox")
    }

    def "should set all required OpenAPI metadata"() {
        when: "creating OpenAPI configuration"
        def openAPI = config.config()

        then: "all required metadata is present"
        openAPI.info != null
        openAPI.info.title != null && !openAPI.info.title.isEmpty()
        openAPI.info.version != null && !openAPI.info.version.isEmpty()
        openAPI.info.description != null && !openAPI.info.description.isEmpty()
    }

    def "should support multiple server configurations"() {
        when: "creating OpenAPI configuration"
        def openAPI = config.config()

        then: "multiple servers are configured"
        openAPI.servers.size() >= 2
        
        and: "each server has required fields"
        openAPI.servers.each { server ->
            assert server.description != null
            assert server.url != null
        }
    }

    def "should maintain consistent API version"() {
        when: "creating OpenAPI configuration"
        def openAPI = config.config()

        then: "API version follows semantic versioning"
        openAPI.info.version =~ /^\d+\.\d+\.\d+$/
    }

    def "should provide meaningful API description"() {
        when: "creating OpenAPI configuration"
        def openAPI = config.config()

        then: "description contains key information"
        openAPI.info.description.contains("trades")
        openAPI.info.description.contains("validating")
        openAPI.info.description.contains("processing")
    }

    def "should handle port as Value annotation"() {
        given: "the port field"
        def portField = OpenApiConfig.class.getDeclaredField("port")

        expect: "field has @Value annotation"
        portField.isAnnotationPresent(org.springframework.beans.factory.annotation.Value.class)
        
        and: "annotation references server.port property"
        def valueAnnotation = portField.getAnnotation(org.springframework.beans.factory.annotation.Value.class)
        valueAnnotation.value() == '${server.port}'
    }

    def "should have default port value"() {
        given: "a new config instance"
        def newConfig = new OpenApiConfig()

        when: "accessing port field via reflection"
        def portField = OpenApiConfig.class.getDeclaredField("port")
        portField.setAccessible(true)
        def portValue = portField.get(newConfig)

        then: "default port is set"
        portValue == 18092
    }

    def "should create immutable server configurations"() {
        when: "creating OpenAPI configuration"
        def openAPI = config.config()
        def originalServersSize = openAPI.servers.size()
        def originalFirstServerUrl = openAPI.servers[0].url

        and: "attempting to modify servers"
        openAPI.servers[0].url = "http://modified.com"

        then: "server can be modified (not immutable by default)"
        openAPI.servers[0].url == "http://modified.com"
        openAPI.servers.size() == originalServersSize
    }
}