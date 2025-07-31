package com.thred.datingapp.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MinFileCountValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinFileCount {

    String message() default "최소 사진은 {min}개 이상이여야 합니다.";

    int min() default 1;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
