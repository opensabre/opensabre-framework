package io.github.opensabre.webmvc.rest;

import tools.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.entity.vo.Result;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Rest统一返回报文封装，在rest方法返回后送给客户端前执行
 */
@RestControllerAdvice
public class RestResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    /**
     * 框架中不需要包装为Result对象的包名
     */
    @Value("${opensabre.rest.result.framework.excludes:org.springdoc}")
    private String excludeFrameworkPackageStr = "org.springdoc";
    /**
     * 应用中不需要包装为Result对象的包名
     */
    @Value("${opensabre.rest.result.excludes:}")
    private String excludePackageStr = "";

    @Override
    public boolean supports(@NotNull MethodParameter returnType,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果不需要进行封装的，可以添加一些校验手段，比如添加标记排除的注解
        Class<?> returnTypeContainingClass = returnType.getContainingClass();
        boolean hasResponseBody = AnnotatedElementUtils.hasAnnotation(returnTypeContainingClass, ResponseBody.class) ||
                returnType.hasMethodAnnotation(ResponseBody.class);
        boolean hasRestController = AnnotatedElementUtils.hasAnnotation(returnTypeContainingClass, RestController.class) ||
                returnType.hasMethodAnnotation(RestController.class);
        return hasResponseBody && hasRestController && isNeedWrap(returnTypeContainingClass.getPackageName())
                && !isPassthroughType(returnType.getParameterType(), converterType);
    }

    /**
     * 是否需要包装为Result统一报文
     *
     * @param packageName 包名
     * @return 是否需要 true/false
     */
    private boolean isNeedWrap(String packageName) {
        String delimiter = ",";
        String allExcludePackageStr = StringUtils.joinWith(delimiter, excludeFrameworkPackageStr, excludePackageStr);
        return !StringUtils.startsWithAny(packageName, StringUtils.split(allExcludePackageStr, delimiter));
    }

    /**
     * Binary, file and streaming responses must be written by their original message handler.
     */
    private boolean isPassthroughType(Class<?> returnType,
                                      Class<? extends HttpMessageConverter<?>> converterType) {
        return HttpEntity.class.isAssignableFrom(returnType)
                || HttpHeaders.class.isAssignableFrom(returnType)
                || Resource.class.isAssignableFrom(returnType)
                || ResourceRegion.class.isAssignableFrom(returnType)
                || InputStream.class.isAssignableFrom(returnType)
                || ByteBuffer.class.isAssignableFrom(returnType)
                || returnType == byte[].class
                || StreamingResponseBody.class.isAssignableFrom(returnType)
                || ResponseBodyEmitter.class.isAssignableFrom(returnType)
                || ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)
                || ResourceHttpMessageConverter.class.isAssignableFrom(converterType)
                || ResourceRegionHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    private boolean isPassthroughBody(Object body) {
        return body instanceof Resource
                || body instanceof ResourceRegion
                || body instanceof InputStream
                || body instanceof ByteBuffer
                || body instanceof byte[]
                || body instanceof StreamingResponseBody
                || body instanceof ResponseBodyEmitter;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body,
                                  @NotNull MethodParameter returnType,
                                  @NotNull MediaType selectedContentType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NotNull ServerHttpRequest request,
                                  @NotNull ServerHttpResponse response) {
        // 提供一定的灵活度，如果body已经被包装了，就不进行包装
        if (body instanceof Result) {
            return body;
        }
        if (isPassthroughBody(body)) {
            return body;
        }
        // 如果是String，将Result转为json string
        if (Objects.isNull(body) || body instanceof String) {
            return new ObjectMapper().writeValueAsString(Result.success(body));
        }
        return Result.success(body);
    }
}
