# Maijishop CRMEB Pro 3.2 Java实现

本项目是CRMEB Pro 3.2的Java版本重构实现，基于Spring Boot框架。

## 项目结构

```
maijishop
├── src/main/java
│   └── com/maijishop/crmeb
│       ├── common            # 通用组件
│       │   ├── aspect        # 切面
│       │   ├── constant      # 常量
│       │   ├── enums         # 枚举
│       │   ├── exception     # 异常
│       │   └── utils         # 工具类
│       ├── config            # 配置类
│       ├── controller        # 控制层
│       ├── dto               # 数据传输对象
│       ├── entity            # 实体类
│       ├── mapper            # MyBatis映射接口
│       ├── service           # 服务接口
│       │   └── impl          # 服务实现
│       └── vo                # 视图对象
├── src/main/resources
│   ├── mapper               # MyBatis XML映射文件
│   ├── static               # 静态资源
│   ├── templates            # 模板文件
│   └── application.yml      # 应用配置文件
└── src/test/java            # 测试代码
```

## 技术栈

- Spring Boot 2.7.5
- MyBatis-Plus 3.5.2
- MySQL 8.0+
- Redis
- JWT用于身份验证
- Druid数据库连接池
- Lombok简化代码
- Hutool和Commons-Lang3工具库

## 开发环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

## 运行项目

1. 克隆项目到本地

```bash
git clone https://github.com/nieqianqian051/crmeb3.2.git
cd crmeb3.2/maijishop
```

2. 修改数据库配置

编辑`src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/maijishop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: yourUsername
    password: yourPassword
```

3. 安装依赖并运行

```bash
mvn clean install
mvn spring-boot:run
```

4. 访问测试接口

在浏览器中访问：`http://localhost:8080/api/test/hello`

## 开发规范

- 遵循REST API设计规范
- 统一响应格式
- 使用全局异常处理
- 接口文档使用Swagger/Knife4j生成
- 代码风格遵循阿里巴巴Java开发手册

## 状态管理与长时间运行进程

对于长时间运行的任务，本项目采用：

1. Spring的@Scheduled注解进行任务调度
2. Redis实现分布式锁
3. 持久化存储任务状态
4. 使用状态机模式管理复杂业务状态转换

## 多线程与并发控制

- 使用Spring事务管理
- 使用ThreadPoolTaskExecutor进行线程池管理
- 乐观锁机制避免数据竞争
- 分布式锁处理跨服务并发 