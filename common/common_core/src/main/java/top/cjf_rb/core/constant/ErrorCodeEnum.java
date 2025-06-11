package top.cjf_rb.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举集合
 *
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    /**
     * 未知错误
     */
    UNKNOWN_ERROR("系统忙不过来了, 稍后再试试"), DATA_CHANGED_NEED_REFRESH("数据有变化, 刷新后再试试"),

    PARAMS_INCORRECT("接口参数错误"), BUSINESS_INCORRECT("业务不符合"), PERMISSION_DENIED("无权访问"), NOT_FOUND("数据不存在或已删除"),

    // 登录相关
    LOGIN_EXPIRED("登录已过期, 请重新登录"), LOGIN_INCORRECT("登录已失效, 请重新登录"), NOT_LOGGED_IN("未登录"),
    SQUEEZED_OFFLINE("账号已在其他设备登录"), AUTHENTICATION_INCORRECT("认证异常, 请重新登录"),

    // 账号/用户/密码
    USER_EXISTED("用户已存在"), USER_NOT_EXIST("用户不存在"), ACCOUNT_EXISTED("账号已存在"), ACCOUNT_NOT_EXIST("账号不存在"),
    ACCOUNT_DISABLED("账号已停用"), ACCOUNT_LOCKED("账号已锁定"), ACCOUNT_EXPIRED("账号已过期"), ACCOUNT_UNAUTHORIZED("账号未认证"),
    PASSWORD_INCORRECT("密码不正确"), ACCOUNT_OR_PASSWORD_INCORRECT("账号或密码不正确"), PASSWORD_NOT_CHANGED("新旧密码一致, 无需修改"),

    // 手机号/验证码
    PHONE_EXISTED("手机号已存在"), PHONE_NOT_EXIST("手机号不存在"), BAD_CAPTCHA("验证码不正确"), BAD_EXPIRED("验证码已失效"),
    FAILED_SEND_CAPTCHA("验证码发送失败"), REPEAT_SEND_CAPTCHA("验证码已发送过, 注意查收"),

    NAME_EXISTED("名字已存在"), DECRYPTION_FAILED("解密失败"),

    EXCEL_EXPORT_FAILED("Excel导出失败"), EXCEL_IMPORT_FAILED("Excel导入失败"), UPLOAD_FAILED("文件上传失败"),

    // 数据操作
    SELECT_FAILED("查询失败"), UPDATE_FAILED("更新失败"), INSERT_FAILED("新增失败"), DELETE_FAILED("删除失败"),

    INTERNAL_CALL_FAILED("内部接口调用失败"), TOO_MANY_REQUESTS("请求过多, 请稍后再试"), REPEAT_REQUEST("请勿重复请求"),

    OPERATION_TIMED_OUT("操作超时, 请刷新页面"), REQUEST_TIMED_OUT("请求超时, 请稍后再试"), WAIT_TIMED_OUT("请求超时, 请稍后再试"),

    HAS_SUB_DATA("存在子级数据, 无法处理"), READ_ONLY("不可修改或删除"), DATA_EXISTED("数据已存在"), DATA_NOT_EXIST("数据不存在"),
    DATA_EXIST_RELATION("数据存在关联"), DATA_DUPLICATED("数据重复");

    private final String msg;

    public String getCode() {
        return name();
    }

}

