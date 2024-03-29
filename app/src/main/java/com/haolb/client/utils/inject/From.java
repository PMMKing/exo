package com.haolb.client.utils.inject;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD })
@Inherited
public @interface From {

    public abstract int value();

    public abstract boolean canBeNull() default false;

}
