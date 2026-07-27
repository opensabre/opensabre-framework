package io.github.opensabre.boot.rest;

import io.github.opensabre.boot.annotations.ResourcePermission;
import io.github.opensabre.boot.entity.RestMappingInfo;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
/** Covers resource snapshot collection semantics. */
class MappingInfoHandlerTest {

    @Test
    void methodlessMappingShouldExpandToEveryHttpMethod() throws Exception {
        MappingInfoHandler handler = handlerFor("included", RequestMappingInfo.paths("/any").build());

        Set<RestMappingInfo> mappings = handler.getMappingInfo();

        assertThat(mappings).hasSize(RequestMethod.values().length);
        assertThat(mappings).extracting(RestMappingInfo::getMethod)
                .containsExactlyInAnyOrder(java.util.Arrays.stream(RequestMethod.values())
                        .map(Enum::name).toArray(String[]::new));
    }

    @Test
    void registerFalseShouldExcludeInternalMapping() throws Exception {
        MappingInfoHandler handler = handlerFor("excluded",
                RequestMappingInfo.paths("/internal").methods(RequestMethod.PUT).build());

        assertThat(handler.getMappingInfo()).isEmpty();
    }

    private MappingInfoHandler handlerFor(String methodName, RequestMappingInfo mappingInfo) throws Exception {
        Method method = TestController.class.getDeclaredMethod(methodName);
        Map<RequestMappingInfo, HandlerMethod> methods = Map.of(
                mappingInfo, new HandlerMethod(new TestController(), method));
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping() {
            @Override
            public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
                return methods;
            }
        };
        MappingInfoHandler handler = new MappingInfoHandler();
        handler.requestMappingHandlerMapping = mapping;
        return handler;
    }

    private static class TestController {
        void included() {
        }

        @ResourcePermission(register = false)
        void excluded() {
        }
    }
}
