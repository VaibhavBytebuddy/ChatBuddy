# Critical Constraints

## Dependency Injection

### ✅ Required Pattern: Constructor Injection
```java
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EntityServiceImpl implements EntityService {
    
    EntityValidator entityValidator;    // Injected via constructor
    EntityMapper entityMapper;          // No @Autowired annotation
    EntityDao entityDao;                // No type annotation needed
}
```

### ❌ Prohibited: Field Injection
```java
// NEVER do this
@Autowired
private EntityValidator entityValidator;  // ❌ Field injection

@Autowired
EntityMapper entityMapper;                 // ❌ Field injection
```

### Why Constructor Injection?
- Makes dependencies explicit
- Easier to test (no reflection needed)
- Immutable after construction
- Fails fast if dependency missing

---

## Naming Requirements

### Fully Qualified Variable Names
```java
// ✅ CORRECT - Full type name as variable name
CustomerOrderEntity customerOrderEntity = dao.findById(id);
CustomerOrderInDto customerOrderInDto = request.getBody();
List<CustomerOrderOutDto> customerOrderOutDtos = mapper.toOutDtos(entities);

// ❌ WRONG - Generic or abbreviated names
Entity entity = dao.findById(id);
InDto dto = request.getBody();
List<OutDto> results = mapper.toOutDtos(entities);
```

### No Inline Comments
```java
// ❌ WRONG - No inline comments
int count = 0; // initialize counter
if (status == ACTIVE) { // check if active
    process(); // do processing
}

// ✅ CORRECT - Self-documenting code
int processedItemCount = 0;
if (isActiveStatus(status)) {
    processActiveItem();
}
```

---

## Null Safety

### Return Types
```java
// ✅ CORRECT - Use Optional for nullable returns
public Optional<Entity> findByExternalId(String externalId) {
    return dao.findByExternalId(externalId);
}

// ❌ WRONG - Never return null
public Entity findByExternalId(String externalId) {
    return dao.findByExternalId(externalId).orElse(null);  // ❌
}
```

### Collection Returns
```java
// ✅ CORRECT - Return empty collection, not null
public List<Entity> findAll() {
    List<Entity> results = dao.findAll();
    return CollectionUtils.isEmpty(results) ? new ArrayList<>() : results;
}

// ❌ WRONG - Never return null collection
public List<Entity> findAll() {
    return null;  // ❌
}
```

### Null Check Patterns
```java
// Standard null checks
if (Objects.isNull(value)) { return; }
if (Objects.nonNull(value)) { process(value); }

// Collection checks
if (CollectionUtils.isEmpty(list)) { return; }
if (!CollectionUtils.isEmpty(list)) { processList(list); }

// String checks
if (StringUtils.isBlank(str)) { return; }
if (StringUtils.isNotBlank(str)) { processString(str); }
```

---

## Code Quality Standards

### Method Length
| Metric | Target | Maximum |
|--------|--------|---------|
| Lines per method | ≤ 5 | 10 |
| Nesting depth | ≤ 2 | 3 |
| Parameters | ≤ 3 | 5 |
| Cyclomatic complexity | ≤ 5 | 10 |

### Line Length
```java
// Maximum 140 characters per line
// Break long lines at logical points:

public FacetSearchOutDto<CustomerOrderOutDto, CustomerOrderEntity> search(
        String customerId,
        CustomerOrderSearchInDto customerOrderSearchInDto) {
    // ...
}
```

### Early Returns
```java
// ✅ CORRECT - Early return pattern
public void process(List<Item> items) {
    if (CollectionUtils.isEmpty(items)) { return; }
    items.forEach(this::processItem);
}

// ❌ WRONG - Unnecessary nesting
public void process(List<Item> items) {
    if (!CollectionUtils.isEmpty(items)) {
        items.forEach(this::processItem);
    }
}
```

### Single Responsibility
```java
// ✅ CORRECT - Each method does one thing
public Entity create(InDto dto) {
    validateInput(dto);
    Entity entity = mapToEntity(dto);
    setDefaults(entity);
    return save(entity);
}

private void validateInput(InDto dto) { /* ... */ }
private Entity mapToEntity(InDto dto) { /* ... */ }
private void setDefaults(Entity entity) { /* ... */ }
private Entity save(Entity entity) { /* ... */ }
```

---

## Formatting Rules

### Braces Required
```java
// ✅ CORRECT - Always use braces
if (condition) {
    doSomething();
}

// ❌ WRONG - No braces
if (condition)
    doSomething();
```

### Blank Lines
```java
// ✅ CORRECT - Logical separation only
public void process() {
    validateInput();
    
    Entity entity = createEntity();
    entity.setDefaults();
    
    save(entity);
    publishEvent(entity);
}

// ❌ WRONG - Excessive blank lines
public void process() {
    validateInput();


    Entity entity = createEntity();

    entity.setDefaults();


    save(entity);
}
```

---

## Prohibited Patterns

### No Hardcoded Values
```java
// ❌ WRONG
if (status.equals("ACTIVE")) { }
String url = "http://localhost:8080/api";

// ✅ CORRECT
if (status.equals(Status.ACTIVE)) { }
String url = appProperties.getApiUrl();
```

### No Duplicated Logic
```java
// ❌ WRONG - Same validation in multiple places
public void createOrder(OrderDto dto) {
    if (StringUtils.isBlank(dto.getName())) {
        throw new ValidationException("Name required");
    }
}

public void updateOrder(OrderDto dto) {
    if (StringUtils.isBlank(dto.getName())) {  // Duplicated
        throw new ValidationException("Name required");
    }
}

// ✅ CORRECT - Extract to validator
public void createOrder(OrderDto dto) {
    orderValidator.validate(dto);
}

public void updateOrder(OrderDto dto) {
    orderValidator.validate(dto);
}
```

---

## Pre-Commit Checklist

Before completing any task, verify:

- [ ] All code compiles (`mvn compile`)
- [ ] Tests pass (`mvn test`)
- [ ] No inline comments in code
- [ ] All variables use fully qualified names
- [ ] No null returns (use Optional or empty collections)
- [ ] Methods are ≤10 lines (ideally ≤5)
- [ ] No hardcoded values
- [ ] No duplicated logic
- [ ] Constructor injection only
- [ ] Braces used for all conditionals
- [ ] Line length ≤140 characters
- [ ] Nesting depth ≤2 levels
