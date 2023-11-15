package org.sunbird.cb.hubservices.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NetworkServerProperties {

    @Value("${redis.host.name}")
    private String redisHostName;

    @Value("${redis.port}")
    private String redisPort;

    @Value("${redis.timeout}")
    private String redisTimeout;

    @Value("${redis.userList.read.timeout}")
    private Integer redisUserListReadTimeOut;

    @Value("${network.request.default.limit}")
    private Integer defaultLimit;

    @Value("${network.request.max.limit}")
    private Integer maxLimit;

    public String getRedisHostName() {
        return redisHostName;
    }

    public void setRedisHostName(String redisHostName) {
        this.redisHostName = redisHostName;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisTimeout() {
        return redisTimeout;
    }

    public void setRedisTimeout(String redisTimeout) {
        this.redisTimeout = redisTimeout;
    }

    public Integer getRedisUserListReadTimeOut() {
        return redisUserListReadTimeOut;
    }

    public void setRedisUserListReadTimeOut(Integer redisUserListReadTimeOut) {
        this.redisUserListReadTimeOut = redisUserListReadTimeOut;
    }

    public Integer getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(Integer defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }
}
