package com.om.ChatBuddy.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiEndPointConstants {

    public static final String API_V1 = "/api/v1";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Domain {
        public static final String CHAT = API_V1 + "/chats";
        public static final String ROOM = API_V1 + "/rooms";
        public static final String PUBLIC = "/public";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Public {
        public static final String HEALTH = "health";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WebSocket {
        public static final String WS_CHAT = "/ws-chat";
        public static final String TOPIC_PREFIX = "/topic";
        public static final String APP_PREFIX = "/app";

        public static final String CHAT_SEND = "/chat.sendMessage";
        public static final String CHAT_ADD_USER = "/chat.addUser";
        public static final String TOPIC_PUBLIC = "/topic/public";
    }

}
