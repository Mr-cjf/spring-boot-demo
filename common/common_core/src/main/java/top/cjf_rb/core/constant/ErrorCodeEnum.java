package top.cjf_rb.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 业务错误码枚举集合
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    /**
     * 未知错误
     */
    UNKNOWN_ERROR(AppCodeErrorTypeConst.INTERNAL_ERROR, "系统忙不过来了, 稍后再试试"),
    DATA_CHANGED_NEED_REFRESH(AppCodeErrorTypeConst.BUSINESS_ERROR, "数据有变化, 刷新后再试试"),

    // 请求参数相关
    PARAMS_INCORRECT(AppCodeErrorTypeConst.BUSINESS_ERROR, "接口参数错误"),
    BUSINESS_INCORRECT(AppCodeErrorTypeConst.BUSINESS_ERROR, "业务不符合"),
    PERMISSION_DENIED(AppCodeErrorTypeConst.BUSINESS_ERROR, "无权访问"),
    NOT_FOUND(AppCodeErrorTypeConst.BUSINESS_ERROR, "数据不存在或已删除"),
    OPERATION_TIMED_OUT(AppCodeErrorTypeConst.BUSINESS_ERROR, "操作超时, 请刷新页面"),
    REQUEST_TIMED_OUT(AppCodeErrorTypeConst.BUSINESS_ERROR, "请求超时, 请稍后再试"),
    WAIT_TIMED_OUT(AppCodeErrorTypeConst.BUSINESS_ERROR, "等待超时, 请稍后再试"),
    TOO_MANY_REQUESTS(AppCodeErrorTypeConst.BUSINESS_ERROR, "请求过多, 请稍后再试"),
    REPEAT_REQUEST(AppCodeErrorTypeConst.BUSINESS_ERROR, "请勿重复请求"),


    // 登录相关
    // 账号/用户/密码
    USER_EXISTED(AppCodeErrorTypeConst.BUSINESS_ERROR, "用户已存在"),
    USER_NOT_EXIST(AppCodeErrorTypeConst.BUSINESS_ERROR, "用户不存在"),
    ACCOUNT_EXISTED(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号已存在"),
    ACCOUNT_NOT_EXIST(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号不存在"),
    ACCOUNT_DISABLED(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号已停用"),
    ACCOUNT_LOCKED(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号已锁定"),
    ACCOUNT_EXPIRED(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号已过期"),
    ACCOUNT_UNAUTHORIZED(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号未认证"),
    PASSWORD_INCORRECT(AppCodeErrorTypeConst.BUSINESS_ERROR, "密码不正确"),
    ACCOUNT_OR_PASSWORD_INCORRECT(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号或密码不正确"),
    PASSWORD_NOT_CHANGED(AppCodeErrorTypeConst.BUSINESS_ERROR, "新旧密码一致, 无需修改"),
    LOGIN_EXPIRED(AppCodeErrorTypeConst.BUSINESS_ERROR, "登录已过期, 请重新登录"),
    LOGIN_INCORRECT(AppCodeErrorTypeConst.BUSINESS_ERROR, "登录已失效, 请重新登录"),
    NOT_LOGGED_IN(AppCodeErrorTypeConst.BUSINESS_ERROR, "未登录"),
    SQUEEZED_OFFLINE(AppCodeErrorTypeConst.BUSINESS_ERROR, "账号已在其他设备登录"),
    AUTHENTICATION_INCORRECT(AppCodeErrorTypeConst.BUSINESS_ERROR, "认证异常, 请重新登录"),

    // 手机号/验证码
    PHONE_EXISTED(AppCodeErrorTypeConst.IGNORE_ERROR, "手机号已存在"),
    PHONE_NOT_EXIST(AppCodeErrorTypeConst.IGNORE_ERROR, "手机号不存在"),
    BAD_CAPTCHA(AppCodeErrorTypeConst.IGNORE_ERROR, "验证码不正确"),
    BAD_EXPIRED(AppCodeErrorTypeConst.IGNORE_ERROR, "验证码已失效"),
    FAILED_SEND_CAPTCHA(AppCodeErrorTypeConst.BUSINESS_ERROR, "验证码发送失败"),
    REPEAT_SEND_CAPTCHA(AppCodeErrorTypeConst.IGNORE_ERROR, "验证码已发送过, 注意查收"),

    NAME_EXISTED(AppCodeErrorTypeConst.IGNORE_ERROR, "名字已存在"),
    DECRYPTION_FAILED(AppCodeErrorTypeConst.IGNORE_ERROR, "解密失败"),

    // 文件上传/下载
    EXCEL_EXPORT_FAILED(AppCodeErrorTypeConst.BUSINESS_ERROR, "Excel导出失败"),
    EXCEL_IMPORT_FAILED(AppCodeErrorTypeConst.BUSINESS_ERROR, "Excel导入失败"),
    UPLOAD_FAILED(AppCodeErrorTypeConst.INTERNAL_ERROR, "文件上传失败"),

    // 数据操作
    SELECT_FAILED(AppCodeErrorTypeConst.INTERNAL_ERROR, "查询失败"),
    UPDATE_FAILED(AppCodeErrorTypeConst.INTERNAL_ERROR, "更新失败"),
    INSERT_FAILED(AppCodeErrorTypeConst.INTERNAL_ERROR, "新增失败"),
    DELETE_FAILED(AppCodeErrorTypeConst.INTERNAL_ERROR, "删除失败"),

    // 内部接口调用
    INTERNAL_CALL_FAILED(AppCodeErrorTypeConst.INTERNAL_ERROR, "内部接口调用失败"),

    // 数据相关
    HAS_SUB_DATA(AppCodeErrorTypeConst.BUSINESS_ERROR, "存在子级数据, 无法处理"),
    READ_ONLY(AppCodeErrorTypeConst.BUSINESS_ERROR, "不可修改或删除"),
    DATA_EXISTED(AppCodeErrorTypeConst.BUSINESS_ERROR, "数据已存在"),
    DATA_NOT_EXIST(AppCodeErrorTypeConst.BUSINESS_ERROR, "数据不存在"),
    DATA_EXIST_RELATION(AppCodeErrorTypeConst.BUSINESS_ERROR, "数据存在关联"),
    DATA_DUPLICATED(AppCodeErrorTypeConst.BUSINESS_ERROR, "数据重复"),
    DATA_ERRORS(AppCodeErrorTypeConst.BUSINESS_ERROR, "数据错误"),
    ;

    /**
     * 错误类型
     */
    private final String type;
    /**
     * 错误信息
     */
    private final String msg;

    public String getCode() {
        return name();
    }

    public List<ErrorCodeEnum> getErrorCodeEnums(String type) {
        List<ErrorCodeEnum> allEnums = Arrays.stream(ErrorCodeEnum.values())
                                             .toList();

        return allEnums.stream()
                       .filter(errorCodeEnum -> type == null || Objects.equals(errorCodeEnum.getType(), type))
                       .toList();
    }

}

