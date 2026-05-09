# Coding Style Preferences

## General Principles

| Principle | Guideline |
|-----------|-----------|
| **No inline comments** | Code should be self-documenting through clear naming |
| **Fully qualified names** | Use descriptive, domain-specific variable names |
| **Consistent formatting** | Follow existing indentation and line breaks in codebase |
| **Single responsibility** | Each method does one thing well |
| **Early returns** | Prefer guard clauses over nested conditions |

## Variable Naming Conventions

### ✅ Do Use
```java
// Method parameters - use full entity/dto type name
void create(CustomerOrderInDto customerOrderInDto)
void find(String customerOrderId)
void patchUpdate(String customerOrderId, CustomerOrderInDto customerOrderInDto)
void deleteBulk(List<String> customerOrderIds)

// Local variables - match the type name
CustomerOrderEntity customerOrderEntity = mapper.inDtoToEntity(dto);
List<CustomerOrderOutDto> customerOrderOutDtos = entities.stream()...;
CustomerOrderSearchInDto customerOrderSearchInDto = new CustomerOrderSearchInDto();
```

### ❌ Don't Use
```java
// Generic names are NOT allowed
void create(InDto dto)           // Too generic
void find(String id)             // Ambiguous
Entity e = dao.findById(id);     // Single letter
List<OutDto> results = ...;      // Not descriptive
```

## Lombok Annotation Ordering

**Order matters!** Follow this exact sequence:

### For Service/Controller/Validator Classes
```java
@Slf4j                                                    // 1. Logging (optional)
@Service / @RestController / @Component                   // 2. Spring stereotype
@RequiredArgsConstructor                                  // 3. Constructor injection
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)  // 4. Field defaults
public class EntityService {
    EntityValidator entityValidator;  // No @Autowired, no type annotation
    EntityMapper entityMapper;
}
```

### For Entity Classes
```java
@EqualsAndHashCode(callSuper = true)                      // 1. Equals/HashCode
@Data                                                      // 2. Getters/Setters/ToString
@AllArgsConstructor                                        // 3. All-args constructor
@NoArgsConstructor                                         // 4. No-args constructor
@Document(collection = COLLECTION_NAME)                    // 5. MongoDB annotation
@FieldDefaults(level = AccessLevel.PRIVATE)                // 6. Field defaults (NO makeFinal)
public class Entity extends MongoDbBaseEntity {
    String fieldName;
    Boolean active = true;
}
```

### For DTO Classes
```java
@EqualsAndHashCode(callSuper = true)  // Only if extends parent
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public static class EntityOutDto extends EntityCommonDto {
    String id;
    LocalDateTime createdAt;
}
```

### For Event DPO Classes
```java
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder                          // Use SuperBuilder for inheritance
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityEventDpo extends EventDpo {
    String entityId;
    String userId;
}
```

## Import Conventions

### Static Imports (Required)
```java
// Collection names - always static import
import static com.{company}.{project}.common.CollectionNames.ENTITY_COLLECTION;

// API endpoints - always static import
import static com.{company}.{project}.common.ApiEndPointConstants.Domain.API_ENTITY;

// Kafka topics - always static import
import static com.{company}.{project}.core.eventDriven.Topic.ENTITY_TOPIC;

// MongoDB Aggregation - wildcard allowed
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
```

### Utility Classes
```java
// Use Spring's CollectionUtils (NOT Apache)
import org.springframework.util.CollectionUtils;
CollectionUtils.isEmpty(list)

// Use Apache's StringUtils for strings
import org.apache.commons.lang3.StringUtils;
StringUtils.isBlank(str)
StringUtils.isNotBlank(str)

// Use Java's Objects for null checks
import java.util.Objects;
Objects.nonNull(value)
Objects.isNull(value)
```

## MongoDB Field Constants

Define at the TOP of Validator class, before dependencies:
```java
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EntityValidator {

    // Field constants - private static final, uppercase with underscores
    private static final String _ID = "_id";
    private static final String PARENT_ID = "parentId";
    private static final String STATUS = "status";
    private static final String ACTIVE = "active";
    private static final String COUNT = "count";
    private static final String NAME = "name";
    private static final String CREATED_AT = "createdAt";
    private static final String LAST_MODIFIED_AT = "lastModifiedAt";

    // Dependencies after constants
    EntityDao entityDao;
    MongoTemplate mongoTemplate;
}
```

## Stream Processing Patterns

### Collection Mapping
```java
// Single mapping
List<OutDto> outDtos = entities.stream()
    .map(mapper::entityToOutDto)
    .toList();

// With null filtering
List<OutDto> outDtos = entities.stream()
    .filter(Objects::nonNull)
    .map(mapper::entityToOutDto)
    .toList();
```

### Bulk Operations
```java
// Bulk processing with method reference
items.stream()
    .filter(Objects::nonNull)
    .forEach(this::processItem);

// Bulk processing with explicit lambda
items.stream()
    .filter(item -> Objects.nonNull(item) && StringUtils.isNotBlank(item.getId()))
    .forEach(item -> processItem(item, context));
```

### Chained Operations
```java
// Complex transformations
List<ProcessedItem> results = items.stream()
    .filter(Objects::nonNull)
    .filter(item -> StringUtils.isNotBlank(item.getId()))
    .map(this::transformItem)
    .filter(Objects::nonNull)
    .sorted(Comparator.comparing(ProcessedItem::getOrder))
    .toList();
```

### Grouping and Collecting
```java
// Group by field
Map<String, List<Entity>> byCategory = entities.stream()
    .collect(Collectors.groupingBy(Entity::getCategory));

// Extract IDs
List<String> ids = entities.stream()
    .map(Entity::getId)
    .toList();

// Find first match
Optional<Entity> match = entities.stream()
    .filter(e -> e.getStatus().equals(status))
    .findFirst();
```

## Optional Handling

### findById Pattern
```java
public Entity findById(String entityId) {
    return entityDao.findById(entityId)
        .orElseThrow(() -> new EntityException(ErrorCode.ENTITY_NOT_FOUND,
                "Entity with id " + entityId + " not found"));
}
```

### Uniqueness Validation
```java
public void validateUniqueness(String field1, String field2) {
    entityDao.findByField1AndField2(field1, field2)
        .ifPresent(existing -> {
            throw new EntityException(ErrorCode.ENTITY_ALREADY_EXISTS,
                    "Entity with " + field1 + " and " + field2 + " already exists");
        });
}
```

### Optional Returns
```java
// For nullable queries - return Optional
public Optional<Entity> findByExternalId(String externalId) {
    return entityDao.findByExternalId(externalId);
}

// Caller handles Optional
entityValidator.findByExternalId(externalId)
    .ifPresent(entity -> processEntity(entity));
```

## Null/Empty Check Patterns

```java
// Null checks - use Objects class
if (Objects.isNull(value)) { ... }
if (Objects.nonNull(value)) { ... }

// Collection checks - use CollectionUtils
if (CollectionUtils.isEmpty(list)) { return; }
if (!CollectionUtils.isEmpty(list)) { ... }

// String checks - use StringUtils
if (StringUtils.isBlank(str)) { ... }
if (StringUtils.isNotBlank(str)) { ... }

// Default value with ternary
dto = Objects.isNull(dto) ? new Dto() : dto;
list = CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;

// Early return pattern
private void processItems(List<Item> items) {
    if (CollectionUtils.isEmpty(items)) { return; }
    items.forEach(this::processItem);
}
```

## Exception Message Format

Always include identifying information:
```java
// Entity not found
throw new EntityException(ErrorCode.ENTITY_NOT_FOUND,
        "Entity with id " + entityId + " not found");

// Duplicate entity
throw new EntityException(ErrorCode.ENTITY_ALREADY_EXISTS,
        "Parent " + parentId + " already has child with name " + childName);

// Invalid input
throw new EntityException(ErrorCode.INVALID_INPUT,
        "Field 'name' and 'type' are mandatory");

// Business rule violation
throw new EntityException(ErrorCode.OPERATION_NOT_ALLOWED,
        "Cannot delete entity " + entityId + " with active children");
```

## Private Helper Methods

### Naming Conventions
| Pattern | Examples |
|---------|----------|
| `set*()` | `setDefaults()`, `setCreatedBy()` |
| `get*()` | `getCriteria()`, `getAggregation()` |
| `validate*()` | `validateInput()`, `validateUniqueness()` |
| `is*()` | `isValidInput()`, `isActive()` |
| `attach*()` | `attachChildren()`, `attachMetadata()` |
| `create*()` | `createChildItem()`, `createAuditLog()` |
| `transform*()` | `transformItem()`, `transformResponse()` |
| `build*()` | `buildCriteria()`, `buildAggregation()` |
