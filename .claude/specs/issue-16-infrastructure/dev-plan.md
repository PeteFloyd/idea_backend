# Spring Boot 基础设施搭建 - Development Plan

## Overview
搭建 Spring Boot 项目的基础架构，包括 CORS 配置、分页参数、统一响应与异常处理体系，以及单元测试覆盖率保障（≥90%）。

## Task Breakdown

### Task 1: 依赖与配置迁移
- **ID**: task-1
- **type**: default
- **Description**: 添加 validation 依赖到 pom.xml，将 application.properties 迁移为 application.yml 格式，配置 CORS（允许所有来源）、分页默认值（默认 10，最大 50）、Jackson 日期格式（ISO8601）
- **File Scope**: pom.xml, src/main/resources/application.yml
- **Dependencies**: None
- **Test Command**: `./mvnw -q clean compile -DskipTests`
- **Test Focus**:
  - 验证项目编译通过
  - 确认 application.yml 格式正确
  - 检查依赖版本兼容性

### Task 2: 基础包结构与配置类
- **ID**: task-2
- **type**: default
- **Description**: 创建 config/ 包，实现 WebConfig（分页参数配置）、CorsConfig（从 application.yml 读取 CORS 设置）、JacksonConfig（日期序列化配置）
- **File Scope**: src/main/java/com/learn/demo/config/**
- **Dependencies**: depends on task-1
- **Test Command**: `./mvnw -q test -Dtest=*Config*Test -Djacoco.skip=true`
- **Test Focus**:
  - WebConfig 正确注册分页参数解析器，验证默认值 10、最大值 50
  - CorsConfig 正确配置跨域策略（允许所有来源、方法、请求头）
  - JacksonConfig 正确配置 ObjectMapper（ISO8601 日期格式、忽略未知属性）
  - Spring Context 能够成功加载所有配置类

### Task 3: 统一响应与异常体系
- **ID**: task-3
- **type**: default
- **Description**: 创建 dto/、exception/、util/ 包，实现 ApiResponse<T>（统一响应封装）、PageResponse（分页响应）、ErrorResponse（错误响应），实现 ResponseUtil 工具类，实现 BusinessException（业务异常）、GlobalExceptionHandler（全局异常处理器，处理 MethodArgumentNotValidException、BusinessException、认证授权异常等）
- **File Scope**: src/main/java/com/learn/demo/dto/**, src/main/java/com/learn/demo/exception/**, src/main/java/com/learn/demo/util/**
- **Dependencies**: depends on task-1
- **Test Command**: `./mvnw -q test -Dtest=*Response*Test,*Exception*Test,ResponseUtilTest -Djacoco.skip=true`
- **Test Focus**:
  - ApiResponse 能够正确封装成功/失败响应，包含 code、message、data 字段
  - PageResponse 继承 ApiResponse，正确封装分页数据（items、total、page、size）
  - ErrorResponse 正确封装错误信息（code、message、timestamp、path）
  - ResponseUtil 提供便捷方法构造常见响应（success、error、page）
  - BusinessException 能够携带错误码和消息
  - GlobalExceptionHandler 能够捕获并格式化各类异常：
    - MethodArgumentNotValidException → 400 + 字段验证错误详情
    - BusinessException → 业务错误码 + 业务错误消息
    - 未捕获异常 → 500 + 通用错误消息

### Task 4: 单元测试与覆盖率
- **ID**: task-4
- **type**: default
- **Description**: 在 pom.xml 中添加 Jacoco 插件并配置 90% 覆盖率阈值（行覆盖、分支覆盖），为所有配置类编写单元测试（WebConfig、CorsConfig、JacksonConfig），为所有响应/异常类编写单元测试（ApiResponse、PageResponse、ErrorResponse、ResponseUtil、BusinessException、GlobalExceptionHandler）
- **File Scope**: pom.xml, src/test/java/com/learn/demo/**
- **Dependencies**: depends on task-2, depends on task-3
- **Test Command**: `./mvnw -q clean verify`
- **Test Focus**:
  - Jacoco 插件在 verify 阶段生成覆盖率报告
  - 覆盖率检查失败时构建失败（阈值 90%）
  - 配置类测试场景：
    - WebConfig: 验证分页解析器注册、默认/最大值配置
    - CorsConfig: 验证 CORS 规则注册（允许来源、方法、头部、凭证）
    - JacksonConfig: 验证 ObjectMapper Bean 的日期格式、未知属性处理
  - 响应/异常类测试场景：
    - ApiResponse: 成功响应、失败响应、带数据响应
    - PageResponse: 分页数据封装、空列表处理
    - ErrorResponse: 错误信息构造、时间戳格式
    - ResponseUtil: success()、error()、page() 方法
    - BusinessException: 构造函数、错误码/消息获取
    - GlobalExceptionHandler: 各类异常处理方法（使用 MockMvc 或直接调用）
  - 所有测试通过，代码覆盖率达到 90% 以上

## Acceptance Criteria
- [ ] 项目依赖完整（validation、Jacoco）
- [ ] application.yml 配置就绪（CORS、分页、Jackson）
- [ ] 配置类实现完成（WebConfig、CorsConfig、JacksonConfig）
- [ ] 统一响应体系实现完成（ApiResponse、PageResponse、ErrorResponse、ResponseUtil）
- [ ] 异常处理体系实现完成（BusinessException、GlobalExceptionHandler）
- [ ] 所有单元测试通过（`./mvnw test`）
- [ ] 代码覆盖率 ≥90%（`./mvnw verify` 通过 Jacoco 检查）
- [ ] 项目能够成功启动（Spring Context 加载无错误）

## Technical Notes
- **Spring Boot 版本**: 4.0.1（基于 JDK 17），使用 Spring Boot 4.x 的 API（如 `WebMvcConfigurer`）
- **测试框架**: JUnit 5 + Spring Boot Test + MockMvc
- **覆盖率工具**: Jacoco Maven Plugin（配置 `check` goal 在 verify 阶段执行）
- **CORS 策略**: 开发阶段允许所有来源（生产环境需调整为白名单）
- **分页参数**: 前端通过 `page`、`size` 参数传递，后端解析为 `Pageable`
- **日期格式**: 统一使用 ISO8601（`yyyy-MM-dd'T'HH:mm:ss.SSSZ`）
- **错误码设计**: 预留业务错误码范围（建议 1000-9999），系统错误使用标准 HTTP 状态码
- **依赖注意事项**:
  - 使用 `spring-boot-starter-validation` 而非旧版 `hibernate-validator`
  - Jacoco 排除 Lombok 生成代码（配置 `<exclude>**/*Lombok*</exclude>`）
- **测试隔离**: 配置类测试使用 `@SpringBootTest` 或 `@ContextConfiguration`，异常处理器测试使用 `@WebMvcTest` + MockMvc
