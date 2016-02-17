package ru.nojs.inject;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Юыху on 14.02.2016.
 */
@Scope
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Singleton {
}
