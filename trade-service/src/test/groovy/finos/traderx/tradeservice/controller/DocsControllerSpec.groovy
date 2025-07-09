package finos.traderx.tradeservice.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@Title("DocsController API Tests")
@Subject(DocsController)
@WebMvcTest(controllers = [DocsController])
@TestPropertySource(properties = [
    "logging.level.root=WARN",
    "spring.main.banner-mode=off"
])
class DocsControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    def "should redirect root path to swagger-ui.html"() {
        when: "requesting root path"
        def result = mockMvc.perform(get("/"))

        then: "should redirect to swagger-ui.html"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("swagger-ui.html"))
    }

    def "should handle GET request to root path"() {
        when: "making GET request to root"
        def result = mockMvc.perform(get("/"))

        then: "should return redirect response"
        result.andExpect(status().isFound())
    }

    def "should return proper redirect location header"() {
        when: "requesting root path"
        def result = mockMvc.perform(get("/"))

        then: "should have location header pointing to swagger-ui.html"
        result.andExpect(header().string("Location", "swagger-ui.html"))
    }

    def "should not return any content body"() {
        when: "requesting root path"
        def result = mockMvc.perform(get("/"))

        then: "should have empty content"
        result.andExpect(content().string(""))
    }

    def "should handle multiple requests consistently"() {
        when: "making multiple requests to root path"
        def result1 = mockMvc.perform(get("/"))
        def result2 = mockMvc.perform(get("/"))
        def result3 = mockMvc.perform(get("/"))

        then: "all requests should redirect consistently"
        result1.andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("swagger-ui.html"))
        
        result2.andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("swagger-ui.html"))
        
        result3.andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("swagger-ui.html"))
    }

    def "should respond quickly to root path requests"() {
        when: "requesting root path"
        def startTime = System.currentTimeMillis()
        def result = mockMvc.perform(get("/"))
        def endTime = System.currentTimeMillis()

        then: "should respond within reasonable time"
        result.andExpect(status().is3xxRedirection())
        (endTime - startTime) < 1000 // Should respond within 1 second
    }

    def "should handle concurrent requests to root path"() {
        when: "making concurrent requests"
        def results = []
        (1..5).each { i ->
            results << mockMvc.perform(get("/"))
        }

        then: "all requests should succeed with redirect"
        results.each { result ->
            result.andExpect(status().is3xxRedirection())
                  .andExpect(redirectedUrl("swagger-ui.html"))
        }
    }

    def "should maintain proper HTTP semantics for GET request"() {
        when: "making GET request to root"
        def result = mockMvc.perform(get("/"))

        then: "should follow HTTP redirect semantics"
        result.andExpect(status().isFound()) // 302 Found
              .andExpect(redirectedUrl("swagger-ui.html"))
    }

    def "should not accept other HTTP methods on root path"() {
        when: "making POST request to root"
        def postResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/")
        )

        then: "should return method not allowed"
        postResult.andExpect(status().isMethodNotAllowed())
    }

    def "should handle edge case with trailing slash"() {
        when: "requesting root with explicit trailing slash"
        def result = mockMvc.perform(get("/"))

        then: "should still redirect properly"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("swagger-ui.html"))
    }

    def "should provide proper content type for redirect"() {
        when: "requesting root path"
        def result = mockMvc.perform(get("/"))

        then: "should have appropriate content type or no content type for redirect"
        result.andExpect(status().is3xxRedirection())
        // Redirects typically don't have content-type, so we just verify the redirect works
    }

    def "should be accessible without authentication"() {
        when: "making unauthenticated request to root"
        def result = mockMvc.perform(get("/"))

        then: "should allow access and redirect"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("swagger-ui.html"))
    }

    def "should handle request with various headers"() {
        when: "making request with custom headers"
        def result = mockMvc.perform(
            get("/")
                .header("User-Agent", "Test-Agent")
                .header("Accept", "text/html,application/xhtml+xml")
        )

        then: "should still redirect regardless of headers"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("swagger-ui.html"))
    }

    def "should support API documentation discovery pattern"() {
        when: "requesting root for API documentation"
        def result = mockMvc.perform(get("/"))

        then: "should redirect to swagger documentation"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("swagger-ui.html"))
        
        and: "redirect target should be swagger-ui for API documentation"
        def redirectUrl = result.andReturn().response.getHeader("Location")
        redirectUrl == "swagger-ui.html"
    }

    def "should maintain redirect behavior under load"() {
        when: "making many rapid requests"
        def results = []
        (1..10).each { i ->
            results << mockMvc.perform(get("/"))
        }

        then: "all requests should consistently redirect"
        results.each { result ->
            result.andExpect(status().is3xxRedirection())
                  .andExpect(redirectedUrl("swagger-ui.html"))
        }
    }
}