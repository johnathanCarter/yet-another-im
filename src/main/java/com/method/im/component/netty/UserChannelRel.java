package com.method.im.component.netty;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户和channel绑定关系
 */
public class UserChannelRel {

    private static HashMap<String, Channel> manager = new HashMap<>();

    public static void put(String sendId, Channel channel) {
        manager.put(sendId, channel);
    }

    public static Channel get(String sendId) {
        return manager.get(sendId);
    }

    public static void output() {
        for (Map.Entry<String, Channel> entry : manager.entrySet()) {
            System.out.println("userId: " + entry.getKey() + " channelId: " + entry.getValue().id().asLongText());
        }
    }
}
