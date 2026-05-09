# Naming Conventions

## Component Naming Table

| Component | Pattern | Example | Notes |
|-----------|---------|---------|-------|
| **Controller** | `*Controller` | `CustomerOrderController` | REST endpoint handler |
| **Service Interface** | `*Service` | `CustomerOrderService` | Business logic contract |
| **Service Impl** | `*ServiceImpl` | `CustomerOrderServiceImpl` | Business logic implementation |
| **Repository** | `*Dao` | `CustomerOrderDao` | MongoDB repository |
| **Entity** | `*Entity` | `CustomerOrderEntity` | MongoDB document model |
| **DTO Container** | `*Dto` | `CustomerOrderDto` | Outer class containing DTOs |
| **Input DTO** | `*InDto` | `CustomerOrderInDto` | Request payload |
| **Output DTO** | `*OutDto` | `CustomerOrderOutDto` | Response payload |
| **Search DTO** | `*SearchInDto` | `CustomerOrderSearchInDto` | Search filter input |
| **Facet Result** | `*FacetResult` | `CustomerOrderFacetResult` | Aggregation result |
| **Mapper** | `*Mapper` | `CustomerOrderMapper` | MapStruct interface |
| **Validator** | `*Validator` | `CustomerOrderValidator` | Validation & data access |
| **Exception** | `*Exception` | `CustomerOrderException` | Domain exception |
| **Event DPO** | `*EventDpo` | `CustomerOrderEventDpo` | Kafka event payload |
| **Event Publisher** | `*EventPublisher` | `CustomerOrderEventPublisher` | Kafka producer interface |
| **Event Listener** | `*EventListener` | `CustomerOrderEventListener` | Kafka consumer |

## Constant Naming

### Collection Names
Location: `com.{company}.{project}.common.CollectionNames`

```java
public class CollectionNames {
    // Pattern: DOMAIN__ENTITY (double underscore)
    public static final String CUSTOMER__ORDER = "customer__order";
    public static final String ORDER__ITEM = "order__item";
    public static final String USER__ACCOUNT = "user__account";
    public static final String TALENT__SUBSCRIPTION = "talent__subscription";
}
```

### API Endpoint Constants
Location: `com.{company}.{project}.common.ApiEndPointConstants`

```java
public class ApiEndPointConstants {
    
    public static class Domain {
        // Pattern: API_{ENTITY} with nested paths
        public static final String API_CUSTOMER_ORDER = "/api/customers/{customerId}/orders";
        public static final String API_ORDER_ITEM = "/api/orders/{orderId}/items";
        public static final String API_USER_ACCOUNT = "/api/users/{userId}/account";
    }
    
    public static class Public {
        // Pattern: API_PUBLIC_{ENTITY}
        public static final String API_PUBLIC_PRODUCT = "/api/public/products";
        public static final String API_PUBLIC_CATALOG = "/api/public/catalog";
    }
}
```

### Kafka Topics
Location: `com.{company}.{project}.core.eventDriven.Topic`

```java
public class Topic {
    // Pattern: {Domain}Topic
    public static final String ORDER_TOPIC = "OrderTopic";
    public static final String ACCOUNT_TOPIC = "AccountTopic";
    public static final String FIRESTORE_TOPIC = "FirestoreTopic";
}
```

## Variable Naming Patterns

### Method Parameters
```java
// Single entity operations
void create(CustomerOrderInDto customerOrderInDto)
void find(String customerOrderId)
void patchUpdate(String customerOrderId, CustomerOrderInDto customerOrderInDto)

// Bulk operations
void createBulk(List<CustomerOrderInDto> customerOrderInDtos)
void deleteBulk(List<String> customerOrderIds)

// Search operations
FacetSearchOutDto search(String customerId, CustomerOrderSearchInDto customerOrderSearchInDto)
```

### Local Variables
```java
// Entity variables - use full type name
CustomerOrderEntity customerOrderEntity = dao.findById(id);
List<CustomerOrderEntity> customerOrderEntities = dao.findByCustomerId(customerId);

// DTO variables
CustomerOrderInDto customerOrderInDto = new CustomerOrderInDto();
CustomerOrderOutDto customerOrderOutDto = mapper.entityToOutDto(entity);
List<CustomerOrderOutDto> customerOrderOutDtos = entities.stream().map(...).toList();

// Search variables
CustomerOrderSearchInDto customerOrderSearchInDto = request.getSearchInDto();
CustomerOrderFacetResult customerOrderFacetResult = validator.search(customerId, searchInDto);
```

### Field Constants
```java
// MongoDB field names - match entity field names exactly
private static final String _ID = "_id";
private static final String CUSTOMER_ID = "customerId";
private static final String ORDER_STATUS = "orderStatus";
private static final String CREATED_AT = "createdAt";
private static final String TOTAL_AMOUNT = "totalAmount";
```

### Consumer Group IDs
```java
// Pattern: {domain}_group
private static final String GROUP_ID = "order_group";
private static final String FIRESTORE_SYNC_GROUP = "firestore_sync_group";
private static final String ANALYTICS_GROUP = "analytics_group";
```

## Package Naming

```
com.{company}.{project}/
├── common/                     # Shared utilities
│   ├── exception/              # Exception classes
│   ├── search/                 # Search utilities
│   └── util/                   # Helper utilities
├── core/                       # Core infrastructure
│   └── eventDriven/            # Kafka infrastructure
└── domain/                     # Domain packages
    ├── customer/               # Customer domain
    │   ├── order/              # Nested: CustomerOrder
    │   └── profile/            # Nested: CustomerProfile
    └── product/                # Product domain
        └── inventory/          # Nested: ProductInventory
```

## File Naming in Tests

### Test Classes
```
{Feature}IntegrationTest.java
{Feature}ServiceTest.java
{Feature}ValidatorTest.java
```

### Test Resources
```
dtos/{domain}/
├── POST_{Entity}InDto.json
├── POST_{Entity}OutDto.json
├── GET_{Entity}OutDto.json
├── PATCH_{Entity}InDto.json
├── PATCH_{Entity}OutDto.json
├── POST_{Entity}SearchInDto.json
└── DELETE_{Entity}Ids.json
```

### Seed Data Files
```
mongoDb/integration/
├── G00001__{collection_name}.json
├── G00002__{collection_name}.json
└── G00003__{collection_name}.json
```

## Abbreviations to Avoid

| ❌ Don't Use | ✅ Use Instead |
|--------------|----------------|
| `dto` | `customerOrderInDto` |
| `entity` | `customerOrderEntity` |
| `id` | `customerOrderId` |
| `req` | `customerOrderRequest` |
| `res` | `customerOrderResponse` |
| `e` | `customerOrderEntity` |
| `svc` | `customerOrderService` |
| `repo` | `customerOrderDao` |
