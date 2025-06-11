package top.cjf_rb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoDubboService {

    /**
     * 自定义生成的接口名 默认为接口名+Service
     *
     * @return 接口包名
     */
    String interfaceName() default "";

    /**
     * 自定义生成的接口实现类名 默认为接口名+ServiceImpl
     *
     * @return 接口实现类名
     */
    String implName() default "";

    /**
     * 自定义生成的接口包名 默认为接口包名+.service 例如 最终生成路径 = 默认为接口包名 + interfacePackage
     *
     * @return 接口包名
     */
    String interfacePackage() default ".service";

    /**
     * 自定义生成的接口实现类包名 默认为接口包名 + interfacePackage +.impl 例如 最终生成路径 = 默认为接口包名 + interfacePackage + implPackage
     *
     * @return 接口实现类包名
     */
    String implPackage() default ".impl";

}
