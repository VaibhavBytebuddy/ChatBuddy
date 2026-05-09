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
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Public {
        public static final String HEALTH = API_V1 + "/public/health";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WebSocket {
        public static final String ENDPOINT = "/ws-chat";
        public static final String TOPIC_PREFIX = "/topic";
        public static final String APP_PREFIX = "/app";
    }
}
