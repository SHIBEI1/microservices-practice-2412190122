# 第 03 周作业记录

## 工程创建与运行结果

第三周创建宠物医院管理系统的 Spring Boot 工程，完成应用启动、基础 YAML 配置、运行状态验证接口、Actuator 健康检查和启动测试。第三周未实现宠物、预约、病历或账单等业务模块，也未接入数据库。

第四周已将工程由 `monolith/pet-hospital/` 调整至 `monolith/`，使 `monolith/pom.xml` 与 `monolith/src/` 直接属于同一个 Maven 工程。本文命令同步为当前目录，第三周提交截图中的旧目录保留为历史记录；第四周新增业务另见 `docs/homework/week-04/index.md`。

应用名称为 `pet-hospital`，服务端口为 `8080`。`GET /api/hello` 用于验证服务可访问，`GET /actuator/health` 用于验证应用健康状态。

## 启动命令

```bash
cd monolith
./mvnw spring-boot:run
```

## 接口验证

```bash
curl "http://localhost:8080/api/hello?name=KongLei"
curl http://localhost:8080/actuator/health
```

### `GET /api/hello?name=KongLei`

```json
{"message":"Welcome to Pet Hospital, KongLei!"}
```

### `GET /actuator/health`

```json
{"groups":["liveness","readiness"],"status":"UP"}
```



## 本周完成汇总

- 创建了独立的 Spring Boot 工程，当前统一位于 `monolith/`。
- 配置了应用名称 `pet-hospital`、服务端口 `8080` 和 Actuator 健康检查端点。
- 实现了用于验证服务运行状态的 `GET /api/hello` 接口。
- 保留并通过了 Spring Boot 启动测试。

## 启动测试结果

保留 `PetHospitalApplicationTests` 中使用 `@SpringBootTest` 的 `contextLoads` 测试，用于确认 Spring 应用上下文能够加载。

```bash
cd monolith
./mvnw test
```
