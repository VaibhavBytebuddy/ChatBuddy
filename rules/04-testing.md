# Testing Rules

## Test Infrastructure

### Base Test Class
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"localhost", "test"})
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseProjectApplicationTests {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Container
    static MongoDBAtlasLocalContainer mongoDBContainer = new MongoDBAtlasLocalContainer();

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getConnectionString);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    protected void assertResponse(ResultActions resultActions, Resource expectedResource) throws Exception {
        String expectedJson = new String(expectedResource.getInputStream().readAllBytes());
        resultActions.andExpect(content().json(expectedJson, false));
    }

    protected String loadResource(Resource resource) throws IOException {
        return new String(resource.getInputStream().readAllBytes());
    }
}
```

### Test Configuration Class
```java
@TestConfiguration
public class ProjectTestConfiguration {

    @Bean
    public ProjectUserContext projectUserContext() {
        return new ProjectUserContext();
    }

    @Bean
    public ProjectProperties projectProperties() {
        ProjectProperties props = new ProjectProperties();
        props.setPublicApiKey("test-api-key");
        return props;
    }
}
```

## Authentication in Tests

### Public APIs (No auth or API key)
```java
// Using API key header
mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .header("api-key", projectProperties.getPublicApiKey()))
        .andExpect(status().isOk());
```

### Protected APIs (JWT token)
```java
@Autowired
ProjectUserContext projectUserContext;

// Using Authorization header
mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}", TEST_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", projectUserContext.getToken()))
        .andExpect(status().isOk());
```

## Complete Integration Test Template

```java
class EntityIntegrationTest extends BaseProjectApplicationTests {

    // API endpoints
    private static final String BASE_URL = "/api/parent/{parentId}/entities";
    private static final String ENTITY_URL = BASE_URL + "/{entityId}";
    private static final String SEARCH_URL = BASE_URL + "/search";
    private static final String BULK_URL = BASE_URL + "/bulk";

    // Pre-seeded test data IDs (from mongoDb/integration/)
    private static final String TEST_PARENT_ID = "PARENT_001";
    private static final String EXISTING_ENTITY_ID = "ENTITY_001";
    private static final String ANOTHER_ENTITY_ID = "ENTITY_002";

    // Request/Response JSON resources
    @Value("classpath:dtos/entity/POST_EntityInDto.json")
    Resource POST_EntityInDto;

    @Value("classpath:dtos/entity/POST_EntityOutDto.json")
    Resource POST_EntityOutDto;

    @Value("classpath:dtos/entity/GET_EntityOutDto.json")
    Resource GET_EntityOutDto;

    @Value("classpath:dtos/entity/PATCH_EntityInDto.json")
    Resource PATCH_EntityInDto;

    @Value("classpath:dtos/entity/PATCH_EntityOutDto.json")
    Resource PATCH_EntityOutDto;

    @Value("classpath:dtos/entity/POST_EntitySearchInDto.json")
    Resource POST_EntitySearchInDto;

    @Value("classpath:dtos/entity/DELETE_EntityIds.json")
    Resource DELETE_EntityIds;

    @Autowired
    ProjectUserContext projectUserContext;

    @Test
    @Order(1)
    void search() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(SEARCH_URL, TEST_PARENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", projectUserContext.getToken())
                        .content(loadResource(POST_EntitySearchInDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").isNumber());
    }

    @Test
    @Order(2)
    void find() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(ENTITY_URL, TEST_PARENT_ID, EXISTING_ENTITY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", projectUserContext.getToken()))
                .andDo(print())
                .andExpect(status().isOk());

        assertResponse(resultActions, GET_EntityOutDto);
    }

    @Test
    @Order(3)
    void findForNonExistentId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENTITY_URL, TEST_PARENT_ID, "NON_EXISTENT_ID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", projectUserContext.getToken()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void create() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.post(BASE_URL, TEST_PARENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", projectUserContext.getToken())
                        .content(loadResource(POST_EntityInDto)))
                .andDo(print())
                .andExpect(status().isCreated());

        assertResponse(resultActions, POST_EntityOutDto);
    }

    @Test
    @Order(5)
    void patchUpdate() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.patch(ENTITY_URL, TEST_PARENT_ID, EXISTING_ENTITY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", projectUserContext.getToken())
                        .content(loadResource(PATCH_EntityInDto)))
                .andDo(print())
                .andExpect(status().isOk());

        assertResponse(resultActions, PATCH_EntityOutDto);
    }

    @Test
    @Order(6)
    void deleteBulk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BULK_URL, TEST_PARENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", projectUserContext.getToken())
                        .content(loadResource(DELETE_EntityIds)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
```

## Test Data Directory Structure

```
src/test/resources/
├── application-test.yml              # Test-specific config
├── mongoDb/
│   └── integration/                  # Seed data (loaded before tests)
│       ├── G00001__parent.json       # Parent collection seed
│       ├── G00002__entity.json       # Entity collection seed
│       └── G00003__child.json        # Child collection seed
└── dtos/
    └── entity/                       # Test payloads by domain
        ├── POST_EntityInDto.json     # Create request
        ├── POST_EntityOutDto.json    # Expected create response
        ├── POST_EntityInDtos.json    # Bulk create request
        ├── POST_EntitySearchInDto.json  # Search request
        ├── GET_EntityOutDto.json     # Expected find response
        ├── PATCH_EntityInDto.json    # Update request
        ├── PATCH_EntityOutDto.json   # Expected update response
        └── DELETE_EntityIds.json     # Bulk delete IDs
```

## Test Data File Examples

### Seed Data: `G00002__entity.json`
```json
[
  {
    "_id": "ENTITY_001",
    "parentId": "PARENT_001",
    "name": "Test Entity One",
    "description": "First test entity",
    "active": true,
    "status": "ACTIVE",
    "createdAt": {"$date": "2024-01-01T00:00:00Z"},
    "lastModifiedAt": {"$date": "2024-01-01T00:00:00Z"},
    "_class": "com.company.project.domain.entity.EntityEntity"
  },
  {
    "_id": "ENTITY_002",
    "parentId": "PARENT_001",
    "name": "Test Entity Two",
    "active": true,
    "status": "DRAFT"
  }
]
```

### Request DTO: `POST_EntityInDto.json`
```json
{
  "name": "New Test Entity",
  "description": "Created in test",
  "active": true
}
```

### Expected Response: `POST_EntityOutDto.json`
```json
{
  "name": "New Test Entity",
  "description": "Created in test",
  "active": true,
  "parentId": "PARENT_001"
}
```

### Search Request: `POST_EntitySearchInDto.json`
```json
{
  "pageNumber": 0,
  "pageLimit": 10,
  "sortBy": "createdAt",
  "sortOrder": "DESC",
  "active": true
}
```

## Test Naming Conventions

| Method Name | Purpose |
|-------------|---------|
| `search()` | Search with filters |
| `find()` | Get single entity |
| `findForNonExistentId()` | 404 error case |
| `create()` | Create new entity |
| `createForInvalidInput()` | 400 validation error |
| `createBulk()` | Bulk create |
| `patchUpdate()` | Partial update |
| `deleteBulk()` | Bulk delete |

## Test Assertions

```java
// Status assertions
.andExpect(status().isOk())
.andExpect(status().isCreated())
.andExpect(status().isNoContent())
.andExpect(status().isNotFound())
.andExpect(status().isBadRequest())

// JSON path assertions
.andExpect(jsonPath("$.id").exists())
.andExpect(jsonPath("$.name").value("Expected Name"))
.andExpect(jsonPath("$.items").isArray())
.andExpect(jsonPath("$.items.length()").value(5))
.andExpect(jsonPath("$.totalItems").isNumber())

// Custom response comparison
assertResponse(resultActions, expectedResource);
```

## Key Testing Principles

1. **Use `@Order(n)`** to control execution sequence
2. **Use pre-seeded IDs** from `mongoDb/integration/` files
3. **Avoid extracting dynamic IDs** from responses
4. **Resource injection** for all JSON payloads
5. **Use `assertResponse()`** for full response comparison
6. **Separate error case tests** with descriptive names
7. **Always `.andDo(print())`** for debugging visibility
