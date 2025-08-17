package com.thred.datingapp.common.aspect;

import com.thred.datingapp.common.annotation.DistributedLock;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class DistributedLockAspect {

  private final RedissonClient   redissonClient;
  private final ExpressionParser parser = new SpelExpressionParser();

  @Around("@annotation(distributedLock)")
  public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
    String key = parseKey(joinPoint, distributedLock.key());
    long waitTime = distributedLock.waitTime();
    long leaseTime = distributedLock.leaseTime();

    RLock rLock = redissonClient.getLock(key);
    boolean lockAcquired = false;
    try {
      lockAcquired = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
      if(!lockAcquired) {
        log.error("[around] 락 획득 실패 ===> lockKey: {}", key);
        throw new CustomException(BaseErrorCode.LOCK_ACQUIRE_FAIL);
      }
      log.info("[around] 락 획득 성공 ===> lockKey: {}", key);
      return joinPoint.proceed();

    } catch (InterruptedException e) {
      log.error("[around] 락 획득중 인터셉트 발생 ===> lockKey: {}", key);
      throw new CustomException(BaseErrorCode.LOCK_ACQUIRE_INTERRUPTED);
    } catch (Throwable e) {
      log.error("[around] 알 수 없는 예외 발생 ===> lockKey: {}, error: {}", key, e.getMessage());
      throw new CustomException(BaseErrorCode.INTERNAL_SERVER_ERROR);
    }finally {
      if(lockAcquired && rLock.isHeldByCurrentThread()) {
        rLock.unlock();
        log.info("[RedissonLock] 락 해제 완료 ===> lockKey: {}", key);
      }
    }

  }

  private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
    StandardEvaluationContext context = new StandardEvaluationContext();
    Object[] args = joinPoint.getArgs();
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = signature.getParameterNames();

    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }
    return parser.parseExpression(keyExpression).getValue(context, String.class);

  }
}
