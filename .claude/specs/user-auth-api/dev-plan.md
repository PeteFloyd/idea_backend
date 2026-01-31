# User Authentication Module API - Development Plan

## Overview
实现基于 JWT 的用户认证系统，包含注册、登录、登出功能和安全配置，支持 Token 黑名单管理和会话过期控制。

## Task Breakdown

### Task 1: Dependencies and Configuration
- **ID**: task-1
- **type**: quick-fix
- **Description**: 添加 Spring Security 和 JWT 依赖到 pom.xml，配置 JWT 参数到 application.yml（secret 从环境变量读取，过期时间 24 小时）
- **File Scope**:
  - idea_back/pom.xml
  - idea_back/src/main/resources/application.yml
- **Dependencies**: None
- **Test Command**: `./mvnw clean compile -f idea_back/pom.xml`
- **Test Focus**:
  - 依赖正确解析无冲突
  - 配置文件格式正确
  - 编译通过

### Task 2: DTOs and Request/Response Models
- **ID**: task-2
- **type**: default
- **Description**: 创建认证相关 DTO 类：RegisterRequest（含 @Valid、@Pattern、@Size、@Email 注解）、LoginRequest、LoginResponse（包含嵌套 UserInfo 和 Token 元数据）
- **File Scope**:
  - idea_back/src/main/java/com/learn/demo/dto/auth/**
  - idea_back/src/test/java/com/learn/demo/dto/auth/**
- **Dependencies**: task-1
- **Test Command**: `./mvnw test -Dtest=*Request*Test,*Response*Test -f idea_back/pom.xml --fail-at-end`
- **Test Focus**:
  - RegisterRequest 校验规则正确（用户名 3-20 字符字母数字，密码 6-32 字符，邮箱格式）
  - 非法输入触发验证异常
  - DTO 序列化/反序列化正确

### Task 3: Security Infrastructure
- **ID**: task-3
- **type**: default
- **Description**: 实现 JWT 核心组件和安全配置：JwtConfig（读取 yml）、JwtTokenProvider（HS256 生成/验证/解析）、UserPrincipal（实现 UserDetails，根据 UserStatus 判断 isEnabled）、TokenBlacklistService（ConcurrentHashMap 内存黑名单 + 过期清理）、JwtAuthenticationFilter（继承 OncePerRequestFilter，检查黑名单）、SecurityConfig（过滤器链，白名单 /api/auth/register 和 /api/auth/login，BCryptPasswordEncoder Bean）
- **File Scope**:
  - idea_back/src/main/java/com/learn/demo/config/**
  - idea_back/src/main/java/com/learn/demo/security/**
  - idea_back/src/test/java/com/learn/demo/security/**
- **Dependencies**: task-1
- **Test Command**: `./mvnw test -Dtest=Jwt*Test,Security*Test,TokenBlacklist*Test -f idea_back/pom.xml --fail-at-end -Dspring.profiles.active=test`
- **Test Focus**:
  - JwtTokenProvider 生成有效 Token
  - Token 验证成功/失败/过期场景
  - 黑名单添加/查询/自动清理过期 Token
  - UserPrincipal 根据 UserStatus 正确返回 isEnabled
  - SecurityConfig 白名单路径不需认证，其他路径需认证

### Task 4: Auth Service and Controller
- **ID**: task-4
- **type**: default
- **Description**: 实现认证业务逻辑和 API 端点：AuthService（register 检查重复用户名，BCrypt 加密密码；login 通过 AuthenticationManager 认证，返回 JWT；logout 添加 Token 到黑名单）、AuthController（POST /api/auth/register 返回 201，POST /api/auth/login 返回 200 + JWT，POST /api/auth/logout 需 Authorization 头返回 200）、扩展 GlobalExceptionHandler 映射异常（BadCredentialsException→401，DisabledException→403，重复用户名→400）
- **File Scope**:
  - idea_back/src/main/java/com/learn/demo/service/AuthService.java
  - idea_back/src/main/java/com/learn/demo/controller/AuthController.java
  - idea_back/src/main/java/com/learn/demo/exception/GlobalExceptionHandler.java
  - idea_back/src/test/java/com/learn/demo/service/AuthServiceTest.java
  - idea_back/src/test/java/com/learn/demo/controller/AuthControllerTest.java
- **Dependencies**: task-2, task-3
- **Test Command**: `./mvnw test -Dtest=Auth*Test -f idea_back/pom.xml --fail-at-end -Dspring.profiles.active=test`
- **Test Focus**:
  - 注册成功返回 201，重复用户名返回 400
  - 登录成功返回 JWT（24h 过期），错误凭证返回 401，禁用用户返回 403
  - 登出成功将 Token 加入黑名单，后续请求使用该 Token 被拒绝
  - MockMvc 集成测试覆盖所有端点状态码和响应格式
  - 异常处理器正确映射所有认证异常

## Acceptance Criteria
- [ ] 注册接口校验用户名（3-20 字符字母数字）、密码（6-32 字符 BCrypt 加密）、邮箱格式，重复用户名返回 400
- [ ] 登录接口正确凭证返回 JWT（24h 过期），错误凭证返回 401，禁用用户返回 403
- [ ] 登出接口成功将 Token 加入内存黑名单，黑名单自动清理过期 Token
- [ ] SecurityConfig 正确配置白名单（/api/auth/register、/api/auth/login），其他端点需认证
- [ ] JWT 使用 HS256 算法，secret 从环境变量 ${JWT_SECRET} 读取
- [ ] 所有单元测试通过，代码覆盖率 ≥90%
- [ ] GlobalExceptionHandler 正确映射 BadCredentialsException→401、DisabledException→403、重复用户名→400

## Technical Notes
- **安全模式**: 使用 AuthenticationManager + UserDetailsService 标准模式
- **密码加密**: BCryptPasswordEncoder（Spring Security 推荐，自动加盐）
- **JWT 算法**: HS256（对称加密，适合单服务场景；未来扩展多服务可切换 RS256）
- **黑名单策略**: 内存 ConcurrentHashMap 存储（简单高效，重启丢失；生产环境建议迁移 Redis）
- **过期清理**: TokenBlacklistService 定时任务或惰性清理过期 Token，避免内存泄漏
- **环境变量**: JWT_SECRET 必须通过环境变量传入，禁止硬编码（建议生产环境使用 256 位随机字符串）
- **用户状态**: UserPrincipal.isEnabled() 基于 User.userStatus 判断，确保禁用用户无法登录
- **测试隔离**: 使用 @SpringBootTest + @Transactional 确保测试数据回滚，避免数据库污染
- **错误映射约束**:
  - 401 Unauthorized: 凭证错误（BadCredentialsException）
  - 403 Forbidden: 账号禁用（DisabledException）
  - 400 Bad Request: 输入校验失败（重复用户名、格式错误）
