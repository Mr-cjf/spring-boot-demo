package top.cjf_rb.core.web.validation.constraintvalidators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import top.cjf_rb.core.util.IdCards;
import top.cjf_rb.core.web.validation.constraints.IdCard;

/**
 * 身份证号格式校验器
 *
 * @author lty
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return IdCards.validate(value);
    }
}

