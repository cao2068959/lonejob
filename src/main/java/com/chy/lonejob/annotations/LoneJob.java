package com.chy.lonejob.annotations;


import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoneJob {

    String name() default "";

    String cron() default "";
}
