package com.om.ChatBuddy.common.model.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class MongoDbBaseEntity {

    @Id
    String id;

    @Version
    Long version;

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime lastModifiedAt;
}
