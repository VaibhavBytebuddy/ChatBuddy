# Backend Architecture

## Technology Stack

| Technology | Purpose | Version |
|------------|---------|---------|
| **Spring Boot** | Application framework | 2.7.x / 3.x |
| **Java** | Core language | 17+ |
| **MongoDB** | Document database | 6.0+ |
| **Kafka** | Event streaming | 3.x |
| **MapStruct** | DTO-Entity mapping | 1.5.x |
| **Lombok** | Boilerplate reduction | Latest |
| **JUnit 5** | Testing framework | 5.x |
| **Testcontainers** | Integration testing | Latest |

## Package Structure

```
com.{company}.{project}/
├── common/
│   ├── ApiEndPointConstants.java      # REST endpoint paths
│   ├── CollectionNames.java           # MongoDB collection constants
│   ├── exception/
│   │   ├── ErrorCode.java             # Error code enum
│   │   ├── {Project}Exception.java    # Base exception class
│   │   └── GlobalExceptionHandler.java # @ControllerAdvice
│   └── search/
│       ├── FacetResult.java           # Faceted search results
│       ├── FacetSearchOutDto.java     # Paginated response wrapper
│       └── DeprecatedSearchInDto.java # Base search input
├── core/
│   ├── MongoDbBaseEntity.java         # Base entity with id, version, timestamps
│   └── eventDriven/
│       ├── Topic.java                 # Kafka topic constants
│       ├── EventDpo.java              # Base event data object
│       ├── EventPublisher.java        # Generic event publisher interface
│       └── EventPublisherImpl.java    # Kafka producer implementation
├── domain/
│   ├── {feature}/
│   │   ├── {Feature}Controller.java
│   │   ├── {Feature}Service.java
│   │   ├── {Feature}ServiceImpl.java
│   │   ├── {Feature}Validator.java
│   │   ├── {Feature}Dao.java
│   │   ├── {Feature}Entity.java
│   │   ├── {Feature}Dto.java
│   │   ├── {Feature}Mapper.java
│   │   ├── {Feature}Exception.java
│   │   └── eventDriven/
│   │       ├── {Feature}EventDpo.java
│   │       ├── {Feature}EventPublisher.java
│   │       └── {Feature}EventListener.java
```

## Layered Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Controller Layer                         │
│  - REST endpoints, request/response handling                     │
│  - Input validation annotations (@Valid, @PathVariable)          │
│  - HTTP status codes (@ResponseStatus)                           │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                         Service Layer                            │
│  - Interface + Implementation pattern                            │
│  - Business logic orchestration                                  │
│  - @Transactional for write operations                           │
│  - Delegates to Validator for data access                        │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                        Validator Layer                           │
│  - Data validation and business rules                            │
│  - Complex MongoDB queries (MongoTemplate)                       │
│  - findById with exception handling                              │
│  - Search/aggregation logic                                      │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                          Dao Layer                               │
│  - MongoRepository interface                                     │
│  - Simple CRUD operations                                        │
│  - Query method definitions                                      │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                         Entity Layer                             │
│  - MongoDB document models                                       │
│  - Extends MongoDbBaseEntity                                     │
│  - @Document annotation with collection name                     │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Patterns

### Create Flow
```
Controller.create(InDto) 
  → Service.create(InDto)
    → Validator.validateInput(InDto)
    → Mapper.inDtoToEntity(InDto) → Entity
    → Validator.setDefaults(Entity)
    → Dao.save(Entity)
    → EventPublisher.publish(EventDpo)  # Optional
    → Mapper.entityToOutDto(Entity) → OutDto
```

### Read Flow
```
Controller.find(id)
  → Service.find(id)
    → Validator.findById(id) → Entity
    → Mapper.entityToOutDto(Entity) → OutDto
```

### Update Flow (Patch)
```
Controller.patchUpdate(id, InDto)
  → Service.patchUpdate(id, InDto)
    → Validator.findById(id) → existing Entity
    → Mapper.inDtoToExistingEntity(InDto, Entity)  # Null-safe merge
    → Dao.save(Entity)
    → EventPublisher.publish(EventDpo)  # Optional
    → Mapper.entityToOutDto(Entity) → OutDto
```

### Search Flow
```
Controller.search(parentId, SearchInDto)
  → Service.search(parentId, SearchInDto)
    → Validator.search(parentId, SearchInDto)
      → Build Criteria from SearchInDto
      → MongoTemplate.aggregate(...) → FacetResult
    → Mapper.entitiesToOutDtos(entities) → List<OutDto>
    → FacetSearchOutDto<OutDto, Entity>
```

## Cross-Cutting Concerns

### Exception Handling
- All domain exceptions extend `{Project}Exception`
- `GlobalExceptionHandler` maps exceptions to HTTP responses
- Use `ErrorCode` enum for consistent error codes

### Event-Driven Communication
- Kafka topics defined in `Topic.java`
- Events extend `EventDpo` base class
- Publishers use generic `EventPublisher<T>`
- Listeners use `@KafkaListener` annotation

### Security
- API endpoints secured by Spring Security
- Public APIs use API key header: `api-key`
- Protected APIs use JWT: `Authorization: Bearer {token}`

### Auditing
- `MongoDbBaseEntity` provides:
  - `id` - Document ID
  - `version` - Optimistic locking
  - `createdAt` - Creation timestamp
  - `lastModifiedAt` - Last update timestamp
