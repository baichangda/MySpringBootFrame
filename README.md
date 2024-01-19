# 自定义Spring框架
## 框架
- jdk21(https://adoptium.net/zh-CN/temurin/releases/)
- springBoot3+
- gradle8+
## 第三方技术要求
### log(log4j2)
- 集成log4j2、排除spring默认logback依赖

### mysql/plsql(spring jdbc)
- 可以配置Condition使用
- 主要方法在com.bcd.base.support_jdbc.service.BaseService里面
- 可以使用代码生成器com.bcd.base.support_jdbc.service.CodeGenerator

### mongo(spring mongo)
- 可以配置Condition使用
- 主要方法在com.bcd.base.support_mongodb.service.BaseService里面
- 可以使用代码生成器com.bcd.base.support_mongodb.service.CodeGenerator

### json(jackson)
- ObjectMapper使用全局静态变量JsonUtil.GLOBAL_OBJECT_MAPPER

### redis(spring redis)
- 内部使用lettuce异步客户端
- 可以使用的RedisTemplate在RedisConfig中定义了、注入可以使用、也可以使用RedisUtil构造

### 权限(sa-token)
- https://sa-token.cc/

### http客户端(okhttp)
- 注入OkHttpClient使用、bean定义在OkHttpConfig中

### excel操作(easyExcel)
- 网上查阅资料

### 本地缓存(caffeine)
- 网上查阅资料

### web api文档(knife4j)
- https://doc.xiaominfo.com/docs/quick-start

## 代码目录要求
### com.bcd.base
- 第三方库的配置
- 工具包
- 引入新的三方库采用support_xxx格式命名包
### 业务模块
- com.bcd.xxx命令新的业务模块包
