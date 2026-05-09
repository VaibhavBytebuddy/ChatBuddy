package com.om.ChatBuddy.chat;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatDao extends MongoRepository<MessageEntity, String> {
}
