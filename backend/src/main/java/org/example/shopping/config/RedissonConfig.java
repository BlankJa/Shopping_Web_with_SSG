package org.example.startup.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${redisson.connection-pool-size:10}")
    private int connectionPoolSize;

    @Value("${redisson.connection-minimum-idle-size:5}")
    private int connectionMinimumIdleSize;

    @Value("${redisson.idle-connection-timeout:10000}")
    private int idleConnectionTimeout;

    @Value("${redisson.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${redisson.timeout:3000}")
    private int timeout;

    @Value("${redisson.retry-attempts:3}")
    private int retryAttempts;

    @Value("${redisson.retry-interval:1500}")
    private int retryInterval;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        String address = "redis://" + redisHost +":" + redisPort;
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setConnectTimeout(connectTimeout)
                .setTimeout(timeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval);
        
        // 如果有密码则设置密码
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }
        
        return Redisson.create(config);
    }
}