# Code Structure Rules

## Controller Layer

### Standard Controller Template
```java
@Slf4j
@RestController
@RequestMapping(API_ENTITY)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EntityController {

    EntityService entityService;

    // POST /search - Faceted search with pagination
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public FacetSearchOutDto<EntityOutDto, EntityEntity> search(
            @PathVariable String parentId,
            @RequestBody EntitySearchInDto entitySearchInDto) {
        return entityService.search(parentId, entitySearchInDto);
    }

    // GET /{id} - Find single entity
    @GetMapping("/{entityId}")
    @ResponseStatus(HttpStatus.OK)
    public EntityOutDto find(@PathVariable String entityId) {
        return entityService.find(entityId);
    }

    // POST - Create single entity
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityOutDto create(
            @PathVariable String parentId,
            @RequestBody EntityInDto entityInDto) {
        return entityService.create(parentId, entityInDto);
    }

    // POST /bulk - Create multiple entities
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void createBulk(
            @PathVariable String parentId,
            @RequestBody List<EntityInDto> entityInDtos) {
        entityService.createBulk(parentId, entityInDtos);
    }

    // PATCH /{id} - Partial update
    @PatchMapping("/{entityId}")
    @ResponseStatus(HttpStatus.OK)
    public EntityOutDto patchUpdate(
            @PathVariable String entityId,
            @RequestBody EntityInDto entityInDto) {
        return entityService.patchUpdate(entityId, entityInDto);
    }

    // DELETE /bulk - Delete multiple entities
    @DeleteMapping("/bulk")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBulk(@RequestBody List<String> entityIds) {
        entityService.deleteBulk(entityIds);
    }
}
```

### Controller Best Practices
- Use `@PathVariable` for URL parameters
- Use `@RequestBody` for JSON payloads
- Always specify `@ResponseStatus`
- Delegate all logic to Service layer
- No business logic in controllers

## Service Layer

### Service Interface
```java
public interface EntityService {
    
    FacetSearchOutDto<EntityOutDto, EntityEntity> search(
            String parentId, EntitySearchInDto entitySearchInDto);
    
    EntityOutDto find(String entityId);
    
    EntityOutDto create(String parentId, EntityInDto entityInDto);
    
    void createBulk(String parentId, List<EntityInDto> entityInDtos);
    
    EntityOutDto patchUpdate(String entityId, EntityInDto entityInDto);
    
    void deleteBulk(List<String> entityIds);
}
```

### Service Implementation
```java
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EntityServiceImpl implements EntityService {

    EntityValidator entityValidator;
    EntityMapper entityMapper;
    EntityDao entityDao;
    FacetSearchMapper<EntityOutDto, EntityEntity> facetSearchMapper;
    
    // Optional: Event publisher for cross-service communication
    EntityEventPublisher entityEventPublisher;

    @Override
    public FacetSearchOutDto<EntityOutDto, EntityEntity> search(
            String parentId, EntitySearchInDto entitySearchInDto) {
        EntityFacetResult facetResult = entityValidator.search(parentId, entitySearchInDto);
        List<EntityOutDto> outDtos = facetResult.getItems().stream()
                .map(entityMapper::entityToOutDto)
                .toList();
        return facetSearchMapper.toFacetSearchOutDto(outDtos, facetResult);
    }

    @Override
    public EntityOutDto find(String entityId) {
        EntityEntity entityEntity = entityValidator.findById(entityId);
        return entityMapper.entityToOutDto(entityEntity);
    }

    @Override
    @Transactional
    public EntityOutDto create(String parentId, EntityInDto entityInDto) {
        entityValidator.validateCreate(parentId, entityInDto);
        EntityEntity entityEntity = entityMapper.inDtoToEntity(entityInDto);
        entityValidator.setDefaults(entityEntity, parentId);
        EntityEntity saved = entityDao.save(entityEntity);
        entityEventPublisher.publishCreated(saved);
        return entityMapper.entityToOutDto(saved);
    }

    @Override
    @Transactional
    public void createBulk(String parentId, List<EntityInDto> entityInDtos) {
        if (CollectionUtils.isEmpty(entityInDtos)) { return; }
        entityInDtos.stream()
                .filter(Objects::nonNull)
                .forEach(dto -> create(parentId, dto));
    }

    @Override
    @Transactional
    public EntityOutDto patchUpdate(String entityId, EntityInDto entityInDto) {
        EntityEntity existing = entityValidator.findById(entityId);
        entityMapper.inDtoToExistingEntity(entityInDto, existing);
        EntityEntity saved = entityDao.save(existing);
        entityEventPublisher.publishUpdated(saved);
        return entityMapper.entityToOutDto(saved);
    }

    @Override
    @Transactional
    public void deleteBulk(List<String> entityIds) {
        if (CollectionUtils.isEmpty(entityIds)) { return; }
        entityIds.stream()
                .filter(StringUtils::isNotBlank)
                .forEach(entityDao::deleteById);
    }
}
```

### Transaction Rules
| Operation | @Transactional |
|-----------|---------------|
| search | ❌ No |
| find | ❌ No |
| create | ✅ Yes |
| createBulk | ✅ Yes |
| patchUpdate | ✅ Yes |
| deleteBulk | ✅ Yes |

## DTO Structure

```java
public class EntityDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public abstract static class EntityCommonDto {
        String name;
        String description;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntityInDto extends EntityCommonDto { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntityOutDto extends EntityCommonDto {
        String id;
        LocalDateTime createdAt;
        LocalDateTime lastModifiedAt;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntitySearchInDto extends DeprecatedSearchInDto {
        List<String> filterIds = new ArrayList<>();
        Boolean active;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntityFacetResult extends FacetResult<EntityEntity> {
        List<FacetCount> statusCounts = new ArrayList<>();
    }
}
```

## Entity Structure

```java
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = ENTITY_COLLECTION)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityEntity extends MongoDbBaseEntity {
    String parentId;
    String name;
    Boolean active = true;
    EntityStatus status;
    List<ChildItem> children = new ArrayList<>();
}
```

## Mapper Interface

```java
@Mapper(componentModel = "spring", uses = {ChildMapper.class})
public interface EntityMapper {

    // InDto to Entity - New creation
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "parentId", ignore = true)  // Set in Validator
    @Mapping(target = "status", ignore = true)     // Set in Validator
    EntityEntity inDtoToEntity(EntityInDto entityInDto);

    // Entity to OutDto - Response mapping
    @Mapping(target = "children", source = "children")
    EntityOutDto entityToOutDto(EntityEntity entityEntity);

    // InDto to existing Entity - Patch update (null-safe)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    void inDtoToExistingEntity(EntityInDto entityInDto, @MappingTarget EntityEntity entityEntity);

    // Bulk mapping
    List<EntityOutDto> entitiesToOutDtos(List<EntityEntity> entities);
}
```

## Validator Structure

```java
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EntityValidator {

    // Field constants
    private static final String _ID = "_id";
    private static final String PARENT_ID = "parentId";
    private static final String STATUS = "status";
    private static final String ACTIVE = "active";
    private static final String NAME = "name";

    // Dependencies
    EntityDao entityDao;
    MongoTemplate mongoTemplate;
    ParentValidator parentValidator;

    // Find by ID with exception
    public EntityEntity findById(String entityId) {
        return entityDao.findById(entityId)
                .orElseThrow(() -> new EntityException(ErrorCode.ENTITY_NOT_FOUND,
                        "Entity with id " + entityId + " not found"));
    }

    // Validate create input
    public void validateCreate(String parentId, EntityInDto entityInDto) {
        parentValidator.findById(parentId);  // Verify parent exists
        validateUniqueName(parentId, entityInDto.getName());
    }

    // Set default values
    public void setDefaults(EntityEntity entityEntity, String parentId) {
        entityEntity.setParentId(parentId);
        entityEntity.setStatus(EntityStatus.DRAFT);
        entityEntity.setActive(true);
    }

    // Validate uniqueness
    private void validateUniqueName(String parentId, String name) {
        entityDao.findByParentIdAndName(parentId, name)
                .ifPresent(existing -> {
                    throw new EntityException(ErrorCode.ENTITY_ALREADY_EXISTS,
                            "Entity with name " + name + " already exists");
                });
    }

    // Faceted search with aggregation
    public EntityFacetResult search(String parentId, EntitySearchInDto searchInDto) {
        Criteria criteria = buildCriteria(parentId, searchInDto);
        // ... aggregation logic
        return facetResult;
    }

    private Criteria buildCriteria(String parentId, EntitySearchInDto searchInDto) {
        Criteria criteria = Criteria.where(PARENT_ID).is(parentId);
        if (Objects.nonNull(searchInDto.getActive())) {
            criteria.and(ACTIVE).is(searchInDto.getActive());
        }
        if (!CollectionUtils.isEmpty(searchInDto.getFilterIds())) {
            criteria.and(_ID).in(searchInDto.getFilterIds());
        }
        return criteria;
    }
}
```

## Dao (Repository) Interface

```java
@Repository
public interface EntityDao extends MongoRepository<EntityEntity, String> {

    // Standard finder by parent
    List<EntityEntity> findByParentId(String parentId);

    // Optional finder for uniqueness check
    Optional<EntityEntity> findByParentIdAndName(String parentId, String name);

    // Finder with multiple conditions
    List<EntityEntity> findByParentIdAndActiveTrue(String parentId);

    // Count queries
    long countByParentId(String parentId);

    // Existence check
    boolean existsByParentIdAndName(String parentId, String name);

    // Delete by parent
    void deleteByParentId(String parentId);
}
```
