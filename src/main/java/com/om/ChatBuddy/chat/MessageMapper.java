package com.om.ChatBuddy.chat;

import com.om.ChatBuddy.chat.MessageDto.MessageInDto;
import com.om.ChatBuddy.chat.MessageDto.MessageOutDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    MessageEntity inDtoToEntity(MessageInDto messageInDto);

    MessageOutDto entityToOutDto(MessageEntity messageEntity);

    List<MessageOutDto> entitiesToOutDtos(List<MessageEntity> entities);
}