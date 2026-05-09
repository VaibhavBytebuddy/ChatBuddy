# Execution Guidelines

## Creating a New Domain Feature - Step by Step

This guide walks through creating a complete new domain feature from scratch, following the project's layered architecture.

---

## Phase 1: Setup Constants & Configuration

### Step 1: Add Collection Name
Location: `com.{company}.{project}.common.CollectionNames`

```java
// Add to CollectionNames.java
public static final String CUSTOMER__ORDER = "customer__order";
```

### Step 2: Add Error Codes
Location: `com.{company}.{project}.common.exception.ErrorCode`

```java
// Add to ErrorCode enum
CUSTOMER_ORDER_NOT_FOUND(100301, "Customer order not found"),
CUSTOMER_ORDER_ALREADY_EXISTS(100302, "Customer order already exists"),
CUSTOMER_ORDER_INVALID_STATUS(100303, "Invalid order status transition"),
```

### Step 3: Add API Endpoint Constant
Location: `com.{company}.{project}.common.ApiEndPointConstants`

```java
// Add to ApiEndPointConstants.Domain class
public static final String API_CUSTOMER_ORDER = "/api/customers/{customerId}/orders";
```

---

## Phase 2: Create Domain Model

### Step 4: Create Entity
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderEntity`

```java
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = CUSTOMER__ORDER)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerOrderEntity extends MongoDbBaseEntity {
    
    String customerId;
    String orderNumber;
    OrderStatus status;
    BigDecimal totalAmount;
    List<OrderItem> items = new ArrayList<>();
    Boolean active = true;
}
```

### Step 5: Create DTOs
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderDto`

```java
public class CustomerOrderDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public abstract static class CustomerOrderCommonDto {
        String orderNumber;
        BigDecimal totalAmount;
        List<OrderItemDto> items;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomerOrderInDto extends CustomerOrderCommonDto {
        // Input-only fields
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomerOrderOutDto extends CustomerOrderCommonDto {
        String id;
        String customerId;
        OrderStatus status;
        LocalDateTime createdAt;
        LocalDateTime lastModifiedAt;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomerOrderSearchInDto extends DeprecatedSearchInDto {
        List<String> filterIds = new ArrayList<>();
        List<OrderStatus> statusFilter = new ArrayList<>();
        Boolean active;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomerOrderFacetResult extends FacetResult<CustomerOrderEntity> {
        List<FacetCount> statusCounts = new ArrayList<>();
    }
}
```

---

## Phase 3: Create Data Access Layer

### Step 6: Create Dao Interface
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderDao`

```java
@Repository
public interface CustomerOrderDao extends MongoRepository<CustomerOrderEntity, String> {
    
    List<CustomerOrderEntity> findByCustomerId(String customerId);
    
    Optional<CustomerOrderEntity> findByCustomerIdAndOrderNumber(
            String customerId, String orderNumber);
    
    long countByCustomerId(String customerId);
}
```

### Step 7: Create Mapper Interface
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderMapper`

```java
@Mapper(componentModel = "spring")
public interface CustomerOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    CustomerOrderEntity inDtoToEntity(CustomerOrderInDto customerOrderInDto);

    CustomerOrderOutDto entityToOutDto(CustomerOrderEntity customerOrderEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    void inDtoToExistingEntity(
            CustomerOrderInDto customerOrderInDto,
            @MappingTarget CustomerOrderEntity customerOrderEntity);
}
```

---

## Phase 4: Create Exception & Validation

### Step 8: Create Exception Class
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderException`

```java
public class CustomerOrderException extends ProjectException {

    public CustomerOrderException(ErrorCode errorCode, String developerMessage) {
        super(errorCode, developerMessage);
    }

    public CustomerOrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
```

### Step 9: Create Validator Class
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderValidator`

```java
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerOrderValidator {

    private static final String CUSTOMER_ID = "customerId";
    private static final String STATUS = "status";
    private static final String ACTIVE = "active";

    CustomerOrderDao customerOrderDao;
    CustomerValidator customerValidator;
    MongoTemplate mongoTemplate;

    public CustomerOrderEntity findById(String customerOrderId) {
        return customerOrderDao.findById(customerOrderId)
                .orElseThrow(() -> new CustomerOrderException(
                        ErrorCode.CUSTOMER_ORDER_NOT_FOUND,
                        "Order with id " + customerOrderId + " not found"));
    }

    public void validateCreate(String customerId, CustomerOrderInDto dto) {
        customerValidator.findById(customerId);
        validateUniqueOrderNumber(customerId, dto.getOrderNumber());
    }

    public void setDefaults(CustomerOrderEntity entity, String customerId) {
        entity.setCustomerId(customerId);
        entity.setStatus(OrderStatus.PENDING);
        entity.setActive(true);
    }

    private void validateUniqueOrderNumber(String customerId, String orderNumber) {
        customerOrderDao.findByCustomerIdAndOrderNumber(customerId, orderNumber)
                .ifPresent(existing -> {
                    throw new CustomerOrderException(
                            ErrorCode.CUSTOMER_ORDER_ALREADY_EXISTS,
                            "Order with number " + orderNumber + " already exists");
                });
    }
}
```

---

## Phase 5: Create Business Logic Layer

### Step 10: Create Service Interface
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderService`

```java
public interface CustomerOrderService {
    
    FacetSearchOutDto<CustomerOrderOutDto, CustomerOrderEntity> search(
            String customerId, CustomerOrderSearchInDto searchInDto);
    
    CustomerOrderOutDto find(String customerOrderId);
    
    CustomerOrderOutDto create(String customerId, CustomerOrderInDto dto);
    
    CustomerOrderOutDto patchUpdate(String customerOrderId, CustomerOrderInDto dto);
    
    void deleteBulk(List<String> customerOrderIds);
}
```

### Step 11: Create ServiceImpl Class
```java
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerOrderServiceImpl implements CustomerOrderService {

    CustomerOrderValidator customerOrderValidator;
    CustomerOrderMapper customerOrderMapper;
    CustomerOrderDao customerOrderDao;
    // ... implement all interface methods
}
```

---

## Phase 6: Create REST Endpoints

### Step 12: Create Controller
Location: `com.{company}.{project}.domain.customer.order.CustomerOrderController`

```java
@Slf4j
@RestController
@RequestMapping(API_CUSTOMER_ORDER)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerOrderController {

    CustomerOrderService customerOrderService;

    // ... implement all REST endpoints
}
```

---

## Phase 7: Create Tests

### Step 13: Create Seed Data
Location: `src/test/resources/mongoDb/integration/G00050__customer__order.json`

### Step 14: Create Test DTOs
Location: `src/test/resources/dtos/customerOrder/`

### Step 15: Create Integration Test
Location: `src/test/java/.../CustomerOrderIntegrationTest.java`

---

## Feature Creation Checklist

```markdown
- [ ] CollectionNames constant added
- [ ] ErrorCode values added
- [ ] ApiEndPointConstants added
- [ ] Entity class created
- [ ] DTO classes created (InDto, OutDto, SearchInDto, FacetResult)
- [ ] Dao interface created
- [ ] Mapper interface created
- [ ] Exception class created
- [ ] Validator class created
- [ ] Service interface created
- [ ] ServiceImpl class created
- [ ] Controller class created
- [ ] Seed data JSON created
- [ ] Test DTO JSONs created
- [ ] Integration test created
- [ ] Build successful (`mvn clean install`)
```
