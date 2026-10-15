# 宠物医院数据库设计

## 本周业务范围

第四周实现宠物就诊预约。两个核心实体是宠物 `Pet` 与预约 `Appointment`，医生 `Doctor` 是校验接诊状态、出诊时间和预约冲突所需的支撑实体。宠物主人暂以姓名、电话保存在宠物表中，不单独建立账户表。

一个宠物可以拥有多条预约，一名医生可以接收多条预约；每条预约必须关联一个已存在的宠物和一名已存在的医生。历史预约保留，取消不删除记录。

## 数据库与持久化

- 运行数据库：MySQL 8.4，数据库名为 `pet_hospital`，字符集为 `utf8mb4`，存储引擎为 InnoDB。
- 默认连接地址为 `jdbc:mysql://localhost:3306/pet_hospital`，连接参数指定北京时间、UTF-8 及本地开发连接方式。
- 默认用户名为 `pet_hospital`，通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 配置连接，账号权限限定在本项目数据库。
- 应用可读取 `monolith/application-local.properties` 中的本机连接参数，该文件不提交 Git；其他环境可直接使用环境变量。
- 数据由 MySQL 服务保存，停止或重启 Spring Boot 应用不会删除数据库记录。MySQL 安装、建库和连接步骤见根目录 README。
- 使用 Spring Data JPA Repository 读写，JPA 的 `ddl-auto: update` 负责开发阶段建表及结构更新。
- 测试使用独立的 H2 内存数据库，每次测试可重新执行，不修改日常运行数据。

## pets：宠物档案

| 字段 | 类型 | 约束与含义 |
| --- | --- | --- |
| id | BIGINT | 主键，自增 |
| name | VARCHAR(60) | 非空，宠物名称 |
| species | VARCHAR(30) | 非空，种类 |
| age | INTEGER | 非空，年龄 0–50 |
| owner_name | VARCHAR(60) | 非空，主人姓名 |
| owner_phone | VARCHAR(11) | 非空，以 1 开头的 11 位手机号 |
| health_notes | VARCHAR(500) | 可空，健康备注 |

## doctors：医生及出诊安排

| 字段 | 类型 | 约束与含义 |
| --- | --- | --- |
| id | BIGINT | 主键，自增 |
| code | VARCHAR(30) | 非空、唯一，医生工号 |
| name | VARCHAR(60) | 非空，医生姓名 |
| department | VARCHAR(60) | 非空，科室 |
| day_of_week | VARCHAR(9) | 非空，星期枚举字符串，如 `MONDAY` |
| start_time | TIME | 非空，开始接诊时间 |
| end_time | TIME | 非空，结束接诊时间 |
| accepting | BIT(1) | 非空，是否接诊 |

每名医生当前只维护一个每周重复的出诊时段。开始时间须早于结束时间，边界按 30 分钟对齐。工号只使用英文字母、数字、下划线和连字符，保存前统一转为大写。

## appointments：预约

| 字段 | 类型 | 约束与含义 |
| --- | --- | --- |
| id | BIGINT | 主键，自增 |
| pet_id | BIGINT | 非空，外键引用 `pets.id` |
| doctor_id | BIGINT | 非空，外键引用 `doctors.id` |
| appointment_time | DATETIME(6) | 非空，预约开始时间 |
| active_slot | DATETIME(6) | 可空，用于约束当前占用的时段 |
| reason | VARCHAR(500) | 非空，就诊原因 |
| status | VARCHAR(16) | 非空，预约状态字符串 |
| created_at | DATETIME(6) | 非空，创建时间 |

预约表设置联合唯一约束 `(doctor_id, active_slot)` 和 `(pet_id, active_slot)`。待确认、已确认及已完成记录的 `active_slot` 等于 `appointment_time`，取消时将 `active_slot` 设为 `NULL`。这样既能防止同一医生或同一宠物在同一时间重复预约，又能保留取消历史，并允许该时间重新被预约。

外键保证宠物、医生关联有效；医生工号及预约占位唯一约束提供数据库层保障。`ck_pet_age` 限定年龄为 0–50，`ck_doctor_hours` 限定医生开始时间早于结束时间，`ck_appointment_slot` 保证取消状态的占位为空、其他状态的占位等于预约时间。星期和预约状态通过检查约束限制可用值。Service 在写入前先检查并返回明确的业务错误，数据库唯一约束同时防止并发请求绕过预检查。

姓名、就诊原因等字段的非空白校验，以及手机号格式检查，由请求 DTO 的 Bean Validation 完成；数据库同时保留字段非空和长度约束。

## Repository 与请求对象

数据访问接口统一位于 `com.zjgsu.kl.repository`，包括 `PetRepository`、`DoctorRepository` 和 `AppointmentRepository`，均继承 `JpaRepository`。预约 Repository 提供按宠物查询、按医生或宠物检查时段占用等方法。

API 输入由 `PetDtos.CreateRequest`、`DoctorDtos.CreateRequest` 和 `AppointmentDtos.CreateRequest` 承载，Controller 使用 `@Valid` 校验请求，不直接访问 Repository。

## 可重复的测试数据准备

测试数据构造代码位于 `monolith/src/test/java/com/zjgsu/kl/support/TestData.java`，统一创建宠物、医生及预约样例。Repository 测试在每次测试中保存样例，执行 `flush` 和清空持久化上下文后重新查询，以确认数据实际经过数据库读写。

测试使用 `src/test/resources/application.yml` 中的 H2 内存数据库，`ddl-auto: create-drop` 自动建表。`@Transactional` 测试完成后回滚数据，不依赖提前插入的固定主键或手工执行 SQL。Service 测试使用固定时钟和模拟 Repository，API 测试通过 MockMvc 请求创建各自所需的数据，因此从 `monolith/` 执行 `./mvnw test` 可以自动准备测试数据并重复通过。

## 业务规则与事务

1. 宠物、医生必须存在，医生必须处于接诊状态。
2. 预约时间须晚于当前时间，秒与纳秒为零，分钟只能为 `00` 或 `30`。
3. 预约日须符合医生出诊星期，预约的完整 30 分钟须落在出诊时间内。
4. 同一医生、同一宠物在同一时段只能有一条占用记录。
5. 新预约为 `PENDING`；可确认成 `CONFIRMED`，已确认预约到达就诊时间后可完成为 `COMPLETED`。
6. `PENDING` 或 `CONFIRMED` 预约可在就诊时间前取消为 `CANCELLED`；终态不能再次变更。
7. 预约创建及状态变更在 Service 事务中完成，取消同步释放占位。

时间采用 `LocalDateTime`，业务时钟固定使用 `Asia/Shanghai`，请求时间按北京时间解释，不支持跨时区预约。星期和预约状态均使用 VARCHAR 字符串枚举映射。

## 持久化验证

2026 年 10 月 15 日，4 项数据库约束测试全部通过，验证外键、唯一约束及检查约束能够拒绝不合法数据。另使用隔离的 H2 文件数据库启动真实应用，创建并操作预约后停止进程，再以同一数据库文件重启；重新查询得到原有的 2 条宠物、1 条医生和 3 条预约记录，取消历史仍然存在，验证了数据库数据可在进程重启后保留。

上述结果为接入 MySQL 前的 H2 阶段验证记录。当前日常运行配置已切换到 MySQL，H2 仅用于自动化测试。MySQL 阶段的读写、约束和应用重启验证结果记录在第四周作业记录的“数据库持久化”部分。

MySQL 阶段实际执行了 7 项 Repository 读写及数据库约束测试，全部通过。通过真实 HTTP 请求保存数据并重启应用后，宠物编号 `15` 和预约编号 `20` 的内容保持一致，已取消的预约编号 `19` 仍可查询，验证了 MySQL 数据持久化与历史记录保留。

## 本周未设计的表

本周不创建主人账户、病历、账单和权限表，不进行微服务数据库拆分。后续按作业要求逐步补充。
