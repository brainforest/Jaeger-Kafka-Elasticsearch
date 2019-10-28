package demo.instrument;

import java.lang.annotation.*;

/**
 * Huabing Zhao
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Traced {
}