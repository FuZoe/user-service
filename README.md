# 用户权限管理系统 - 用户服务

## 项目概述

这是一个基于微服务架构的用户权限管理系统中的用户服务模块。系统采用Spring Boot + Spring Cloud技术栈，实现了用户注册、登录、权限管理等功能。

## 系统架构

```
+-------------------+     +---------------------+     +----------------------+
|   User Service    |<--->| Permission Service  |<--->|  Logging Service     |
|  (HTTP API + MQ)  |     |   (RPC服务端)       |     |   (MQ消费者)         |
+-------------------+     +---------------------+     +----------------------+
```

## 技术栈

- **Spring Boot 3.2.0**: 基础框架
- **Spring Cloud 2023.0.0**: 微服务框架
- **Spring Cloud Alibaba 2023.0.1.0**: 阿里云微服务组件
- **Nacos**: 服务注册与发现、配置中心
- **ShardingSphere 5.4.1**: 分库分表
- **RocketMQ 2.2.3**: 消息队列
- **Seata**: 分布式事务
- **MySQL**: 数据库
- **JWT**: 身份认证
- **OpenFeign**: RPC调用

## 核心功能

### 1. 用户管理
- 用户注册（分布式事务）
- 用户登录（JWT认证）
- 用户信息查询
- 用户信息更新
- 密码重置

### 2. 权限控制
- 角色分级：普通用户、管理员、超级管理员
- 基于角色的数据访问控制
- 权限验证

### 3. 分库分表
- 用户表水平分片（2库2表）
- 基于用户ID的哈希分片策略
- 雪花算法生成分布式主键

### 4. 异步日志
- 操作日志异步记录
- RocketMQ消息队列
- 关键操作日志持久化

## 数据库设计

### 分库分表策略
- **数据库**: user_db_0, user_db_1
- **表**: users_0, users_1
- **分片键**: user_id
- **分片算法**: HASH_MOD

### 用户表结构
```sql
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY,           -- 用户ID（雪花算法）
    username VARCHAR(50) NOT NULL UNIQUE, -- 用户名
    password VARCHAR(255) NOT NULL,       -- 密码（BCrypt加密）
    email VARCHAR(100),                   -- 邮箱
    phone VARCHAR(20),                    -- 手机号
    gmt_create TIMESTAMP NOT NULL,        -- 创建时间
    gmt_modified TIMESTAMP NOT NULL,      -- 修改时间
    is_deleted TINYINT NOT NULL DEFAULT 0 -- 软删除标记
);
```

## API接口

### 1. 用户注册
```
POST /user/register
Content-Type: application/json

{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "phone": "13800138000"
}
```

### 2. 用户登录
```
POST /user/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "password123"
}
```

### 3. 查询用户列表
```
GET /user/users?page=0&size=10
Authorization: Bearer <jwt_token>
```

### 4. 查询用户信息
```
GET /user/{userId}
Authorization: Bearer <jwt_token>
```

### 5. 更新用户信息
```
PUT /user/{userId}
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
    "email": "newemail@example.com",
    "phone": "13900139000"
}
```

### 6. 密码重置
```
POST /user/reset-password
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
    "oldPassword": "oldpassword",
    "newPassword": "newpassword"
}
```

## 部署要求

### 环境依赖
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Nacos 2.0+
- RocketMQ 4.9+
- Seata 1.6+

### 配置说明

#### 1. 数据库配置
```properties
# 分库分表配置
spring.shardingsphere.datasource.ds0.jdbc-url=jdbc:mysql://localhost:3306/user_db_0
spring.shardingsphere.datasource.ds1.jdbc-url=jdbc:mysql://localhost:3306/user_db_1
```

#### 2. Nacos配置
```properties
spring.cloud.nacos.discovery.server-addr=localhost:8848
spring.cloud.nacos.config.server-addr=localhost:8848
```

#### 3. RocketMQ配置
```properties
rocketmq.name-server=localhost:9876
rocketmq.producer.group=user-service-producer
```

#### 4. Seata配置
```properties
seata.enabled=true
seata.application-id=user-service
seata.tx-service-group=user-service-group
```

## 快速开始

### 1. 环境准备
```bash
# 创建数据库
CREATE DATABASE user_db_0;
CREATE DATABASE user_db_1;

# 执行初始化脚本
mysql -u root -p < src/main/resources/db/init.sql
```

### 2. 启动依赖服务
```bash
# 启动Nacos
nacos-server

# 启动RocketMQ
mqnamesrv
mqbroker -n localhost:9876

# 启动Seata
seata-server
```

### 3. 启动用户服务
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 4. 测试接口
```bash
# 注册用户
curl -X POST http://localhost:8082/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'

# 用户登录
curl -X POST http://localhost:8082/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

## 项目结构

```
src/main/java/com/example/userservice/
├── UserServiceApplication.java          # 启动类
├── client/                              # RPC客户端
│   ├── PermissionServiceClient.java     # 权限服务客户端
│   └── *.java                          # 响应对象
├── config/                              # 配置类
│   ├── JwtInterceptor.java             # JWT拦截器
│   └── WebConfig.java                  # Web配置
├── controller/                          # 控制器层
│   └── UserController.java             # 用户控制器
├── dto/                                 # 数据传输对象
│   ├── ApiResponse.java                # 统一响应格式
│   ├── UserRegisterRequest.java        # 注册请求
│   └── *.java                          # 其他DTO
├── entity/                              # 实体类
│   └── User.java                       # 用户实体
├── exception/                           # 异常处理
│   └── GlobalExceptionHandler.java     # 全局异常处理器
├── mq/                                  # 消息队列
│   ├── LogMessageProducer.java         # 日志消息生产者
│   └── OperationLogMessage.java        # 操作日志消息
├── repository/                          # 数据访问层
│   └── UserRepository.java             # 用户仓库
├── service/                             # 业务逻辑层
│   └── UserService.java                # 用户服务
└── util/                                # 工具类
    └── JwtUtil.java                    # JWT工具类
```

## 注意事项

1. **分库分表**: 确保数据库和表已正确创建
2. **服务依赖**: 需要先启动Nacos、RocketMQ、Seata等依赖服务
3. **权限服务**: 需要配合权限服务一起使用
4. **日志服务**: 需要配合日志服务消费MQ消息
5. **JWT密钥**: 生产环境请修改JWT密钥配置

## 开发说明

### 编译测试
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn clean test

# 打包项目
mvn clean package
```

### 代码规范
- 使用Lombok简化代码
- 统一异常处理
- 统一响应格式
- 完善的日志记录
- 参数验证

## 许可证

MIT License 