package top.cjf_rb.core.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ClientAgentEnum {

    /**
     * 平台
     */
    PLATFORM("PLATFORM", "平台"),
    /**
     * 机构
     */
    ORGANIZATION("ORGANIZATION", "机构"),
    /**
     * 终端
     */
    TERMINAL("TERMINAL", "终端");

    private final String code;
    private final String message;
}
