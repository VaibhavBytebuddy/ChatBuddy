package com.om.ChatBuddy.chat;

import com.om.ChatBuddy.chat.MessageDto.MessageInDto;
import com.om.ChatBuddy.chat.MessageDto.MessageOutDto;

public interface ChatService {
    MessageOutDto processAndSaveMessage(MessageInDto messageInDto);
}