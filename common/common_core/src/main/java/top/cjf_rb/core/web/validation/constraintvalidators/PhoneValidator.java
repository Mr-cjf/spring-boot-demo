package top.cjf_rb.core.web.validation.constraintvalidators;

import top.cjf_rb.core.constant.RegexPatternEnum;
import top.cjf_rb.core.web.validation.constraints.Phone;

import java.util.regex.Pattern;

/**
 * 手机号格式校验器
 */
public class PhoneValidator extends AbstractRegexValidator<Phone> {
    /**
     * 手机号格式正则
     */
    public static final Pattern PATTERN = RegexPatternEnum.PHONE.getPattern();

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
}
