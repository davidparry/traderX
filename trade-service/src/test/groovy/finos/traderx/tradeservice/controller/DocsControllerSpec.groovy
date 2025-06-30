package finos.traderx.tradeservice.controller

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Title("DocsController Tests")
@Subject(DocsController)
class DocsControllerSpec extends Specification {

    DocsController controller

    def setup() {
        controller = new DocsController()
    }

    def "should redirect root path to swagger UI"() {
        when: "accessing root path"
        def result = controller.index()

        then: "should redirect to swagger-ui.html"
        result == "redirect:swagger-ui.html"
    }

    def "should be annotated as Controller"() {
        expect: "controller has @Controller annotation"
        controller.class.getAnnotation(org.springframework.stereotype.Controller) != null
    }

    def "index method should have RequestMapping annotation"() {
        given: "the index method"
        def method = controller.class.getDeclaredMethod("index")

        expect: "method has @RequestMapping annotation"
        method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping) != null
        
        and: "mapping is for root path"
        def annotation = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping)
        annotation.value() == ["/"] as String[]
    }

    def "should handle multiple calls consistently"() {
        expect: "multiple calls return same result"
        controller.index() == "redirect:swagger-ui.html"
        controller.index() == "redirect:swagger-ui.html"
        controller.index() == "redirect:swagger-ui.html"
    }

    def "should not throw exceptions"() {
        when: "calling index method"
        def result = controller.index()

        then: "no exceptions thrown"
        notThrown(Exception)
        
        and: "result is not null"
        result != null
    }

    def "should return Spring MVC redirect format"() {
        when: "calling index method"
        def result = controller.index()

        then: "result follows Spring redirect convention"
        result.startsWith("redirect:")
        !result.startsWith("forward:")
        !result.contains("http://")
        !result.contains("https://")
    }

    def "should redirect to correct Swagger UI endpoint"() {
        when: "calling index method"
        def result = controller.index()

        then: "redirects to swagger-ui.html"
        result == "redirect:swagger-ui.html"
        
        and: "not to other common documentation endpoints"
        result != "redirect:swagger-ui/index.html"
        result != "redirect:/swagger-ui.html"
        result != "redirect:api-docs"
        result != "redirect:v3/api-docs"
    }

    def "controller instance should be reusable"() {
        given: "a single controller instance"
        def singleController = new DocsController()

        expect: "multiple calls on same instance work correctly"
        singleController.index() == "redirect:swagger-ui.html"
        singleController.index() == "redirect:swagger-ui.html"
        
        and: "instance remains stateless"
        singleController.index() == new DocsController().index()
    }
}