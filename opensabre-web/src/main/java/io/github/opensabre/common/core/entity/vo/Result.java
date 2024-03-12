package io.github.opensabre.common.core.entity.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.github.opensabre.common.core.exception.BaseException;
import io.github.opensabre.common.core.exception.ErrorType;
import io.github.opensabre.common.core.exception.SystemErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Rest统一返回数据结构
 */
@Schema(description = "rest请求的返回模型，所有rest正常都返回该类的对象")
@Getter
public class Result<T> {
    /**
     * 处理成功的常量代码
     */
    public static final String SUCCESSFUL_CODE = "000000";
    /**
     * 处理成功的默认提示信息
     */
    public static final String SUCCESSFUL_MESG = "处理成功";
    /**
     * 统一报文中code代码，标明异常类型
     */
    @Schema(title = "处理结果code", description = "处理结果，000000为成功", requiredMode = Schema.RequiredMode.REQUIRED, example="000000")
    private final String code;
    /**
     * 统一报文中提示信息
     */
    @Schema(title = "处理结果描述信息", description = "对code的详细说明和描述", example = "成功")
    private final String mesg;
    /**
     * 统一报文中报文生成时间
     */
    @Schema(title = "请求结果生成时间戳", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01T20:30:00.123Z")
    @JsonFormat(pattern = DatePattern.UTC_MS_PATTERN)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime time;
    /**
     * 统一报文中数据部分，支持泛型
     */
    @Schema(title = "处理结果数据信息", description = "可能为字符串、数组、对象等", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海市")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * 默认构建方法，无参时默认为成功报文
     */
    public Result() {
        this.code = SUCCESSFUL_CODE;
        this.mesg = SUCCESSFUL_MESG;
        this.time = LocalDateTime.now();
    }

    /**
     * 通过ErrorType构建Result
     *
     * @param errorType 错误类型
     */
    public Result(ErrorType errorType) {
        this.code = errorType.getCode();
        this.mesg = errorType.getMesg();
        this.time = LocalDateTime.now();
    }

    /**
     * 通过ErrorType和data数据构建Result
     *
     * @param errorType 错误类型
     * @param data      错误数据
     */
    public Result(ErrorType errorType, T data) {
        this(errorType);
        this.data = data;
    }

    /**
     * 内部使用，用于构造成功的结果
     *
     * @param code 错误码
     * @param mesg 错误信息
     * @param data 错误数据对象
     */
    private Result(String code, String mesg, T data) {
        this.code = code;
        this.mesg = mesg;
        this.data = data;
        this.time = LocalDateTime.now();
    }

    /**
     * 快速创建成功结果并返回结果数据
     *
     * @param data 错误数据对象
     * @return Result
     */
    public static Result success(Object data) {
        return new Result<>(SUCCESSFUL_CODE, SUCCESSFUL_MESG, data);
    }

    /**
     * 快速创建成功结果
     *
     * @return Result
     */
    public static Result success() {
        return success(null);
    }

    /**
     * 系统异常类没有返回数据
     *
     * @return Result
     */
    public static Result fail() {
        return new Result(SystemErrorType.SYSTEM_ERROR);
    }

    /**
     * 系统异常类并返回结果数据
     *
     * @param data 错误数据对象
     * @return Result
     */
    public static Result fail(Object data) {
        return new Result<>(SystemErrorType.SYSTEM_ERROR, data);
    }

    /**
     * 系统异常类没有返回数据
     *
     * @param baseException 异常类参数
     * @return Result
     */
    public static Result fail(BaseException baseException) {
        return fail(baseException, null);
    }

    /**
     * 系统异常类并返回结果数据
     *
     * @param data          错误数据对象
     * @param baseException 异常对象
     * @return Result
     */
    public static Result fail(BaseException baseException, Object data) {
        return new Result<>(baseException.getErrorType(), data);
    }

    /**
     * 系统异常类并返回结果数据
     *
     * @param errorType 错误类型
     * @param data      错误数据
     * @return Result
     */
    public static Result fail(ErrorType errorType, Object data) {
        return new Result<>(errorType, data);
    }

    /**
     * 系统异常类并返回结果数据
     *
     * @param errorType 错误类型
     * @return Result
     */
    public static Result fail(ErrorType errorType) {
        return Result.fail(errorType, null);
    }

    /**
     * 成功code=000000
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isSuccess() {
        return SUCCESSFUL_CODE.equals(this.code);
    }

    /**
     * 失败
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isFail() {
        return !isSuccess();
    }
}
