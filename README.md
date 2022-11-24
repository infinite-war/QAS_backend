# QAS_backend
问答网站(后端部分)


## 使用
1. 使用`resources/database/schema.sql`文件执行建表操作
2. 使用`resources/database/ins.sql`文件恢复记录
2. 修改`application-x.yml`中的配置内容。测试项目时使用`application-dev.yml`配置文件，项目打算发行时改用`application-prod.yml`文件，选择哪个配置文件可以再`application.yml`中设置
3. 启动Redis
4. 本地启动后端项目后，通过浏览器输入`localhost:你配置的端口/swagger-ui.html`可以进入接口测试页面



1. 项目介绍
    - 前后端分离的问答网站（后端部分）
2. 所用技术
    - SpringBoot
    - SpringMVC
    - MybatisPlus（对应MySQL）
    - RedisTemplate（对应Redis）
    - JWT
    - Maven
4. 如何运行
    - 在MySQL中新建forum数据库，执行schema.sql脚本
    - 更改application-prod.yml中的配置
    - 运行Application类中main方法
4. 接口文档
    - swagger-ui自动配置