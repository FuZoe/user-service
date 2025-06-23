# 用户服务部署指南

## 前置条件

### 1. 环境要求
- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Nacos**: 2.0+
- **RocketMQ**: 4.9+
- **Seata**: 1.6+

### 2. 端口占用
- **用户服务**: 8082
- **Nacos**: 8848
- **RocketMQ NameServer**: 9876
- **RocketMQ Broker**: 10909, 10911
- **Seata**: 8091
- **MySQL**: 3306

## 部署步骤

### 第一步：安装依赖服务

#### 1.1 安装并启动 MySQL
```bash
# 下载并安装 MySQL 8.0
# 启动 MySQL 服务
systemctl start mysqld

# 创建数据库
mysql -u root -p
CREATE DATABASE user_db_0;
CREATE DATABASE user_db_1;
exit;

# 执行初始化脚本
mysql -u root -p < src/main/resources/db/init.sql
```

#### 1.2 安装并启动 Nacos
```bash
# 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz
tar -xzf nacos-server-2.2.3.tar.gz
cd nacos/bin

# 启动 Nacos（单机模式）
./startup.sh -m standalone
```

#### 1.3 安装并启动 RocketMQ
```bash
# 下载 RocketMQ
wget https://archive.apache.org/dist/rocketmq/4.9.7/rocketmq-all-4.9.7-bin-release.zip
unzip rocketmq-all-4.9.7-bin-release.zip
cd rocketmq-4.9.7

# 启动 NameServer
nohup sh bin/mqnamesrv &

# 启动 Broker
nohup sh bin/mqbroker -n localhost:9876 &
```

#### 1.4 安装并启动 Seata
```bash
# 下载 Seata
wget https://github.com/seata/seata/releases/download/v1.6.1/seata-server-1.6.1.zip
unzip seata-server-1.6.1.zip
cd seata

# 配置 Seata（修改 conf/registry.conf）
# 启动 Seata
sh bin/seata-server.sh
```

### 第二步：配置用户服务

#### 2.1 修改配置文件
```bash
# 复制开发环境配置
cp application-dev.properties application.properties

# 根据实际情况修改配置
vim application.properties
```

主要修改项：
- 数据库连接信息（用户名、密码）
- Nacos 服务地址
- RocketMQ 服务地址
- Seata 服务地址

#### 2.2 编译项目
```bash
# 清理并编译
mvn clean compile

# 运行测试
mvn clean test

# 打包项目
mvn clean package
```

### 第三步：启动用户服务

#### 3.1 使用 Maven 启动
```bash
mvn spring-boot:run
```

#### 3.2 使用 JAR 包启动
```bash
# 打包
mvn clean package -DskipTests

# 启动
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

#### 3.3 使用 Docker 启动
```bash
# 构建镜像
docker build -t user-service .

# 运行容器
docker run -d -p 8082:8082 --name user-service user-service
```

### 第四步：验证部署

#### 4.1 检查服务状态
```bash
# 健康检查
curl http://localhost:8082/actuator/health

# 查看日志
tail -f logs/user-service.log
```

#### 4.2 测试 API 接口
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

## 生产环境部署

### 1. 安全配置
```properties
# 修改 JWT 密钥
jwt.secret=your-production-secret-key-here

# 关闭 SQL 显示
spring.shardingsphere.props.sql-show=false

# 调整日志级别
logging.level.com.example.userservice=info
```

### 2. 性能优化
```properties
# 数据库连接池配置
spring.shardingsphere.datasource.ds0.hikari.maximum-pool-size=20
spring.shardingsphere.datasource.ds0.hikari.minimum-idle=5
spring.shardingsphere.datasource.ds1.hikari.maximum-pool-size=20
spring.shardingsphere.datasource.ds1.hikari.minimum-idle=5

# JVM 参数
-Xms2g -Xmx4g -XX:+UseG1GC
```

### 3. 监控配置
```properties
# 启用监控端点
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

## 故障排查

### 1. 常见问题

#### 1.1 数据库连接失败
```bash
# 检查数据库服务状态
systemctl status mysqld

# 检查数据库连接
mysql -u root -p -h localhost -P 3306

# 检查防火墙
firewall-cmd --list-ports
```

#### 1.2 Nacos 连接失败
```bash
# 检查 Nacos 服务状态
curl http://localhost:8848/nacos/v1/ns/operator/metrics

# 检查网络连接
telnet localhost 8848
```

#### 1.3 RocketMQ 连接失败
```bash
# 检查 RocketMQ 服务状态
sh bin/mqadmin clusterList -n localhost:9876

# 检查端口占用
netstat -tlnp | grep 9876
```

#### 1.4 Seata 连接失败
```bash
# 检查 Seata 服务状态
curl http://localhost:8091/v1/registry/status

# 检查日志
tail -f logs/seata-server.log
```

### 2. 日志分析
```bash
# 查看应用日志
tail -f logs/user-service.log

# 查看错误日志
grep ERROR logs/user-service.log

# 查看慢查询日志
grep "slow query" logs/user-service.log
```

### 3. 性能监控
```bash
# 查看 JVM 状态
jstat -gc <pid>

# 查看线程状态
jstack <pid>

# 查看内存使用
jmap -heap <pid>
```

## 备份与恢复

### 1. 数据库备份
```bash
# 备份数据库
mysqldump -u root -p user_db_0 > user_db_0_backup.sql
mysqldump -u root -p user_db_1 > user_db_1_backup.sql

# 恢复数据库
mysql -u root -p user_db_0 < user_db_0_backup.sql
mysql -u root -p user_db_1 < user_db_1_backup.sql
```

### 2. 配置文件备份
```bash
# 备份配置文件
cp application.properties application.properties.backup

# 恢复配置文件
cp application.properties.backup application.properties
```

## 扩展部署

### 1. 集群部署
```bash
# 启动多个实例
java -jar user-service.jar --server.port=8082
java -jar user-service.jar --server.port=8083
java -jar user-service.jar --server.port=8084
```

### 2. 负载均衡
```bash
# 使用 Nginx 配置负载均衡
upstream user-service {
    server localhost:8082;
    server localhost:8083;
    server localhost:8084;
}
```

### 3. 高可用部署
```bash
# 配置数据库主从复制
# 配置 Nacos 集群
# 配置 RocketMQ 集群
# 配置 Seata 集群
```

## 维护操作

### 1. 服务重启
```bash
# 优雅重启
kill -15 <pid>
java -jar user-service.jar
```

### 2. 配置热更新
```bash
# 通过 Nacos 配置中心更新配置
# 应用会自动刷新配置
```

### 3. 版本升级
```bash
# 备份当前版本
cp user-service.jar user-service.jar.backup

# 部署新版本
cp user-service-new.jar user-service.jar

# 重启服务
kill -15 <pid>
java -jar user-service.jar
``` 