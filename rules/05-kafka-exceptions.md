# Kafka Event-Driven Rules

## Overview

The project uses Kafka for asynchronous event-driven communication between services and for decoupled feature execution (e.g., Firestore sync, analytics, notifications).

## Topic Configuration

### Topic Constants File
Location: `com.{company}.{project}.core.eventDriven.Topic`

```java
public class Topic {
    
    // Domain event topics
    public static final String ENTITY_TOPIC = "EntityTopic";
    public static final String ORDER_TOPIC = "OrderTopic";
    public static final String ACCOUNT_TOPIC = "AccountTopic";
    
    // Integration topics
    public static final String FIRESTORE_TOPIC = "FirestoreTopic";
    public static final String NOTIFICATION_TOPIC = "NotificationTopic";
    public static final String ANALYTICS_TOPIC = "AnalyticsTopic";
}
```

### Topic Naming Convention
- PascalCase with "Topic" suffix
- Domain-specific: `{Domain}Topic`
- Integration-specific: `{Integration}Topic`

## Event Data Object (DPO)

### Base EventDpo Class
```java
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDpo {
    
    EventType eventType;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime lastModifiedAt;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime createdAt;
}
```

### EventType Enum
```java
public enum EventType {
    CREATED,
    UPDATED,
    DELETED,
    ADDED,
    REMOVED,
    COMPLETED
}
```

### Domain-Specific Event DPO
```java
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityEventDpo extends EventDpo {
    
    String eventId;
    String sourceService;
    String userId;
    String entityId;
    
    // Optional: Include entity snapshot for consumers
    EntityEntity entitySnapshot;
}
```

## Event Publisher

### Publisher Interface
```java
public interface EntityEventPublisher {
    
    void publish(EntityEventDpo entityEventDpo);
    
    void publishCreated(EntityEntity entityEntity);
    
    void publishUpdated(EntityEntity entityEntity);
    
    void publishDeleted(String entityId, String userId);
}
```

### Publisher Implementation
```java
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EntityEventPublisherImpl implements EntityEventPublisher {

    private static final String SOURCE_SERVICE = "entity-service";
    
    EventPublisher<EntityEventDpo> eventPublisher;

    @Override
    public void publish(EntityEventDpo entityEventDpo) {
        String partitionKey = entityEventDpo.getUserId();
        eventPublisher.publish(ENTITY_TOPIC, partitionKey, entityEventDpo);
        log.info("class=EntityEventPublisher method=publish eventId={} userId={} entityId={}",
                entityEventDpo.getEventId(), entityEventDpo.getUserId(), entityEventDpo.getEntityId());
    }

    @Override
    public void publishCreated(EntityEntity entityEntity) {
        EntityEventDpo dpo = buildEventDpo(entityEntity, EventType.CREATED);
        publish(dpo);
    }

    @Override
    public void publishUpdated(EntityEntity entityEntity) {
        EntityEventDpo dpo = buildEventDpo(entityEntity, EventType.UPDATED);
        publish(dpo);
    }

    @Override
    public void publishDeleted(String entityId, String userId) {
        EntityEventDpo dpo = EntityEventDpo.builder()
                .eventId(String.valueOf(System.currentTimeMillis()))
                .sourceService(SOURCE_SERVICE)
                .userId(userId)
                .entityId(entityId)
                .eventType(EventType.DELETED)
                .createdAt(LocalDateTime.now())
                .build();
        publish(dpo);
    }

    private EntityEventDpo buildEventDpo(EntityEntity entity, EventType eventType) {
        return EntityEventDpo.builder()
                .eventId(String.valueOf(System.currentTimeMillis()))
                .sourceService(SOURCE_SERVICE)
                .userId(entity.getUserId())
                .entityId(entity.getId())
                .entitySnapshot(entity)
                .eventType(eventType)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
```

## Event Listener

### Listener Class
```java
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Profile({"dev", "production"})  // Disable in test profile
public class EntityEventListener {

    private static final String GROUP_ID = "entity_consumer_group";

    EntityValidator entityValidator;
    EntityMapper entityMapper;
    ExternalService externalService;

    @KafkaListener(
            topics = ENTITY_TOPIC,
            groupId = GROUP_ID,
            autoStartup = "${project.kafka.enabled:true}"
    )
    @Transactional
    public void handleEntityEvent(EntityEventDpo entityEventDpo) {
        log.info("class=EntityEventListener method=handleEntityEvent status=start " +
                "eventId={} eventType={} userId={} entityId={}",
                entityEventDpo.getEventId(), entityEventDpo.getEventType(),
                entityEventDpo.getUserId(), entityEventDpo.getEntityId());
        
        try {
            processEvent(entityEventDpo);
            log.info("class=EntityEventListener method=handleEntityEvent status=complete " +
                    "eventId={} userId={} entityId={}",
                    entityEventDpo.getEventId(), entityEventDpo.getUserId(), 
                    entityEventDpo.getEntityId());
        } catch (Exception e) {
            log.error("class=EntityEventListener method=handleEntityEvent status=failed " +
                    "eventId={} userId={} error={}",
                    entityEventDpo.getEventId(), entityEventDpo.getUserId(), 
                    e.getMessage(), e);
            throw new EntityEventException(ErrorCode.EVENT_PROCESSING_FAILED,
                    "Failed to process event: " + entityEventDpo.getEventId());
        }
    }

    private void processEvent(EntityEventDpo dpo) {
        switch (dpo.getEventType()) {
            case CREATED, UPDATED -> syncToExternal(dpo);
            case DELETED -> removeFromExternal(dpo);
            default -> log.warn("Unhandled event type: {}", dpo.getEventType());
        }
    }

    private void syncToExternal(EntityEventDpo dpo) {
        EntityEntity entity = Objects.nonNull(dpo.getEntitySnapshot())
                ? dpo.getEntitySnapshot()
                : entityValidator.findById(dpo.getEntityId());
        externalService.upsert(entityMapper.toExternalDocument(entity));
    }

    private void removeFromExternal(EntityEventDpo dpo) {
        externalService.delete(dpo.getEntityId());
    }
}
```

### Listener Best Practices

| Practice | Description |
|----------|-------------|
| **Use `@Transactional`** | Ensures atomicity of event processing |
| **Use `@Profile`** | Disable in test profile to avoid side effects |
| **Structured logging** | Include eventId, userId, entityId in all logs |
| **Error handling** | Catch, log, and rethrow as domain exception |
| **Idempotency** | Design handlers to be safe for retry |
| **Partition key** | Use userId for ordering guarantees |

---

# Exception Handling

## Exception Structure

### Base Exception Class
```java
public class ProjectException extends RuntimeException {
    
    ErrorCode errorCode;
    String developerMessage;

    public ProjectException(ErrorCode errorCode, String developerMessage) {
        super(developerMessage);
        this.errorCode = errorCode;
        this.developerMessage = this.toString();
    }

    public ProjectException(ErrorCode errorCode) {
        super(errorCode.displayText);
        this.errorCode = errorCode;
        this.developerMessage = errorCode.displayText;
    }
}
```

### Domain-Specific Exception
```java
public class EntityException extends ProjectException {

    public EntityException(ErrorCode errorCode, String developerMessage) {
        super(errorCode, developerMessage);
    }

    public EntityException(ErrorCode errorCode) {
        super(errorCode);
    }
}
```

## ErrorCode Enum

```java
public enum ErrorCode {

    // Generic errors (1000xx)
    ENTITY_NOT_FOUND(100001, "Entity not found"),
    ENTITY_ALREADY_EXISTS(100002, "Entity already exists"),
    INVALID_INPUT(100003, "Invalid input"),
    OPERATION_NOT_ALLOWED(100004, "Operation not allowed"),
    
    // Domain-specific errors (1001xx for Entity, 1002xx for Order, etc.)
    ENTITY_STATUS_INVALID(100101, "Invalid entity status"),
    ENTITY_PARENT_REQUIRED(100102, "Parent entity is required"),
    
    ORDER_ALREADY_COMPLETED(100201, "Order already completed"),
    ORDER_PAYMENT_FAILED(100202, "Order payment failed"),
    
    // Integration errors (12xxxx)
    FIRESTORE_SYNC_FAILED(1200001, "Failed to sync to Firestore"),
    EXTERNAL_API_ERROR(1200002, "External API error"),
    EVENT_PROCESSING_FAILED(1200003, "Event processing failed");

    public final int code;
    public final String displayText;

    ErrorCode(int code, String displayText) {
        this.code = code;
        this.displayText = displayText;
    }
}
```

## Exception Throwing Patterns

```java
// Not found
throw new EntityException(ErrorCode.ENTITY_NOT_FOUND,
        "Entity with id " + entityId + " not found");

// Already exists
throw new EntityException(ErrorCode.ENTITY_ALREADY_EXISTS,
        "Entity with name " + name + " already exists in parent " + parentId);

// Invalid input
throw new EntityException(ErrorCode.INVALID_INPUT,
        "Field 'name' is required");

// Business rule violation
throw new EntityException(ErrorCode.OPERATION_NOT_ALLOWED,
        "Cannot delete entity " + entityId + " with active children");

// Status transition error
throw new EntityException(ErrorCode.ENTITY_STATUS_INVALID,
        "Cannot transition from " + currentStatus + " to " + targetStatus);
```

## Error Code Numbering Convention

| Range | Category | Example |
|-------|----------|---------|
| 1000xx | Generic/Common | ENTITY_NOT_FOUND |
| 1001xx | Entity domain | ENTITY_STATUS_INVALID |
| 1002xx | Order domain | ORDER_PAYMENT_FAILED |
| 1003xx | User domain | USER_NOT_VERIFIED |
| 12xxxxx | Integrations | FIRESTORE_SYNC_FAILED |
