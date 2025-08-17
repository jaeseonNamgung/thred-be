package com.thred.datingapp.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

  String key();                // 락을 식별하기 위한 Redis Key

  long waitTime() default 5;   // 락 획득 대기 시간 (초)

  long leaseTime() default 10; // 락 점유 시간 (초)
}
