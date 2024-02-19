package me.rhys.anticheat.base.check.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInfo {

    String name();

    String type();

    CheckType checkType();

    boolean enabled();

    int banVL() default 20;

    boolean ban() default true;
}
