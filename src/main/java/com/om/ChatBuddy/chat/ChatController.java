package com.om.ChatBuddy.chat;

import com.om.ChatBuddy.chat.MessageDto.MessageInDto;
import com.om.ChatBuddy.chat.MessageDto.MessageOutDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import static com.om.ChatBuddy.common.constant.ApiEndPointConstants.WebSocket.CHAT_ADD_USER;
import static com.om.ChatBuddy.common.constant.ApiEndPointConstants.WebSocket.CHAT_SEND;
import static com.om.ChatBuddy.common.constant.ApiEndPointConstants.WebSocket.TOPIC_PUBLIC;
import static lombok.AccessLevel.PRIVATE;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class ChatController {

    ChatService chatService;

    @MessageMapping(CHAT_SEND)
    @SendTo(TOPIC_PUBLIC)
    public MessageOutDto sendMessage(@Payload MessageInDto messageInDto) {
        return chatService.processAndSaveMessage(messageInDto);
    }

    @MessageMapping(CHAT_ADD_USER)
    @SendTo(TOPIC_PUBLIC)
    public MessageOutDto addUser(@Payload MessageInDto messageInDto) {
        // जॉईन मेसेज सेव्ह करा आणि ब्रॉडकास्ट करा
        return chatService.processAndSaveMessage(messageInDto);
    }

}
