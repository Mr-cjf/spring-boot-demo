package top.cjf_rb.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SexEnum {
    /**
     男
     */
    MAN(1),

    /**
     女
     */
    WOMAN(2),
    ;

    private final Integer value;

}
