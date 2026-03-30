package de.interwebmedia.report.common.messaging;

import redis.clients.jedis.JedisPooled;

public class RedisPublisher {

    private final JedisPooled jedis;

    public RedisPublisher(String host, int port) {
        this.jedis = new JedisPooled(host, port);
    }

    public void publish(String channel, String payload) {
        jedis.publish(channel, payload);
    }
}
