package io.github.opensabre.common.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.entity.vo.Result;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.validation.constraints.NotNull;

/**
 * Rest统一返回报文封装，在rest方法返回后送给客户端前执行
 */
@RestControllerAdvice
public class RestResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Value("${opensabre.rest.result.framework.excludes}")
    private String excludeFrameworkPackageStr;
    @Value("${opensabre.rest.result.excludes}")
    private String excludePackageStr;

    @Override
    public boolean supports(@NotNull MethodParameter returnType,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果不需要进行封装的，可以添加一些校验手段，比如添加标记排除的注解
        Class<?> returnTypeContainingClass = returnType.getContainingClass();
        boolean hasResponseBody = AnnotatedElementUtils.hasAnnotation(returnTypeContainingClass, ResponseBody.class) ||
                returnType.hasMethodAnnotation(ResponseBody.class);
        boolean hasRestController = AnnotatedElementUtils.hasAnnotation(returnTypeContainingClass, RestController.class) ||
                returnType.hasMethodAnnotation(RestController.class);
        return hasResponseBody && hasRestController && isNeedWrap(returnTypeContainingClass.getPackageName());
    }

    /**
     * @param packageName 包名
     * @return 是否需要 true/false
     */
    private boolean isNeedWrap(String packageName) {
        String allExcludePackageStr = StringUtils.joinWith(",", excludeFrameworkPackageStr, excludePackageStr);
        return !StringUtils.startsWithAny(packageName, StringUtils.split(allExcludePackageStr, ","));
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
        // 如果是String，将Result转为json string
        if (body instanceof String) {
            return new ObjectMapper().writeValueAsString(Result.success(body));
        }
        return Result.success(body);
    }
}