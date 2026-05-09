package com.om.ChatBuddy.chat;

import com.om.ChatBuddy.common.model.domain.MongoDbBaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.om.ChatBuddy.common.constant.AppConstants.CollectionNames.CHAT__MESSAGE;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = CHAT__MESSAGE)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageEntity extends MongoDbBaseEntity {

    String content;
    String sender;
    MessageType messageType;
}
