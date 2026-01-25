# 数据库 Schema 与 Entity 定义 - Development Plan

## Overview
为创意分享平台构建完整的 JPA 数据层架构，包括枚举定义、实体映射、Repository 接口及数据库配置。

## Task Breakdown

### Task 1: 枚举类与类型转换器
- **ID**: task-1
- **type**: default
- **Description**: 创建所有业务枚举类型（UserRole、UserStatus、IdeaStatus、CommentStatus、ReportStatus、TargetType）以及 ImagesConverter 实现 List<String> 与 JSON String 的双向转换
- **File Scope**:
  - `src/main/java/com/learn/demo/enums/UserRole.java`
  - `src/main/java/com/learn/demo/enums/UserStatus.java`
  - `src/main/java/com/learn/demo/enums/IdeaStatus.java`
  - `src/main/java/com/learn/demo/enums/CommentStatus.java`
  - `src/main/java/com/learn/demo/enums/ReportStatus.java`
  - `src/main/java/com/learn/demo/enums/TargetType.java`
  - `src/main/java/com/learn/demo/converter/ImagesConverter.java`
- **Dependencies**: None
- **Test Command**: `./mvnw clean compile -q`
- **Test Focus**:
  - 编译验证所有枚举类定义正确
  - ImagesConverter 的 convertToDatabaseColumn 和 convertToEntityAttribute 方法实现正确

---

### Task 2: JPA 实体类定义
- **ID**: task-2
- **type**: default
- **Description**: 创建所有核心实体类（User、Idea、Tag、Like、Comment、Report），配置 JPA 注解、关联关系、索引和约束；使用 Lombok 简化代码，使用 Hibernate 注解实现自动时间戳
- **File Scope**:
  - `src/main/java/com/learn/demo/entity/User.java`
  - `src/main/java/com/learn/demo/entity/Idea.java`
  - `src/main/java/com/learn/demo/entity/Tag.java`
  - `src/main/java/com/learn/demo/entity/Like.java`
  - `src/main/java/com/learn/demo/entity/Comment.java`
  - `src/main/java/com/learn/demo/entity/Report.java`
- **Dependencies**: depends on task-1
- **Test Command**: `./mvnw clean compile -q`
- **Test Focus**:
  - User 实体：字段映射、枚举转换、索引（username unique、email unique）
  - Idea 实体：ManyToOne 关联 User、ManyToMany 关联 Tag、ImagesConverter 应用、全文索引（title + description）
  - Tag 实体：name 唯一约束
  - Like 实体：UniqueConstraint(user_id, idea_id)、复合索引
  - Comment 实体：ManyToOne 关联 Idea 和 User、status 索引
  - Report 实体：targetType + targetId 复合索引、status 索引
  - @CreationTimestamp 和 @UpdateTimestamp 正确应用

---

### Task 3: Repository 接口定义
- **ID**: task-3
- **type**: default
- **Description**: 为所有实体创建 Spring Data JPA Repository 接口，定义自定义查询方法，包括原生 SQL 全文搜索和分页查询
- **File Scope**:
  - `src/main/java/com/learn/demo/repository/UserRepository.java`
  - `src/main/java/com/learn/demo/repository/IdeaRepository.java`
  - `src/main/java/com/learn/demo/repository/TagRepository.java`
  - `src/main/java/com/learn/demo/repository/LikeRepository.java`
  - `src/main/java/com/learn/demo/repository/CommentRepository.java`
  - `src/main/java/com/learn/demo/repository/ReportRepository.java`
- **Dependencies**: depends on task-2
- **Test Command**: `./mvnw clean compile -q`
- **Test Focus**:
  - UserRepository: findByUsername、existsByUsername、findByEmail 方法签名正确
  - IdeaRepository: findByUserId(Pageable) 方法签名、@Query 原生 SQL FULLTEXT 搜索语法正确
  - TagRepository: findByNameIgnoreCase、findTopNByOrderByUsageCountDesc 方法签名
  - LikeRepository: existsByUserIdAndIdeaId、countByIdeaId 方法签名
  - CommentRepository: findByIdeaIdAndStatus(Pageable) 方法签名
  - ReportRepository: findByTargetTypeAndTargetId、findByStatus 方法签名
  - 所有 Repository 继承 JpaRepository<Entity, Long>

---

### Task 4: 数据库配置与集成测试
- **ID**: task-4
- **type**: default
- **Description**: 配置 MySQL 生产数据源和 H2 测试数据源，编写 @DataJpaTest 测试用例验证所有 Repository 方法功能和数据完整性，确保代码覆盖率 ≥90%
- **File Scope**:
  - `src/main/resources/application.yml`
  - `src/test/resources/application-test.yml`
  - `pom.xml` (添加 H2 test 依赖)
  - `src/test/java/com/learn/demo/repository/UserRepositoryTest.java`
  - `src/test/java/com/learn/demo/repository/IdeaRepositoryTest.java`
  - `src/test/java/com/learn/demo/repository/TagRepositoryTest.java`
  - `src/test/java/com/learn/demo/repository/LikeRepositoryTest.java`
  - `src/test/java/com/learn/demo/repository/CommentRepositoryTest.java`
  - `src/test/java/com/learn/demo/repository/ReportRepositoryTest.java`
- **Dependencies**: depends on task-3
- **Test Command**: `./mvnw clean verify -q`
- **Test Focus**:
  - **MySQL 配置验证**：datasource URL (jdbc:mysql://10.43.5.98:3306/idea_db)、用户名密码、JPA ddl-auto=update
  - **H2 测试配置**：in-memory 模式、兼容 MySQL 模式
  - **UserRepository 测试**：创建用户、按 username 查询、检查用户名存在性、邮箱查询、唯一约束冲突
  - **IdeaRepository 测试**：创建创意、按用户 ID 分页查询、全文搜索（中英文关键词）、标签关联、计数器更新
  - **TagRepository 测试**：创建标签、按名称忽略大小写查询、按使用次数排序查询、唯一约束
  - **LikeRepository 测试**：创建点赞、检查用户-创意点赞存在性、统计创意点赞数、唯一约束冲突
  - **CommentRepository 测试**：创建评论、按创意 ID 和状态分页查询、软删除场景
  - **ReportRepository 测试**：创建举报、按目标类型和 ID 查询、按状态查询、多目标类型场景
  - **级联操作验证**：删除用户时的关联数据处理、删除创意时的点赞/评论清理
  - **时间戳验证**：创建时间自动设置、更新时间自动更新
  - **Jacoco 覆盖率**：LINE 覆盖率 ≥90%

---

## Acceptance Criteria
- [ ] 6 个枚举类定义完整，ImagesConverter 实现正确
- [ ] 6 个实体类包含所有必需字段、关联关系、索引和约束
- [ ] 6 个 Repository 接口包含所有自定义查询方法
- [ ] application.yml 配置 MySQL 数据源（10.43.5.98:3306, user=petefloyd, pass=123zhang）
- [ ] application-test.yml 配置 H2 in-memory 数据源
- [ ] pom.xml 包含 H2 测试依赖（scope=test）
- [ ] 所有 @DataJpaTest 测试用例通过（6 个测试类，覆盖所有 Repository 方法）
- [ ] Jacoco 代码覆盖率 ≥90%（LINE）
- [ ] `./mvnw clean verify` 成功执行无错误

## Technical Notes
- **Spring Boot 版本**: 4.0.1，JPA 使用最新特性
- **数据库方言**: MySQL 8.x，需在 application.yml 配置 `spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect`
- **全文索引**: Idea 实体的 title 和 description 字段需创建 FULLTEXT 索引，使用 `@Index(columnList = "title, description", name = "idx_fulltext")` 并在 MySQL 端手动创建
- **JSON 存储**: Idea.images 字段使用 TEXT 类型存储 JSON 字符串，通过 ImagesConverter 转换
- **软删除**: Comment 和 Idea 使用 status 字段实现软删除，不使用物理删除
- **级联规则**:
  - User → Idea/Comment/Report: CascadeType.ALL (用户删除时清理所有内容)
  - Idea → Like/Comment: CascadeType.REMOVE (创意删除时清理点赞和评论)
  - Tag ↔ Idea: 多对多不级联删除
- **测试隔离**: 每个测试方法使用 @Transactional + @Rollback 确保数据隔离
- **H2 兼容性**: H2 测试环境使用 MySQL 兼容模式 `MODE=MySQL`，但不支持 FULLTEXT，IdeaRepository 的全文搜索测试使用 LIKE 查询模拟
- **时间戳**: 使用 Hibernate 的 @CreationTimestamp 和 @UpdateTimestamp，无需手动设置
- **Lombok**: 使用 @Data、@NoArgsConstructor、@AllArgsConstructor 简化实体类代码，确保 pom.xml 的 maven-compiler-plugin 配置了 Lombok 注解处理器
