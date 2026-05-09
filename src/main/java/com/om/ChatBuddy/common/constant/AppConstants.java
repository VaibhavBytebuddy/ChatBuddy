package com.om.ChatBuddy.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    public static final String DEFAULT_ROOM_NAME = "Global Lounge";
    public static final int MAX_MESSAGE_LENGTH = 500;
    public static final String SYSTEM_USER = "System";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CollectionNames {
        public static final String CHAT__MESSAGE = "chat__message";
        public static final String CHAT__ROOM = "chat__room";
    }
}
