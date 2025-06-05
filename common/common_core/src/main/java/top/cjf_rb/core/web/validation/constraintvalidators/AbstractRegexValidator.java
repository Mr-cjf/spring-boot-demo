package top.cjf_rb.core.web.validation.constraintvalidators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * 正则匹配
 */
public abstract class AbstractRegexValidator<T extends Annotation> implements ConstraintValidator<T, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return getPattern().matcher(value).matches();
    }

    /**
     * 需要子类提供对应的Pattern
     *
     * @return Pattern
     */
    protected abstract Pattern getPattern();
}