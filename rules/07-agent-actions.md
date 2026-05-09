# Agent Actions & Permissions

## Permitted Actions (No Approval Required)

### Domain Code
| Action | Files | Description |
|--------|-------|-------------|
| Create Controller | `*Controller.java` | REST endpoints |
| Create Service | `*Service.java`, `*ServiceImpl.java` | Business logic |
| Create Validator | `*Validator.java` | Validation & queries |
| Create DTO | `*Dto.java` | Request/response models |
| Create Entity | `*Entity.java` | MongoDB documents |
| Create Mapper | `*Mapper.java` | MapStruct interfaces |
| Create Exception | `*Exception.java` | Domain exceptions |
| Modify above | Same files | Bug fixes, enhancements |

### Repository & Data Access
| Action | Files | Description |
|--------|-------|-------------|
| Create Dao | `*Dao.java` | MongoRepository interfaces |
| Add query methods | `*Dao.java` | `findBy*`, `existsBy*`, etc. |
| Add aggregations | `*Validator.java` | MongoTemplate queries |

### Event-Driven Components
| Action | Files | Description |
|--------|-------|-------------|
| Create EventDpo | `*EventDpo.java` | Event data objects |
| Create Publisher | `*EventPublisher.java` | Kafka producers |
| Create Listener | `*EventListener.java` | Kafka consumers |

### Constants & Configuration
| Action | Files | Description |
|--------|-------|-------------|
| Add ErrorCode | `ErrorCode.java` | New error codes to enum |
| Add API endpoint | `ApiEndPointConstants.java` | New REST paths |
| Add collection name | `CollectionNames.java` | New MongoDB collections |
| Add topic | `Topic.java` | New Kafka topics |

### Testing
| Action | Files | Description |
|--------|-------|-------------|
| Create integration test | `*IntegrationTest.java` | Full API tests |
| Create unit test | `*Test.java` | Component tests |
| Create test data | `dtos/**/*.json` | Request/response payloads |
| Create seed data | `mongoDb/integration/*.json` | DB seed files |

### Documentation
| Action | Files | Description |
|--------|-------|-------------|
| Add/update README | `README.md` | Documentation |
| Add API docs | Swagger annotations | OpenAPI specs |

## Restricted Actions (Approval Required)

### ⚠️ Build & Dependencies
| Action | Files | Reason |
|--------|-------|--------|
| Modify dependencies | `pom.xml` | Can break build, security |
| Change Java version | `pom.xml` | Compatibility issues |
| Add plugins | `pom.xml` | Build process impact |

### ⚠️ Security Configuration
| Action | Files | Reason |
|--------|-------|--------|
| Modify security config | `SecurityConfig.java` | Access control |
| Change auth settings | `application*.yml` | Security policies |
| Modify JWT config | Security files | Token handling |
| Add/remove roles | Security annotations | Permission changes |

### ⚠️ Infrastructure Configuration
| Action | Files | Reason |
|--------|-------|--------|
| Change DB connection | `application*.yml` | Data access |
| Modify Kafka config | `application*.yml` | Event streaming |
| Change server ports | `application*.yml` | Deployment |
| Modify credentials | `application*.yml`, env | Security |

### ⚠️ Message Contracts
| Action | Files | Reason |
|--------|-------|--------|
| Rename Kafka topics | `Topic.java` | Consumer impact |
| Change EventDpo structure | `*EventDpo.java` | Breaking changes |
| Modify message format | Event classes | Schema compatibility |

### ⚠️ Data Model Changes
| Action | Files | Reason |
|--------|-------|--------|
| Rename collections | `CollectionNames.java` | Data migration |
| Modify base entities | `MongoDbBaseEntity.java` | All entities affected |
| Change field types | `*Entity.java` | Data compatibility |

### ⚠️ Core Exception Handling
| Action | Files | Reason |
|--------|-------|--------|
| Modify GlobalExceptionHandler | `GlobalExceptionHandler.java` | Error responses |
| Change base exception | `ProjectException.java` | All exceptions affected |
| Modify error response format | Exception handlers | API contract |

## Action Decision Tree

```
Is it a new domain feature?
├── Yes → Create all components (Permitted)
└── No → Is it modifying existing code?
        ├── Yes → Is it pom.xml, security, or config?
        │       ├── Yes → Needs Approval (Restricted)
        │       └── No → Allowed (Permitted)
        └── No → Is it adding to constants (ErrorCode, Topic, etc.)?
                ├── Yes → Allowed (Permitted)
                └── No → Ask user for clarification
```

## Approval Request Format

When approval is needed, provide:

1. **What**: Describe the specific change
2. **Why**: Explain the reason for the change  
3. **Impact**: List affected components
4. **Risk**: Potential issues or rollback steps
5. **Alternative**: Other approaches considered

Example:
```markdown
## Approval Request: Add Redis Dependency

**What**: Add spring-boot-starter-data-redis to pom.xml

**Why**: Implement distributed caching for session management

**Impact**: 
- Build configuration change
- New infrastructure requirement (Redis server)
- Application startup dependency

**Risk**: 
- Build may fail if version incompatible
- Application won't start without Redis

**Alternative**: Could use in-memory cache (Caffeine) instead
```

## Guardrails for Safe Operation

### Always Check Before Modifying
- [ ] Is file in restricted list?
- [ ] Does change affect other services?
- [ ] Does change require migration?
- [ ] Does change affect security?
- [ ] Does change affect API contract?

### Rollback Considerations
- Keep changes atomic and reversible
- Document what was changed
- Test before committing
- Have backout productPlan ready
