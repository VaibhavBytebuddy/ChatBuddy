package com.om.ChatBuddy.chat;

import com.om.ChatBuddy.chat.MessageDto.MessageInDto;
import com.om.ChatBuddy.chat.MessageDto.MessageOutDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class ChatServiceImpl implements ChatService {

    ChatDao chatDao;
    MessageMapper messageMapper;

    @Override
    public MessageOutDto processAndSaveMessage(MessageInDto messageInDto) {
        MessageEntity messageEntity = messageMapper.inDtoToEntity(messageInDto);
        messageEntity = chatDao.save(messageEntity);
        return messageMapper.entityToOutDto(messageEntity);
    }
}
