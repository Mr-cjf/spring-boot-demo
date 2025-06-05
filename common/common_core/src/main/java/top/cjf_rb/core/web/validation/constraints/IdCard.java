package top.cjf_rb.core.web.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import top.cjf_rb.core.web.validation.constraintvalidators.IdCardValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <pre>
 * 身份证格式校验
 * 校验规则: 身份证号(15位、18位数字)，最后一位是校验位，可能为数字或字符X
 * </pre>
 */
@Documented
@Constraint(validatedBy = {IdCardValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(IdCard.List.class)
public @interface IdCard {

    String message() default "{validation.constraints.idCard.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Defines several {@code @Account} annotations on the same element.
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        IdCard[] value();
    }
}
