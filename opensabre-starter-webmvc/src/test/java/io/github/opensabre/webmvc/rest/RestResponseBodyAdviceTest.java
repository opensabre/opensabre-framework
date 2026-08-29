package io.github.opensabre.webmvc.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

class RestResponseBodyAdviceTest {

    private final RestResponseBodyAdvice advice = new RestResponseBodyAdvice();

    @Test
    void keepsJsonResponsesWrapped() throws Exception {
        MethodParameter returnType = parameter("json");

        assertThat(advice.supports(returnType, StringHttpMessageConverter.class)).isTrue();
        assertThat(advice.beforeBodyWrite(Map.of("value", "body"), returnType, MediaType.APPLICATION_JSON,
                StringHttpMessageConverter.class, request(), response()))
                .isInstanceOf(io.github.opensabre.common.core.entity.vo.Result.class);
    }

    @Test
    void doesNotWrapFileAndStreamResponses() throws Exception {
        MethodParameter resource = parameter("resource");
        MethodParameter entity = parameter("entity");
        MethodParameter bytes = parameter("bytes");
        MethodParameter stream = parameter("stream");

        assertThat(advice.supports(resource, StringHttpMessageConverter.class)).isFalse();
        assertThat(advice.supports(entity, StringHttpMessageConverter.class)).isFalse();
        assertThat(advice.supports(bytes, StringHttpMessageConverter.class)).isFalse();
        assertThat(advice.supports(stream, StringHttpMessageConverter.class)).isFalse();
        assertThat(advice.supports(parameter("json"), ByteArrayHttpMessageConverter.class)).isFalse();

        byte[] payload = {1, 2, 3};
        InputStream input = new ByteArrayInputStream(payload);
        assertThat(advice.beforeBodyWrite(payload, bytes, MediaType.APPLICATION_OCTET_STREAM,
                ByteArrayHttpMessageConverter.class, request(), response())).isSameAs(payload);
        assertThat(advice.beforeBodyWrite(input, parameter("input"), MediaType.APPLICATION_OCTET_STREAM,
                ByteArrayHttpMessageConverter.class, request(), response())).isSameAs(input);
        assertThat(advice.beforeBodyWrite(new ByteArrayResource(payload), resource, MediaType.APPLICATION_OCTET_STREAM,
                StringHttpMessageConverter.class, request(), response())).isInstanceOf(ByteArrayResource.class);
    }

    private MethodParameter parameter(String methodName) throws Exception {
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new MethodParameter(method, -1);
    }

    private ServletServerHttpRequest request() {
        return new ServletServerHttpRequest(new MockHttpServletRequest());
    }

    private ServletServerHttpResponse response() {
        return new ServletServerHttpResponse(new MockHttpServletResponse());
    }

    @RestController
    private static class TestController {
        @GetMapping("/json")
        Object json() { return Map.of("value", "json"); }

        @GetMapping("/resource")
        ByteArrayResource resource() { return new ByteArrayResource(new byte[] {1}); }

        @GetMapping("/entity")
        ResponseEntity<ByteArrayResource> entity() { return ResponseEntity.ok(new ByteArrayResource(new byte[] {1})); }

        @GetMapping("/bytes")
        byte[] bytes() { return new byte[] {1}; }

        @GetMapping("/stream")
        StreamingResponseBody stream() { return output -> { }; }

        @GetMapping("/input")
        InputStream input() { return InputStream.nullInputStream(); }
    }
}
