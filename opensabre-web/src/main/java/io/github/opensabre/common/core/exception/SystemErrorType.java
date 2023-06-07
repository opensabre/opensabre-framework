package io.github.opensabre.common.core.exception;

import lombok.Getter;

/**
 * 系统内置错误类型
 */
@Getter
public enum SystemErrorType implements ErrorType {
    /**
     * 未预料异常时均处理为该类型
     */
    SYSTEM_ERROR("-1", "系统异常"),
    /**
     * 系统繁忙，限流时
     */
    SYSTEM_BUSY("000001", "系统繁忙,请稍候再试"),
    /**
     * 网关转发时未找到该服务
     */
    GATEWAY_NOT_FOUND_SERVICE("010404", "服务未找到"),
    /**
     * 网关发生异常
     */
    GATEWAY_ERROR("010500", "网关异常"),
    /**
     * 网关调用后端server超时
     */
    GATEWAY_CONNECT_TIME_OUT("010002", "网关超时"),
    /**
     * Form表单字段校验不通过
     */
    ARGUMENT_NOT_VALID("020000", "请求参数校验不通过"),
    /**
     * Rest不支持的方法
     */
    METHOD_NOT_SUPPORTED("020001", "请求方法不支持"),
    /**
     * 无效Token
     */
    INVALID_TOKEN("021001", "无效token"),
    /**
     * 文件上传时，超过设定大小
     */
    UPLOAD_FILE_SIZE_LIMIT("020010", "上传文件大小超过限制"),
    /**
     * DB处理时，唯一键冲突时返回
     */
    DUPLICATE_PRIMARY_KEY("030000", "唯一键冲突");

    /**
     * 错误类型码
     */
    private final String code;
    /**
     * 错误类型描述信息
     */
    private final String mesg;

    /**
     * 构建函数
     *
     * @param code 错误代码
     * @param mesg 错误提示信息
     */
    SystemErrorType(String code, String mesg) {
        this.code = code;
        this.mesg = mesg;
    }
}
