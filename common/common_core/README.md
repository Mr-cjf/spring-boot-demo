# `common_core` 模块项目文档说明

---

## 📌 一、模块简介

`common_core` 是一个 **基础通用功能集**，为上层业务模块提供 **配置支持、工具类封装、常量管理、异常处理、验证器集成、类型转换和序列化定制等功能
**。该模块是 Spring Boot 项目中多个服务共享的基础设施组件。

---

## 🧱 二、核心功能模块

### 1. Web 配置

- **CORS 跨域支持**
- **默认返回 JSON 类型配置**
- **自定义 Jackson 序列化规则（如 Long 转 String、时间类型序列化）**
- **支持从字符串参数自动转换成 `Date`, `LocalDate`, `LocalTime`, `LocalDateTime`**

```java

@Bean
public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) { ...}

@Bean
public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() { ...}
```

---

### 2. Redis 缓存配置

- **RedisTemplate 的 key/value 序列化策略设置（使用 `GenericJackson2JsonRedisSerializer`）**
- **支持 Spring Cache 的缓存配置（TTL、key前缀、null值缓存控制等）**

```java

@Bean
public RedisTemplate<Object, Object> redisTemplate(...) { ...}

@Bean
public RedisCacheManager cacheManager(...) { ...}
```

---

### 3. Redisson 分布式锁配置

- **支持单机、哨兵、集群模式的 Redisson 客户端构建**
- **提供 `RedissonClient`, `RedissonReactiveClient`, `RedissonRxClient`**

```java

@Bean
public RedissonClient redisson() { ...}
```

---

### 4. Dubbo 全局异常处理

- **Dubbo服务提供者全局异常拦截器**
- **统一转换业务异常为预定义异常类型**
- **支持异常类型映射和自定义处理**

---

## ⚙️ 三、工具类与常量管理

### 1. 常用工具类

| 工具类                    | 功能说明                            |
|------------------------|---------------------------------|
| `Asserts`              | 参数断言工具，抛出统一 `BusinessException` |
| `Nones`                | 空值判断工具（空对象、空字符串、空集合）            |
| `IdCards`              | 身份证号格式校验                        |
| `AppRegexPatternConst` | 常用正则表达式集合（手机号、身份证等）             |

---

### 2. 系统常量枚举

| 常量类/枚举类            | 功能说明                    |
|--------------------|-------------------------|
| `AppSystemConst`   | 系统级别常量（日期格式、状态标识、UID 等） |
| `ErrorCodeEnum`    | 业务错误码枚举集合               |
| `SeparatorEnum`    | 各种分隔符定义                 |
| `RegexPatternEnum` | 常用正则表达式集合（手机号、身份证等）     |
| `AppHeaderConst`   | HTTP请求头常量定义             |
| `AppServerConst`   | 服务类型常量定义                |
| `ClientAgentEnum`  | 客户端代理类型枚举               |

---

## 🛡️ 四、异常处理体系

### 1. 统一异常接口

```java
public interface ErrorCode {
    String getCode();

    String getMsg();
}
```

### 2. 业务异常类

```java
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private Object data;
    private String description;
}
```

用于在 Controller 层或 Service 层统一抛出结构化异常信息。

---

## 🔐 五、请求参数校验

### 1. 自定义注解验证器

- 支持手机号、身份证号格式验证
- 使用 JSR 380 规范（Jakarta Validation）

#### 示例：

```java

@Phone(message = "手机号不正确")
private String phone;

@IdCard(message = "身份证号码不正确")
private String idCard;
```

### 2. 验证器实现

- [AbstractRegexValidator](../common_core/src/main/java/top/cjf_rb/core/web/validation/constraintvalidators/AbstractRegexValidator.java)
  抽象类
- [PhoneValidator](../common_core/src/main/java/top/cjf_rb/core/web/validation/constraintvalidators/PhoneValidator.java)
  手机号验证
- [IdCardValidator](../common_core/src/main/java/top/cjf_rb/core/web/validation/constraintvalidators/IdCardValidator.java)
  身份证号验证

---

## 🧩 六、国际化消息支持

- 消息文件路径：
    - [ValidationMessages.properties](../common_core/src/main/resources/ValidationMessages.properties)
    - [ValidationMessages_zh_CN.properties](../common_core/src/main/resources/ValidationMessages_zh_CN.properties)
- 支持国际化提示信息（如英文/中文提示）

---

## 📦 七、Spring Boot 配置整合

- 包括 Tomcat 连接池配置
- 文件上传大小限制
- Redis 默认连接配置
- Jackson 序列化策略（非空属性输出、日期格式等）

```yaml
spring:
  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: true
```

---

## ✨ 八、特色设计

### 1. 泛型增强的常量访问

```java
<T> T getValueAs(Class<T> clazz);
```

### 2. 统一断言风格

```java
Asserts.notNull(user, ErrorCodeEnum.USER_NOT_EXIST);
```

### 3. 可扩展的验证组

```java
public interface Create {
}

public interface Modify {
}
```

---

## 📌 九、总结：该模块作用

| 类别        | 功能描述                         |
|-----------|------------------------------|
| **基础设施**  | Web、Redis、Redisson 配置，支持快速接入 |
| **工具方法**  | 断言、空值判断、正则匹配、身份校验            |
| **异常处理**  | 统一异常结构、错误码机制                 |
| **参数验证**  | 自定义注解 + Validator 实现强类型校验    |
| **国际化支持** | 错误提示多语言适配                    |
| **类型转换**  | HTTP 请求中字符串转时间类型             |
| **序列化定制** | Jackson 自定义序列化/反序列化规则        |
| **可复用性**  | 可作为多个微服务的基础依赖模块              |

---

## 🎯 十、适用场景

适用于所有需要以下能力的 Spring Boot 项目：

- 统一异常处理
- 强类型参数校验
- Redis 缓存操作
- 时间类型自动转换
- 自定义 Jackson 序列化
- 多模块间共享基础组件

---

## 📦 十一、推荐使用方式

将该模块打包为 jar，在其他模块中引入即可直接使用：

```

<dependency>
    <groupId>top.cjf_rb</groupId>
    <artifactId>common_core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

如需进一步拓展，可新增 `Converter`、`ConstraintValidator` 或添加新的 `Jackson Serializer`。

---

## 📁 十二、目录结构参考

```
src/
├── main/
│   ├── java/
│   │   └── top.cjf_rb.core/
│   │       ├── config/               # Spring 配置类
│   │       ├── constant/             # 常量类
│   │       ├── context/              # 上下文数据模型
│   │       ├── exception/            # 异常处理
│   │       ├── util/                 # 工具类
│   │       └── web.validation/       # 请求参数校验相关
│   │           ├── constraints/      # 校验注解
│   │           ├── constraintvalidators/ # 校验器实现
│   │           └── groups/           # 校验分组
│   └── resources/
│       ├── application-core.yml        # Spring Boot 配置
│       ├── ValidationMessages.properties
│       └── ValidationMessages_zh_CN.properties
└── test/
```

---

## 📚 十三、附录

### 1. Maven 依赖说明

```

<dependencies>
    <!-- Spring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-data-32</artifactId>
    </dependency>
</dependencies>
```

---

### 2. 示例配置文件 (`application-core.yml`)

```yaml
server:
  tomcat:
    max-connections: 8192
    accept-count: 100
    threads:
      max: 200
      min-spare: 10

spring:
  servlet:
    multipart:
      max-file-size: 4MB
      max-request-size: 20MB
  web:
    resources:
      add-mappings: false
  jackson:
    default-property-inclusion: non_null
    property-naming-strategy: LOWER_CAMEL_CASE
    serialization:
      fail-on-empty-beans: false
      write-date-keys-as-timestamps: true
      write-date-timestamps-as-nanoseconds: false
      write-dates-as-timestamps: true
    deserialization:
      fail-on-unknown-properties: false
      fail-on-numbers-for-enums: true
      read-date-timestamps-as-nanoseconds: false
  cache:
    redis:
      cache-null-values: true
      key-prefix: "${spring.application.name}:"
      time-to-live: 3d
  data:
    redis:
      port: 6379
      host: localhost
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 4
          min-idle: 1
          max-wait: 2000ms