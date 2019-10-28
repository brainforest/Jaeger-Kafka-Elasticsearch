package demo;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Huabing Zhao
 */
@RestController
public class EShopController {

    private static final Logger logger = LogManager.getLogger(EShopController.class);

    @Autowired
    private RestTemplate restTemplate;

    public SpanContext parentContext;

    @Autowired
    Tracer tracer;

    @RequestMapping(value = "/checkout")
    public String checkout(@RequestHeader HttpHeaders headers) {
        String result = "";
        Span start = tracer.buildSpan("entry").start();
        parentContext = start.context();
        // Use HTTP GET in this demo. In a real world use case,We should use HTTP POST
        // instead.
        // The three services are bundled in one jar for simplicity. To make it work,
        // define three services in Kubernets.
        result += restTemplate.getForEntity("http://localhost:8080/createOrder", String.class).getBody();
        result += "<BR>";
        result += restTemplate.getForEntity("http://localhost:8080/payment", String.class).getBody();
        result += "<BR>";
        result += restTemplate.getForEntity("http://localhost:8080/arrangeDelivery", String.class).getBody();
        start.finish();
        return result;
    }
}
