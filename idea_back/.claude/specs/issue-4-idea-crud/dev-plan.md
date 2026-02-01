# Idea CRUD API - Development Plan

## Overview
Implement complete Idea CRUD operations with JWT authentication, pagination, filtering, tag auto-creation, and soft delete support.

## Task Breakdown

### Task 1: Repository Layer Enhancement
- **ID**: task-1
- **type**: default
- **Description**: Extend IdeaRepository with JpaSpecificationExecutor interface and create IdeaSpecifications utility class for dynamic query building. Add findByUserIdAndIdAndStatus method for ownership verification. Extend LikeRepository with findByUserIdAndIdeaIdIn batch query method.
- **File Scope**:
  - src/main/java/com/learn/demo/repository/IdeaRepository.java
  - src/main/java/com/learn/demo/repository/LikeRepository.java
  - src/main/java/com/learn/demo/specification/IdeaSpecifications.java (new)
  - src/test/java/com/learn/demo/repository/IdeaRepositoryTest.java
  - src/test/java/com/learn/demo/repository/LikeRepositoryTest.java
  - src/test/java/com/learn/demo/specification/IdeaSpecificationsTest.java (new)
- **Dependencies**: None
- **Test Command**: `mvn test -Dtest=IdeaRepositoryTest,LikeRepositoryTest,IdeaSpecificationsTest -Djacoco.skip=false`
- **Test Focus**:
  - JpaSpecificationExecutor integration with IdeaRepository
  - IdeaSpecifications: keyword search (title/description), tag filtering, userId filtering, status exclusion (DELETED)
  - LikeRepository.findByUserIdAndIdeaIdIn: empty list, single idea, multiple ideas, non-existing ideas
  - findByUserIdAndIdAndStatus: author access validation, non-author access denial

### Task 2: DTO Layer
- **ID**: task-2
- **type**: default
- **Description**: Create complete DTO suite for Idea CRUD operations including CreateIdeaRequest (title 1-100, description 1-5000, max 9 images), UpdateIdeaRequest (same validation), IdeaQueryRequest (page, size, sort, keyword, tag, userId), IdeaListResponse (with 200-char description snippet, author info, liked flag), IdeaDetailResponse (full description), and AuthorDto.
- **File Scope**:
  - src/main/java/com/learn/demo/dto/idea/CreateIdeaRequest.java (new)
  - src/main/java/com/learn/demo/dto/idea/UpdateIdeaRequest.java (new)
  - src/main/java/com/learn/demo/dto/idea/IdeaQueryRequest.java (new)
  - src/main/java/com/learn/demo/dto/idea/IdeaListResponse.java (new)
  - src/main/java/com/learn/demo/dto/idea/IdeaDetailResponse.java (new)
  - src/main/java/com/learn/demo/dto/idea/AuthorDto.java (new)
  - src/test/java/com/learn/demo/dto/idea/CreateIdeaRequestTest.java (new)
  - src/test/java/com/learn/demo/dto/idea/UpdateIdeaRequestTest.java (new)
  - src/test/java/com/learn/demo/dto/idea/IdeaQueryRequestTest.java (new)
  - src/test/java/com/learn/demo/dto/idea/IdeaListResponseTest.java (new)
  - src/test/java/com/learn/demo/dto/idea/IdeaDetailResponseTest.java (new)
  - src/test/java/com/learn/demo/dto/idea/AuthorDtoTest.java (new)
- **Dependencies**: None
- **Test Command**: `mvn test -Dtest=CreateIdeaRequestTest,UpdateIdeaRequestTest,IdeaQueryRequestTest,IdeaListResponseTest,IdeaDetailResponseTest,AuthorDtoTest -Djacoco.skip=false`
- **Test Focus**:
  - CreateIdeaRequest validation: title length (0, 1, 100, 101), description length (0, 1, 5000, 5001), images count (0, 9, 10), null fields
  - UpdateIdeaRequest: same validation rules as create
  - IdeaQueryRequest: default values (page=0, size=20), size max limit (100), invalid sort fields, null keyword/tag handling
  - IdeaListResponse: description substring logic (199, 200, 201 chars), null images/tags handling, author mapping, liked flag
  - IdeaDetailResponse: full description preservation, null fields handling
  - AuthorDto: null-safe field mapping

### Task 3: Service Layer
- **ID**: task-3
- **type**: default
- **Description**: Implement IdeaService with full CRUD logic including: paginated list query with dynamic filtering (keyword, tag, userId, status exclusion), detail retrieval, creation with tag auto-creation (findOrCreate + usageCount increment), update with tag sync (decrement old tags, increment new tags), soft delete (status=DELETED), and batch liked status query using LikeRepository.findByUserIdAndIdeaIdIn. Add permission checks (403 for non-author update/delete, admin bypass for delete).
- **File Scope**:
  - src/main/java/com/learn/demo/service/IdeaService.java (new)
  - src/main/java/com/learn/demo/repository/TagRepository.java
  - src/test/java/com/learn/demo/service/IdeaServiceTest.java (new)
- **Dependencies**: depends on task-1, task-2
- **Test Command**: `mvn test -Dtest=IdeaServiceTest -Djacoco.skip=false`
- **Test Focus**:
  - listIdeas: empty result, pagination (first/middle/last page), keyword filtering (title match, description match, case sensitivity), tag filtering (single/multiple tags), userId filtering, status exclusion (DELETED not returned), sort by createdAt/likeCount, liked flag batch query (no likes, partial likes, all liked)
  - getIdeaDetail: existing idea, non-existing idea (404), DELETED idea (404), liked status
  - createIdea: successful creation, new tag auto-creation with usageCount=1, existing tag reuse with usageCount++, max images validation
  - updateIdea: author update success, non-author update (403), tag sync (old tags usageCount--, new tags usageCount++), non-existing idea (404)
  - deleteIdea: author soft delete, admin soft delete, non-author non-admin (403), status set to DELETED, non-existing idea (404)
  - getCurrentUserIdeas: pagination, only current user's ideas returned

### Task 4: Controller Layer and Security Configuration
- **ID**: task-4
- **type**: default
- **Description**: Implement IdeaController with endpoints: GET /api/ideas (paginated list with query params), GET /api/ideas/{id} (detail), POST /api/ideas (create), PUT /api/ideas/{id} (update), DELETE /api/ideas/{id} (soft delete), GET /api/users/me/ideas (current user's ideas). Update SecurityConfig to permit all authenticated users for idea endpoints and require admin role for delete operation (handled in service layer). All endpoints return ApiResponse or PageResponse wrappers.
- **File Scope**:
  - src/main/java/com/learn/demo/controller/IdeaController.java (new)
  - src/main/java/com/learn/demo/config/SecurityConfig.java
  - src/test/java/com/learn/demo/controller/IdeaControllerTest.java (new)
- **Dependencies**: depends on task-3
- **Test Command**: `mvn test -Dtest=IdeaControllerTest -Djacoco.skip=false`
- **Test Focus**:
  - GET /api/ideas: 200 with PageResponse, query param binding (page, size, sort, keyword, tag, userId), JWT authentication (401 without token)
  - GET /api/ideas/{id}: 200 with ApiResponse, 404 for non-existing, 401 without JWT
  - POST /api/ideas: 200 with created idea, 400 for validation errors (title/description length, max images), 401 without JWT
  - PUT /api/ideas/{id}: 200 on success, 403 for non-author, 400 for validation errors, 404 for non-existing, 401 without JWT
  - DELETE /api/ideas/{id}: 200 on success, 403 for non-author non-admin, 404 for non-existing, 401 without JWT
  - GET /api/users/me/ideas: 200 with PageResponse, pagination params, 401 without JWT
  - Response format consistency (ApiResponse/PageResponse structure)

### Task 5: Integration Testing and Coverage Verification
- **ID**: task-5
- **type**: default
- **Description**: Create comprehensive integration tests covering end-to-end workflows: user creates idea with tags, lists ideas with various filters, updates idea with tag changes, soft deletes idea, verifies idea not in list after deletion. Add edge case tests for concurrent tag creation, large dataset pagination, and permission boundary conditions. Verify overall code coverage meets 90% threshold.
- **File Scope**:
  - src/test/java/com/learn/demo/integration/IdeaCrudIntegrationTest.java (new)
  - All source files in src/main/java/com/learn/demo/** related to Idea CRUD
- **Dependencies**: depends on task-3, task-4
- **Test Command**: `mvn clean test verify -Djacoco.skip=false`
- **Test Focus**:
  - End-to-end workflow: create → list (verify in results) → update → detail (verify changes) → delete → list (verify excluded)
  - Tag lifecycle: new tag creation, existing tag reuse, tag count sync on update/delete
  - Pagination edge cases: empty result, single page, multiple pages, last page partial results
  - Permission enforcement: author-only update, admin delete bypass, non-owner 403
  - Liked status accuracy: batch query correctness, user-specific liked flags
  - Description snippet: 200-char truncation in list vs full text in detail
  - Overall coverage report: verify ≥90% line coverage for all Idea CRUD modules

## Acceptance Criteria
- [ ] All 6 API endpoints implemented and functional (GET list, GET detail, POST create, PUT update, DELETE soft delete, GET user ideas)
- [ ] Pagination works with configurable page, size (max 100), and sort (createdAt|likeCount)
- [ ] Dynamic filtering by keyword (title/description), tag, userId, with DELETED status exclusion
- [ ] Tag auto-creation and usageCount synchronization on create/update/delete
- [ ] Description snippet (200 chars) in list response, full description in detail response
- [ ] Liked flag batch querying using findByUserIdAndIdeaIdIn
- [ ] Permission enforcement: author-only update, author/admin delete (403 for others)
- [ ] JWT authentication required for all endpoints (401 without token)
- [ ] All validation rules enforced (title 1-100, description 1-5000, max 9 images)
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Code coverage ≥90%

## Technical Notes
- **JPA Specifications**: Use Specification API for dynamic query building to avoid repository method explosion
- **Tag Management**: Implement findOrCreate pattern with optimistic locking to prevent duplicate tags during concurrent creation
- **Batch Liked Query**: Collect all idea IDs from page result, query LikeRepository once with findByUserIdAndIdeaIdIn, map results to idea IDs to avoid N+1 queries
- **Description Snippet**: Use database substring function in Specification for list query to reduce payload size, or implement in service layer if database function is not portable
- **Soft Delete**: Never physically delete Idea records; set status=DELETED and exclude from all public queries
- **Permission Check**: Implement in service layer, not controller, to ensure business logic encapsulation
- **Test Data Isolation**: Use @Transactional with rollback in tests to prevent cross-test contamination
- **Coverage Threshold**: Configured in pom.xml jacoco-maven-plugin with 90% line coverage minimum
