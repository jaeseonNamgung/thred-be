package com.thred.datingapp.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory());
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    // 트랜잭션 활성화
    template.setEnableTransactionSupport(true);
    return template;
  }

  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()                          // 싱글 Redis 서버 모드 사용
          .setAddress("redis://" + host + ":" + port) // Redis 서버 주소 (호스트와 포트)
          .setConnectionMinimumIdleSize(5)            // 최소 유휴 연결 수
          .setConnectionPoolSize(10)                  // 커넥션 풀 최대 크기
          .setIdleConnectionTimeout(10000)            // 유휴 연결 타임아웃 (ms)
          .setConnectTimeout(10000)                   // 연결 타임아웃 (ms)
          .setTimeout(3000)                           // 명령 실행 타임아웃 (ms)
          .setRetryAttempts(3)                        // 재시도 횟수
          .setRetryInterval(1500);                    // 재시도 간격 (ms)
    return Redisson.create(config);                   // RedissonClient 객체 생성 및 반환

  }
}
