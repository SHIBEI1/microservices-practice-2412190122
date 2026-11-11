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

## 十五、第五周容器化部署

### 实现范围与环境要求

本周沿用 `monolith/` 中的宠物医院 Spring Boot 单体工程，将应用和 MySQL 8.4.11 一起容器化。原有宠物、医生和预约业务不变，本周完成镜像构建、Compose 一键启动、容器内业务接口验证及命名卷持久化验证，尚未进行微服务拆分。

容器化构建与运行只要求 Docker 和 Docker Compose，不依赖宿主机安装 Java、Maven 或 MySQL。Windows 环境使用 Docker Desktop 的 Linux 容器及 WSL 集成，以下命令在 WSL 或 Linux 的仓库根目录执行。

| 文件 | 作用 |
| --- | --- |
| [monolith/Dockerfile](monolith/Dockerfile) | Maven 构建阶段与 JRE 运行阶段 |
| [monolith/.dockerignore](monolith/.dockerignore) | 限制构建上下文，排除构建产物、IDE 配置和本机密码文件 |
| [compose.yaml](compose.yaml) | 编排应用、数据库、健康检查、端口及命名卷 |
| [.env.example](.env.example) | 本地环境变量模板，不包含真实密码 |

### 环境变量配置

首次运行时复制环境变量模板；已有 `.env` 时保留原配置：

```bash
test -f .env || cp .env.example .env
```

用文本编辑器将 `.env` 中 `MYSQL_PASSWORD` 和 `MYSQL_ROOT_PASSWORD` 的占位内容改为本机设置的不同密码。实际 `.env` 已加入 Git 忽略规则，不提交数据库密码。

| 环境变量 | 模板值或用途 |
| --- | --- |
| `APP_PORT` | `8081`，应用映射到本机的端口 |
| `MYSQL_DATABASE` | `pet_hospital` |
| `MYSQL_USER` | `pet_hospital`，应用数据库账号 |
| `MYSQL_PASSWORD` | 应用数据库账号密码 |
| `MYSQL_ROOT_PASSWORD` | MySQL 管理员密码，应用不使用该账号 |

Compose 将数据库地址、账号和密码分别注入应用的 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。数据库地址使用服务名 `mysql:3306`，不使用容器内的 `localhost`。应用与数据库共享 Compose 默认网络，MySQL 不映射宿主机端口，因此不会占用本机已有的 MySQL 端口。

MySQL 初始化账号和数据库的环境变量仅在数据卷为空时生效。已有数据卷应保持现有账号密码，单独修改 `.env` 不会自动修改数据库中的密码。

### 镜像构建

在仓库根目录执行：

```bash
docker build -t pet-hospital:week05 ./monolith
docker image ls pet-hospital:week05
```

构建阶段使用 `maven:3.9.16-eclipse-temurin-25`，下载依赖并执行 `mvn -B -ntp verify`，完成测试和可执行 JAR 打包；运行阶段使用 `eclipse-temurin:25-jre`，仅从构建阶段复制应用 JAR，以非 root 用户 `app` 执行 `java -jar /app/app.jar`，不包含 Maven 和源码。

`.dockerignore` 只放行 `pom.xml` 与 `src/`，并进一步排除本机构建产物、IDE 配置、密码文件及下载标记文件。首次构建不需要提前在宿主机执行 Maven；重复构建显示 `CACHED` 表示复用构建缓存。

### 一键启动、状态与停止

构建并启动应用及数据库，等待健康检查通过：

```bash
docker compose up -d --build --wait --wait-timeout 180
```

查看服务状态及日志：

```bash
docker compose ps
docker compose logs --tail=100 app
docker compose logs --tail=100 mysql
```

Compose 中的 `app` 依赖 `mysql` 的 `service_healthy` 状态。数据库健康检查验证应用账号能够连接业务数据库；应用健康检查访问 `/actuator/health`。正常启动后两个服务均显示 `healthy`。

应用容器内部监听 `8080`，本机默认通过 `http://localhost:8081` 访问。该端口与本地开发方式使用的 `8080` 区分；如修改 `APP_PORT`，接口命令也应使用相应端口。

停止并移除本项目容器与网络：

```bash
docker compose down
```

该命令保留数据库命名卷。日常停止和本周持久化验证均不使用 `docker compose down -v`，因为 `-v` 会删除命名卷及其中的数据库数据。

### 健康检查与业务 API

应用健康检查：

```bash
curl -i -w '\n' http://localhost:8081/actuator/health
```

正常返回 `200`，响应包含 `"status":"UP"`。

创建宠物档案，正常返回 `201`、`Location` 和包含实际资源编号的 JSON：

```bash
curl -i -w '\n' -X POST http://localhost:8081/api/pets \
  -H 'Content-Type: application/json' \
  -d '{"name":"团团","species":"猫","age":2,"ownerName":"孔磊","ownerPhone":"13800000000","healthNotes":"第五周Docker容器化验证"}'
```

查询宠物列表，正常返回 `200`，其中包含刚创建的宠物：

```bash
curl -i -w '\n' http://localhost:8081/api/pets
```

两个业务示例均可直接执行，不依赖预先存在的资源编号。宠物详情接口为 `GET /api/pets/{id}`，编号以创建响应为准；本周截图中的编号为 `3`，不是固定编号。

### 容器重建与数据持久化

MySQL 将 `/var/lib/mysql` 挂载到命名卷 `mysql_data`，当前 Compose 项目对应的实际卷名为 `pet-hospital_mysql_data`。卷与容器生命周期分离，移除容器后重新启动仍挂载原卷。

在同一个终端中，先输入创建响应中的实际宠物编号：

```bash
read -rp 'Pet ID: ' PET_ID
```

查询原记录，删除并重新创建容器，再查询同一编号：

```bash
curl -i -w '\n' "http://localhost:8081/api/pets/$PET_ID"

docker compose down
docker compose up -d --wait --wait-timeout 180

docker compose ps
curl -i -w '\n' "http://localhost:8081/api/pets/$PET_ID"
```

本周验证中，宠物编号 `3` 在重建前后均返回 `200`，名称、种类、年龄、主人信息与健康备注保持一致，说明数据由命名卷中的 MySQL 保存，而非保存在应用或数据库容器的临时文件层中。

2026 年 10 月 20 日首次镜像构建中的 35 项测试全部通过，后续重复构建、Compose 启动、接口访问和容器重建验证均成功。完整过程与八张运行截图见 [第五周作业记录](docs/homework/week-05/index.md)。

## 十六、第六周微服务拆分

本周保留 `monolith/` 原单体，新增三个独立的 Spring Boot 工程，默认 Compose 编排切换为微服务。第十五节及第五周记录中的单体运行方式作为历史版本保留。服务边界、数据库归属及 Mermaid 架构图见 [微服务拆分设计](docs/microservices-design.md)。

| 服务 | 工程目录 | 默认本机端口 | 数据库及账号 |
| --- | --- | --- | --- |
| `pet-service` | `services/pet-service/` | 18082 | `pet_hospital_pet` / `pet_user` |
| `doctor-service` | `services/doctor-service/` | 18083 | `pet_hospital_doctor` / `doctor_user` |
| `appointment-service` | `services/appointment-service/` | 18084 | `pet_hospital_appointment` / `appointment_user` |

三个服务各有独立 `pom.xml`、`src/`、配置、测试和 Dockerfile，只读写各自数据库。预约保存远端宠物和医生的 ID，不建立跨库外键；创建预约时通过 RestTemplate 查询宠物和医生，再执行本地校验与保存。

### 配置与一键启动

在 WSL 或 Linux 的仓库根目录执行。首次使用先复制模板：

```bash
test -f .env || cp .env.example .env
```

已有 `.env` 时不覆盖第五周账号密码，补齐 `.env.example` 中新增的 `PET_*`、`DOCTOR_*`、`APPOINTMENT_*` 配置，并为三个服务设置不同的数据库密码。真实 `.env` 不提交，旧单体的账号、密码和 `mysql_data` 卷继续保留。

容器内服务地址通过 `PET_SERVICE_URL`、`DOCTOR_SERVICE_URL` 覆盖，默认使用 `http://pet-service:8080`、`http://doctor-service:8080`；连接和读取超时通过 `HTTP_CONNECT_TIMEOUT`、`HTTP_READ_TIMEOUT` 设置，默认分别为 `2s`、`3s`。

容器构建和运行不要求宿主机安装 Java、Maven 或额外 MySQL。Windows 使用 Docker Desktop 的 Linux 容器；WSL 已启用 Docker 集成时使用 `docker`，尚未启用集成但可以访问 Windows Docker CLI 时使用 `docker.exe`：

```bash
DOCKER_BIN=docker
if ! docker compose version >/dev/null 2>&1; then
  DOCKER_BIN=docker.exe
fi

"$DOCKER_BIN" compose up -d --build --wait --wait-timeout 240
"$DOCKER_BIN" compose ps
```

默认启动三个应用和三个 MySQL 8.4.11 数据库容器。容器之间通过服务名通信，各数据库不映射宿主机端口，并使用各自命名卷保存数据。本机端口可以通过 `PET_SERVICE_PORT`、`DOCTOR_SERVICE_PORT`、`APPOINTMENT_SERVICE_PORT` 覆盖。

查看日志和停止服务：

```bash
"$DOCKER_BIN" compose logs --tail=100 appointment-service
"$DOCKER_BIN" compose stop
```

移除容器时使用不带 `-v` 的 `compose down`，保留所有数据库卷。原单体不属于默认启动范围，需要时可单独启动：

```bash
"$DOCKER_BIN" compose --profile monolith up -d --build app
```

### 健康接口与跨服务验证

三个服务分别提供健康检查：

```bash
curl -i http://localhost:18082/actuator/health
curl -i http://localhost:18083/actuator/health
curl -i http://localhost:18084/actuator/health
```

从仓库根目录运行完整业务验证：

```bash
./scripts/smoke-test.sh
```

脚本需要 curl 和 Python 3，自动读取 Compose 中的端口，检查每个健康接口，创建宠物和医生，以实际 ID 提交未来预约，验证查询、冲突、确认、取消、取消后重新预约及依赖资源不存在时的失败响应。工号和预约日期自动生成，可以重复执行，不使用固定的历史 ID。

追加真实依赖停机验证：

```bash
./scripts/smoke-test.sh --with-unavailable
```

该模式暂时停止本项目 `pet-service`，验证预约请求返回 `503`、本地预约数量不变，再恢复宠物服务；异常退出时也会尝试恢复服务。依赖资源不存在返回 `404`，连接或读取失败返回 `503`，无效远端响应返回 `502`，不会跳过校验继续写入。已有预约记录仍可独立查询。

### 每个服务的测试命令

测试使用 H2 内存数据库和固定时钟，无需启动 MySQL 或其他服务。使用已有 Maven Wrapper，在仓库根目录分别执行：

```bash
./monolith/mvnw -B -ntp -f services/pet-service/pom.xml test
./monolith/mvnw -B -ntp -f services/doctor-service/pom.xml test
./monolith/mvnw -B -ntp -f services/appointment-service/pom.xml test
```

Maven Wrapper 仅作为现有构建工具使用，三个服务并不依赖单体的业务代码。已安装 Maven 时，也可以进入任一服务目录执行 `mvn test`；宿主机测试需 JDK 25 或更高版本。

当前宠物服务 4 项测试、医生服务 5 项测试、预约服务 15 项测试，共 24 项。测试覆盖上下文与健康接口、各自 Repository 读写、参数校验、业务规则、HTTP 依赖校验、超时和依赖失败不写入。Dockerfile 构建阶段同样执行测试后再打包。

### 第六周作业记录

[第 06 周作业记录](docs/homework/week-06/index.md) 汇总服务拆分设计、代码拆分、本地编排和验证四部分，记录启动与测试命令、跨服务预约流程、404 与 503 失败场景、本地数据变化及验证截图。

2026 年 11 月 1 日验证中，三个微服务与各自 MySQL 数据库均健康运行，24 项独立测试全部通过。跨服务预约创建返回 `201`、查询返回 `200`，依赖资源不存在返回 `404`、依赖停机返回 `503`，两种失败场景下预约总数均保持为 `7`，依赖恢复后健康检查通过。


## 十七、第七周 Nacos 服务发现

### 依赖与运行配置

三个业务服务使用 Spring Cloud `2025.1.0` 和 Spring Cloud Alibaba `2025.1.0.0` BOM 管理 Discovery Starter，基础工程使用 Spring Boot `4.0.8` 和 Java 25。预约业务通过 `DiscoveryClient.getInstances(...)` 获取当前已知实例，将实例 URI 与原业务路径组合后使用 RestTemplate 发起 HTTP 请求。每次参与方校验均重新获取实例列表，连接超时为 `2s`，读取超时为 `3s`。

三个服务使用 `PET_HOSPITAL_GROUP` 分组，命名空间默认使用 `public`，对应 `NACOS_NAMESPACE` 留空。服务名沿用 [微服务拆分设计](docs/microservices-design.md)。

| 服务名 | 本机端口 | 容器端口 |
| --- | --- | --- |
| `pet-service` | 18082 | 8080 |
| `doctor-service` | 18083 | 8080 |
| `appointment-service` | 18084 | 8080 |

Nacos 使用作业指定镜像 `nacos/nacos-server:v3.2.4`，单机模式的数据目录挂载到 `nacos_data` 命名卷。控制台、服务端 HTTP 和 SDK gRPC 分别使用以下端口。

| 用途 | 本机端口 | 容器端口 | 访问方式 |
| --- | --- | --- | --- |
| Nacos 控制台 | 28080 | 8080 | `http://localhost:28080/` |
| Nacos Server HTTP | 18848 | 8848 | 本机 API 使用 `http://localhost:18848/nacos` |
| Nacos SDK gRPC | 19848 | 9848 | SDK 通信，端口为 Server HTTP 端口加 1000 |

端口均绑定宿主机 `127.0.0.1`。应用容器从环境变量读取 `NACOS_SERVER_ADDR=nacos:8848`，通过 Compose 网络连接注册中心。Nacos 中显示的实例 IP 与 `8080` 端口用于容器间 HTTP 请求，本机通过各应用映射端口访问业务接口。

### 首次初始化与启动

在 WSL 或 Linux 的仓库根目录执行。先启动 Docker Desktop 的 Linux 容器引擎，选择可用的 Docker CLI。

```bash
export DOCKER_BIN=docker
if ! docker compose version >/dev/null 2>&1; then
  export DOCKER_BIN=docker.exe
fi

test -f .env || cp .env.example .env
```

首次使用时按 `.env.example` 设置 MySQL 凭据及新增的 `NACOS_*` 配置。Nacos 管理员使用 `NACOS_USERNAME=nacos`，密码填写在 `NACOS_PASSWORD`。`NACOS_AUTH_TOKEN` 填写 Base64 编码的随机密钥，解码后至少 32 字节，Server identity 的 Key 和 Value 填写独立非空值。实际凭据保存在本地 `.env`，模板保留占位内容，Git 忽略规则覆盖真实环境文件。

先单独启动 Nacos，等待其就绪，再初始化管理员密码。

```bash
"$DOCKER_BIN" compose up -d --wait --wait-timeout 180 nacos

python3 scripts/nacos-admin.py init
```

初始化脚本从 Compose 解析后的环境读取本地密码，通过首次管理员初始化 API 设置密码并验证登录。重复执行时验证现有凭据，输出初始化或登录结果。密码与认证 Token 仅在进程内处理。

初始化后构建并启动业务服务与数据库。

```bash
"$DOCKER_BIN" compose up -d --build --wait --wait-timeout 240

"$DOCKER_BIN" compose ps
```

打开 `http://localhost:28080/`，使用本地 `.env` 中的 Nacos 账号密码登录，选择默认命名空间和 `PET_HOSPITAL_GROUP`，查看三个业务服务及实例 IP、端口、健康状态。根地址会跳转到 `/next/`，验证脚本检查重定向后的最终 HTTP 状态。

日常启动可以直接执行上述完整 Compose 命令。Nacos 管理员数据保存在命名卷中。停止和移除容器分别使用以下命令，`down` 保留命名卷。

```bash
"$DOCKER_BIN" compose stop

"$DOCKER_BIN" compose down
```

### 查看注册实例和业务调用

从仓库根目录查看实际注册分组与实例。

```bash
python3 scripts/nacos-admin.py list
```

三个应用的健康接口仍使用各自映射端口。

```bash
curl -i http://localhost:18082/actuator/health
curl -i http://localhost:18083/actuator/health
curl -i http://localhost:18084/actuator/health
```

执行完整跨服务预约验证。

```bash
bash scripts/smoke-test.sh
```

脚本检查实际映射到 `28080` 的 Nacos 控制台 HTTP 地址、三个业务健康接口和三个服务的可用注册实例，然后创建宠物、医生和预约，验证查询、冲突、确认取消及依赖资源不存在时的 `404` 响应。任一检查失败时返回非零退出状态。

查看预约服务实际选取的实例 URI。

```bash
"$DOCKER_BIN" compose logs --tail=100 appointment-service | grep 'Nacos服务发现'
```

### 下线、失败与恢复验证

从仓库根目录执行下线与恢复验证。

```bash
bash scripts/smoke-test.sh --with-unavailable
```

脚本先完成正常预约，再停止 `pet-service`，等待其可用实例数变为 `0` 并输出注册列表。此时预约服务自身健康接口仍可访问，历史预约能够查询。脚本使用新的 `09:30` 时段提交有效预约，预期返回 `503`，并比较前后预约数量。

随后恢复宠物服务，等待应用健康和实例重新注册，再使用同一组预约参数创建与查询预约，预期分别返回 `201`、`200`，预约数量增加一条。异常退出时脚本会尝试恢复已停止的宠物服务。

| 状态 | 业务结果 | 预约数据变化 |
| --- | --- | --- |
| 宠物、医生实例正常 | 创建 201，查询 200 | 新增预约 |
| 依赖资源不存在 | 404 | 数量保持一致 |
| 宠物服务下线或旧地址连接失败 | 503 | 数量保持一致 |
| 宠物实例恢复并可访问 | 创建 201，查询 200 | 新增一条预约 |

SDK 连接、注册实例和应用健康分别表示不同层面的状态。SDK 连接表示应用与 Nacos 通信可用，注册实例表示注册中心当前保存的服务地址及实例状态，Actuator 通过 HTTP 报告当前应用的健康结果。宠物服务下线时，预约服务的 SDK 连接和自身健康仍可正常，但创建预约会因宠物依赖不可用返回 `503`。实例注销由服务停止生命周期和 SDK 连接变化驱动，Actuator 检查与注册操作分别处理。

远端校验成功后，本地保存仍可能因时段冲突、数据库故障或进程中断失败。服务发现负责选择调用地址，本地事务负责预约保存，跨服务调用与本地写入之间仍存在部分成功的窗口。本周远端调用为查询操作，本地失败时以最终预约响应和记录为准。

### 各服务独立测试

从仓库根目录进入对应服务目录执行测试。

```bash
(cd services/pet-service && ../../monolith/mvnw -B -ntp test)

(cd services/doctor-service && ../../monolith/mvnw -B -ntp test)

(cd services/appointment-service && ../../monolith/mvnw -B -ntp test)
```

测试配置关闭 Nacos Discovery 和自动注册，使用 H2 数据库。预约服务通过模拟 DiscoveryClient 和 HTTP 响应验证实例选择、空实例、发现失败、远端错误以及原有业务规则。宠物服务 4 项、医生服务 5 项、预约服务 19 项，共 28 项测试全部通过。

依赖版本对应关系参考 [Spring Cloud Alibaba 2025.1.0.0](https://github.com/alibaba/spring-cloud-alibaba/tree/2025.1.0.0)，鉴权及首次管理员初始化参考 [Nacos 官方鉴权说明](https://nacos.io/en/docs/latest/manual/admin/auth/)。

### 第七周作业记录

[第 07 周作业记录](docs/homework/week-07/index.md) 汇总 Nacos 部署注册、服务发现调用、健康状态与下线恢复、验证脚本四部分，包含运行命令、状态码、数据变化和十张验证截图。

2026 年 11 月 5 日验证中，宠物和医生编号均为 `7`，正常创建预约 `14` 并查询成功。宠物服务下线后新请求返回 `503`，预约数量保持为 `14`。服务恢复后同一请求创建预约 `16`，数量增加到 `15`。默认脚本单独运行退出 `0`，模拟控制台连接失败退出 `7`，三个服务共 `28` 项测试全部通过。

## 十八、macOS 本机开发与启动

2026 年 11 月迁移至 Apple Silicon Mac。宿主机使用 Temurin JDK 25、Docker Desktop 和 VS Code Java 扩展；Maven 使用 `monolith/mvnw` 下载的 3.9.16。三个业务服务、三个 MySQL 数据库与 Nacos 通过 Compose 运行。

首次准备时，在仓库根目录复制 `.env.example` 为 `.env`，将密码及 Nacos 鉴权占位值替换为本机凭据；真实 `.env` 已被 Git 忽略。Nacos Token 使用解码后至少 32 字节的 Base64 随机密钥。恢复脚本权限：

```bash
chmod +x monolith/mvnw scripts/smoke-test.sh
```

在 macOS 终端进入本机仓库根目录后执行，无需进入 WSL：

```bash
docker compose up -d --wait --wait-timeout 180 nacos
python3 scripts/nacos-admin.py init
docker compose up -d --build --wait --wait-timeout 240
docker compose ps
bash scripts/smoke-test.sh
```

首次初始化完成后，日常启动可直接执行 `docker compose up -d --wait --wait-timeout 240`；代码修改后加 `--build`。停止使用 `docker compose stop`，保留数据库数据卷。

Nacos 控制台地址为 `http://localhost:28080/`，账号为 `.env` 中的 `NACOS_USERNAME` 和 `NACOS_PASSWORD`。宠物、医生和预约服务端口分别为 `18082`、`18083`、`18084`，健康接口均为 `/actuator/health`。尚无前端页面，业务通过 REST API 操作。

独立测试不依赖运行中的 MySQL 或 Nacos，在仓库根目录分别执行：

```bash
./monolith/mvnw -B -ntp -f services/pet-service/pom.xml test
./monolith/mvnw -B -ntp -f services/doctor-service/pom.xml test
./monolith/mvnw -B -ntp -f services/appointment-service/pom.xml test
```

根目录 `pom.xml` 仍是早期 Java 练习，并非微服务聚合工程。旧 WSL 的本机密码、IDE 路径和 Docker 数据卷不会随源码自动迁移；本机数据库首次启动会创建新数据。

2026 年 11 月 10 日 Mac 本机验证：三个服务共 28 项测试全部通过，ARM64 镜像构建成功，七个容器健康运行。真实 HTTP 验证覆盖创建、查询、冲突、确认、取消及 404；宠物服务下线时新预约返回 503 且记录数保持为 2，恢复后创建成功，记录数增加到 3。验证数据保留在本机数据卷中。

## 十九、第八周服务通信与负载均衡

预约服务通过 OpenFeign 按服务名查询宠物和医生，Spring Cloud LoadBalancer 从 Nacos 实例列表选择调用目标。Compose 运行两个共享数据库的宠物服务实例，副本通过容器网络接收请求。调用方、被调用方及接口路径见[服务通信与负载均衡](docs/service-communication.md)。

沿用本机配置，在仓库根目录运行以下命令。

```bash
docker compose up -d --build --wait --wait-timeout 240
bash scripts/e2e-test.sh
```

端到端脚本会创建测试预约，核对两个实例的查询日志，并验证依赖下线与恢复。11 月 11 日的验证中，六次正常预约的宠物查询平均分配至两个实例，各三次。两实例下线时返回 `503` 且预约未写入，恢复后同一请求返回 `201`。三个服务共 28 项自动化测试通过。[第 08 周作业记录](docs/homework/week-08/index.md)
