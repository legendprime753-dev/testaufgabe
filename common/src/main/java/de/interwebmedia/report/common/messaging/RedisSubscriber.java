package de.interwebmedia.report.common.messaging;

import java.util.function.BiConsumer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisSubscriber {

    private final String host;
    private final int port;

    public RedisSubscriber(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public JedisPubSub subscribe(BiConsumer<String, String> callback, String... channels) {
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                callback.accept(channel, message);
            }
        };

        new Jedis(host, port).subscribe(pubSub, channels);
        return pubSub;
    }
}
