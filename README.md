# microservices-practice-2412190122

课程名称：微服务开发与实践

姓名：孔磊

学号：2412190122

仓库用途：用于微服务项目开发练习

最终项目：

# 宠物医院管理系统

## 一、项目背景

宠物医院在日常经营中需要管理宠物档案、主人信息、医生排班、就诊预约、病历记录和收费账单。传统的纸质登记或分散管理方式容易出现信息查询困难、预约时间冲突、病历保存不完整以及收费统计不便等问题。

本项目计划开发一套宠物医院管理系统，对预约、接诊、病历和收费等业务流程进行数字化管理，提高宠物医院的工作效率，同时为宠物主人提供更加便捷的就诊服务。

## 二、项目目标

本项目旨在建立统一的宠物及宠物主人档案，实现医生信息与排班管理、宠物就诊预约、预约状态管理、电子病历保存以及诊疗收费管理。系统还将支持医院对预约、就诊和收费情况进行查询与统计，并在后续课程中逐步演进为微服务架构。

## 三、目标用户

### 1. 宠物主人

宠物主人可以管理个人信息和名下宠物，提交并查看就诊预约，查询宠物的历史病历，以及查看诊疗账单和支付状态。

### 2. 宠物医生

宠物医生可以查看个人排班和预约信息，接诊宠物并记录症状，填写诊断结果与治疗方案，创建电子病历并登记诊疗项目。

### 3. 医院管理员

医院管理员可以维护医生和科室信息，管理医生排班，查看全部预约和病历，管理收费账单，并统计医院的就诊和收费情况。

## 四、核心功能

### 1. 宠物与主人档案管理

系统记录宠物主人的姓名、联系方式和家庭地址，并管理其名下宠物的名称、种类、年龄及健康备注。宠物主人可以查看和维护本人及宠物的基本信息。

### 2. 医生与排班管理

系统维护医生的姓名、所属科室、擅长病种、出诊时间和接诊状态，为宠物主人选择医生和预约时间提供依据。

### 3. 就诊预约管理

宠物主人可以选择宠物、医生和就诊时间提交预约。系统对医生的接诊状态和排班时间进行校验，并对待确认、已确认、已完成和已取消等预约状态进行管理。

### 4. 电子病历管理

宠物到院后，医生根据预约信息记录宠物的症状、诊断结果和治疗方案，生成电子病历。系统保存宠物的历史就诊信息，方便医生和宠物主人后续查询。

### 5. 收费账单管理

系统根据诊疗项目生成收费账单，计算收费金额并记录支付状态和支付时间。医院管理员可以按时间查询账单并统计收费情况。

## 五、核心业务流程

### 1. 就诊预约流程

宠物主人选择宠物、医生和就诊时间，系统校验医生排班后生成预约记录，并根据预约确认、到院就诊或取消预约等情况更新预约状态。

### 2. 接诊与病历流程

宠物到院后，医生查询对应的预约记录，核对宠物信息并完成接诊。医生填写症状、诊断结果和治疗方案后，系统生成电子病历。

### 3. 缴费与结算流程

医生完成诊疗后，系统根据诊疗项目生成收费账单。宠物主人完成缴费后，系统更新账单的支付状态，医院管理员可以查询账单并进行收费统计。

## 六、核心领域对象

### 1. PetOwner

`PetOwner`表示宠物主人，主要记录主人编号、姓名、联系电话、家庭地址及其名下的宠物。

### 2. Pet

`Pet`表示宠物，主要记录宠物编号、名称、动物种类、年龄、所属主人和健康备注。

### 3. Doctor

`Doctor`表示医生，主要记录医生编号、姓名、所属科室、擅长病种、出诊时间和接诊状态。

### 4. Appointment

`Appointment`表示就诊预约，主要记录预约编号、关联宠物、关联医生、预约时间、预约状态和预约备注。

### 5. MedicalRecord

`MedicalRecord`表示电子病历，主要记录病历编号、对应宠物、接诊医生、就诊时间、症状描述、诊断结果和治疗方案。

### 6. Bill

`Bill`表示收费账单，主要记录账单编号、关联病历、收费项目、总金额、支付状态和支付时间。

## 七、微服务划分

系统后续计划按照业务职责拆分为宠物与主人档案服务、医生与排班服务、预约服务、病历服务以及收费账单服务。

宠物与主人档案服务负责管理宠物及主人信息；医生与排班服务负责管理医生、科室和出诊安排；预约服务负责处理预约创建、时间校验和状态流转；病历服务负责管理宠物的电子病历；收费账单服务负责处理账单生成、支付状态和收费统计。

各服务拥有相对独立的业务边界，并通过接口或消息机制完成业务协作。

## 八、后续演进方向

### 第一阶段：完善领域模型

完善宠物、主人、医生、预约、病历和账单等领域对象，补充核心业务规则与异常处理。

### 第二阶段：实现基础业务系统

使用Spring Boot构建后端服务，设计RESTful API并接入数据库，实现主要业务数据的持久化管理。

### 第三阶段：完成微服务拆分

按照业务边界拆分各项服务，并逐步加入服务注册与发现、远程调用和API网关。

### 第四阶段：完善安全与可靠性

增加用户登录、身份认证和角色权限控制，并实现统一异常处理、系统日志、服务监控和链路追踪。

### 第五阶段：实现异步业务

使用消息队列实现预约提醒和缴费通知，并处理不同服务之间的数据一致性问题。

### 第六阶段：完成容器化部署

使用Docker完成各项服务及相关中间件的容器化部署，形成可以独立运行的宠物医院微服务系统。

## 九、项目定位

宠物医院管理系统具有明确的用户角色、完整的业务流程和清晰的服务边界，适合作为本学期持续演进的微服务课程项目。通过本项目可以逐步实践Java开发、Spring Boot、数据库、微服务通信、消息队列、权限控制、系统监控和容器化部署等课程内容。

## 十、工程运行说明

### 环境要求

- Java：JDK 25 或更高版本，项目编译目标为 Java 25；当前开发环境为 WSL Ubuntu 中的 JDK 26。
- Maven：Maven 3.9+。推荐使用项目附带的 Maven Wrapper（`./mvnw`）。

### 工程位置

Spring Boot 工程位于仓库的 `monolith/` 目录，`monolith/pom.xml` 与 `monolith/src/` 属于同一个 Maven 工程，应用名称为 `pet-hospital`。第四周已将原来 `monolith/pet-hospital/` 下的工程文件移至 `monolith/`，第三周截图中的旧路径属于历史记录。

仓库根目录原有的 `pom.xml` 和 `src/` 属于前期练习，本周应用应从 `monolith/` 启动。

### 启动和测试命令

Windows PowerShell 中先进入已安装 Java 的 WSL 环境：

```powershell
wsl -d Ubuntu
```

在 WSL 终端中启动应用：

```bash
cd /home/konglei/microservice/microservices-practice-2412190122/monolith
./mvnw spring-boot:run
```

在另一个 WSL 终端中执行测试：

```bash
cd /home/konglei/microservice/microservices-practice-2412190122/monolith
./mvnw test
```

### 基础接口

- 问候接口：`http://localhost:8080/api/hello?name=KongLei`
- 健康检查：`http://localhost:8080/actuator/health`

```bash
curl -i "http://localhost:8080/api/hello?name=KongLei"
curl -i http://localhost:8080/actuator/health
```

### 数据保存

第四周数据库持久化任务已接入 MySQL 8.4，使用 Spring Data JPA 读写 `pet_hospital` 数据库。首次启动时由 JPA 创建表，后续通过 `ddl-auto: update` 更新开发环境表结构，应用重启后仍可读取已保存的数据。前期 H2 文件数据库的运行截图保留为阶段记录。

可通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 配置 MySQL 连接，默认数据库为 `pet_hospital`，用户名为 `pet_hospital`。本机连接参数也可保存在不提交 Git 的 `monolith/application-local.properties` 中。测试使用独立的 H2 内存数据库，正常执行 `./mvnw test` 不要求启动 MySQL。具体安装和连接步骤见下方“第四周数据库持久化”。

## 十一、第四周实现范围

本周围绕“宠物就诊预约”实现完整的单体业务流程：建立宠物档案，登记医生及每周出诊时段，创建预约，查询预约并执行确认、完成或取消操作。核心业务实体为 `Pet` 和 `Appointment`，`Doctor` 提供预约所需的医生与排班信息。新增代码沿用基础包 `com.zjgsu.kl`，按模型、Repository、Service、DTO、Controller 和异常处理分层。

Service 校验预约时间必须在未来、符合医生出诊日且按半小时对齐，医生必须接诊；同一医生或同一宠物在同一时间不能重复预约。预约状态为 `PENDING`、`CONFIRMED`、`COMPLETED`、`CANCELLED`，确认和取消需在预约时间前执行，完成需在预约时间到达后执行。取消保留历史记录并释放预约时段。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST / GET | `/api/pets` | 创建宠物 / 查询宠物列表 |
| GET | `/api/pets/{id}` | 查询宠物详情 |
| POST / GET | `/api/doctors` | 登记医生 / 查询医生列表 |
| GET | `/api/doctors/{id}` | 查询医生详情 |
| POST / GET | `/api/appointments` | 创建预约 / 查询全部预约 |
| GET | `/api/appointments?petId={petId}` | 查询指定宠物的预约 |
| GET | `/api/appointments/{id}` | 查询预约详情 |
| POST | `/api/appointments/{id}/confirm` | 确认预约 |
| POST | `/api/appointments/{id}/complete` | 完成预约 |
| POST | `/api/appointments/{id}/cancel` | 取消预约 |

创建成功返回 `201 Created` 和 `Location`，查询及状态变更成功返回 `200 OK`。参数错误返回 `400`，资源不存在返回 `404`，预约冲突或不允许的状态操作返回 `409`，错误响应使用 `ProblemDetail`。

数据库设计见 [docs/database.md](docs/database.md)，操作步骤、测试与验证记录见 [docs/homework/week-04/index.md](docs/homework/week-04/index.md)。

### 单体实现阶段验证结果

2026 年 10 月 15 日执行 `./mvnw -B -ntp test`，26 项测试全部通过，失败、错误和跳过均为 0，结果为 `BUILD SUCCESS`。包含 1 项启动测试、21 项业务与接口测试、4 项数据库约束测试。测试通过后执行 `./mvnw -B -ntp package -DskipTests`，打包成功。

通过实际 HTTP 请求验证了宠物、医生与预约创建、重复预约拦截、确认、取消及重新预约；并发争抢同一医生时段时，仅一条请求成功。接入 MySQL 前使用隔离的 H2 文件数据库停止并重启应用后，原有宠物、医生及包含取消历史的预约记录均可重新读取。到达预约时间后的完成流程由可控制时钟的集成测试验证。后续新增的三层测试及当前运行数据库见下方数据库持久化与测试说明。

### 当前尚未实现的业务能力

目前尚未实现独立的主人账户、登录和角色权限、多段或临时排班、电子病历、收费支付、预约提醒、前端页面及微服务拆分。主人姓名与电话先保存在宠物档案中；每名医生暂只支持一个每周重复的出诊时段。后续按每周作业要求逐步推进。

## 十二、第四周单体应用实现

### 启动命令

在 WSL 或 Linux 终端中，从仓库根目录启动应用：

```bash
cd monolith
./mvnw spring-boot:run
```

应用默认监听 `8080` 端口，业务模型、Service、Controller 和 Repository 均由同一个 Spring Boot 应用加载。

### 业务接口 curl 示例

应用启动后，在另一个 WSL 或 Linux 终端执行以下命令。两个示例均不需要预先填写资源编号。

创建宠物档案，成功时返回 `201 Created`、`Location` 响应头和包含宠物编号的 JSON：

```bash
curl -i -X POST http://localhost:8080/api/pets \
  -H 'Content-Type: application/json' \
  -d '{"name":"团团","species":"猫","age":2,"ownerName":"孔磊","ownerPhone":"13800000000","healthNotes":"已接种疫苗"}'
```

查询宠物列表，成功时返回 `200 OK` 和宠物数组，其中包含已创建的档案：

```bash
curl -i http://localhost:8080/api/pets
```

预约接口同样使用 `/api/` 前缀，支持创建、查询、确认、完成和取消操作。Controller 处理 HTTP 输入输出，预约时间、医生接诊状态、时段冲突和状态流转规则由 `AppointmentService` 检查，数据库读写通过 Repository 完成。

单体应用实现思路、验证命令和运行结果见 [第四周作业记录](docs/homework/week-04/index.md#单体应用实现)。

## 十三、第四周数据库持久化

### 本地 MySQL 安装与启动

当前开发环境为 WSL Ubuntu，使用 MySQL 8.4。本地首次安装时执行：

```bash
sudo apt update
sudo apt install -y mysql-server
sudo service mysql start
mysql --version
```

### 建库与授权

在 Ubuntu 中通过 `sudo mysql` 进入管理终端，执行以下 SQL。`ReplaceWithYourPassword` 为本机设置的数据库密码：

```sql
CREATE DATABASE IF NOT EXISTS pet_hospital
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'pet_hospital'@'localhost'
  IDENTIFIED BY 'ReplaceWithYourPassword';
GRANT ALL PRIVILEGES ON pet_hospital.* TO 'pet_hospital'@'localhost';
EXIT;
```

以上为首次准备数据库的操作，已有账号使用现有密码连接。运行账号权限限定在 `pet_hospital`，业务表由 JPA 自动创建。

### 连接配置

`monolith/src/main/resources/application.yml` 使用 MySQL Connector/J，主要配置如下：

| 配置项 | 默认值或作用 |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/pet_hospital?connectionTimeZone=Asia/Shanghai&characterEncoding=UTF-8&sslMode=DISABLED&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | `pet_hospital` |
| `DB_PASSWORD` | 本机数据库密码 |
| 驱动 | `com.mysql.cj.jdbc.Driver` |
| JPA 建表策略 | `update`，用于本周开发环境 |

MySQL 连接参数用于本机开发。密码通过环境变量或根目录下的本机配置文件 `monolith/application-local.properties` 提供，文件内容使用 `DB_PASSWORD=本机数据库密码` 的 properties 格式。该文件已加入 Git 忽略规则，不作为项目共享配置提交。

已配置本机连接文件时，从仓库根目录启动：

```bash
cd monolith
./mvnw spring-boot:run
```

其他环境可在 `monolith/` 下通过环境变量提供密码后启动：

```bash
export DB_USERNAME=pet_hospital
read -rsp 'MySQL password: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
./mvnw spring-boot:run
```

应用正常启动后，`curl -i http://localhost:8080/actuator/health` 应返回 `200` 和 `UP`。业务接口调用方式沿用上方 curl 示例。

### 数据结构与测试数据

MySQL 保存 `pets`、`doctors`、`appointments` 三张业务表，使用自增主键、外键、医生工号唯一约束、预约联合唯一约束和检查约束。详细字段、实体关系及约束见 [数据库设计](docs/database.md)。

测试数据通过 `monolith/src/test/java/com/zjgsu/kl/support/TestData.java` 和各测试类自动准备，普通测试使用 H2 内存数据库建表并在事务结束后回滚，无需预先运行 `data.sql`。

## 十四、第四周测试与验证

从仓库根目录执行完整测试：

```bash
cd monolith
./mvnw test
```

| 测试类 | 验证内容 | 数量 |
| --- | --- | --- |
| `RepositoryPersistenceTests` | Repository 写入、重载、删除、关联查询和取消后重新预约 | 3 |
| `AppointmentServiceTests` | Service 正常创建、医生与宠物冲突、排班、状态和取消规则 | 6 |
| `PetHospitalApiTests` | API 正常及错误响应、参数校验和业务流程 | 21 |
| `DatabaseConstraintTests` | 唯一、外键和检查约束 | 4 |
| `PetHospitalApplicationTests` | Spring 应用上下文加载 | 1 |

当前共 35 项测试。测试使用固定时钟及独立准备的数据，H2 只出现在测试依赖中，运行应用使用 MySQL。分层测试、MySQL 验证结果见 [第四周作业记录](docs/homework/week-04/index.md)。

2026 年 10 月 15 日完整测试与随后的打包测试均通过，结果为 35 项测试、0 失败、0 错误、0 跳过，构建结果为 `BUILD SUCCESS`。另在真实 MySQL 上验证了 7 项 Repository 与约束测试，以及接口写入、并发冲突和应用重启后数据保留。

连接配置参考 [MySQL Connector/J 文档](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-jdbc-url-format.html)，本机外部配置使用 [Spring Boot 外部配置](https://docs.spring.io/spring-boot/reference/features/external-config.html)。
