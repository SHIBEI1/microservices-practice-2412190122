# Nacos 配置说明

## 配置范围与环境

预约服务和用户服务通过 Spring Cloud Alibaba Nacos Config `2025.1.0.0` 接入配置中心，使用 `spring.config.import` 导入 `<spring.application.name>-<spring.profiles.active>.properties`，开启动态刷新。`APP_ENV` 选择 `dev`、`test` 或 `prod`，默认 `dev`；预约服务本地独立测试关闭 Nacos Config，不依赖注册中心。

配置使用现有 `NACOS_NAMESPACE`，留空对应 `public`，Group 由 `NACOS_CONFIG_GROUP` 指定，默认为 `PET_HOSPITAL_CONFIG`。服务发现继续使用独立的 `NACOS_GROUP`。环境通过不同 Data ID 隔离；同一命名空间下不同 Data ID 是不同配置资源，修改 test 不会更新 dev。该划分是配置隔离，不代替数据库与网络隔离；同时部署多套业务环境还需分别设置 Compose 项目名、数据库、服务发现分组与端口。

| 环境 | Data ID | Group（默认） | 用途 |
| --- | --- | --- | --- |
| dev | `appointment-service-dev.properties` | `PET_HOSPITAL_CONFIG` | 开发预约开关、连接池大小与日志级别 |
| dev | `user-service-dev.properties` | `PET_HOSPITAL_CONFIG` | 开发 JWT 有效期 1800 秒 |
| test | `appointment-service-test.properties` | `PET_HOSPITAL_CONFIG` | 自动验证或演示使用的预约参数 |
| test | `user-service-test.properties` | `PET_HOSPITAL_CONFIG` | 测试 JWT 有效期 300 秒 |
| prod | `appointment-service-prod.properties` | `PET_HOSPITAL_CONFIG` | 生产配置模板，尚未代表生产部署 |
| prod | `user-service-prod.properties` | `PET_HOSPITAL_CONFIG` | 生产 JWT 有效期模板 900 秒 |

非敏感配置示例位于 `configs/nacos/<环境>/`。数据库密码、Nacos 密码、JWT 密钥和账号密码哈希继续由本地环境变量提供，不发布到配置中心。

## 初始化与启动

沿用本地 `.env` 中的数据库、Nacos 和认证凭据。环境变量设置方法见 [README](../README.md#本地环境变量设置)。首次使用时从 `.env.example` 复制模板并填写本地值，保留 `APP_ENV=dev`，新增 `NACOS_CONFIG_GROUP=PET_HOSPITAL_CONFIG`。不要覆盖已有 `.env`。

```bash
docker compose up -d --wait --wait-timeout 180 nacos
python3 scripts/nacos-admin.py init
python3 scripts/nacos-config.py --env dev
python3 scripts/nacos-config.py --env test
# 需要生产配置模板时再发布
python3 scripts/nacos-config.py --env prod
docker compose up -d --build --wait --wait-timeout 240
bash scripts/config-test.sh
```

发布脚本默认保留已有配置，只创建缺失的 Data ID；需要明确以仓库示例覆盖时使用 `--overwrite`。服务启动必须先发布对应环境配置，导入不使用 `optional:`，避免在配置缺失时误以为已经接入配置中心。选择 test 环境时在独立部署配置中设置 `APP_ENV=test`，对应测试数据库和服务发现分组也应独立设置。

## 动态配置与验证接口

`hospital.booking.enabled` 控制创建预约，关闭时返回 `503` 和“预约登记暂时关闭，请稍后重试”，在参与方查询及数据库写入之前结束请求，不影响既有预约的查询或状态操作。`BookingPolicy` 通过 `@RefreshScope` 在 Nacos 推送后重新读取配置，恢复为 `true` 后重新允许登记，无需打包或重启。

管理员携带 JWT 访问 `GET /api/appointments/config`，可看到明确列出的非敏感字段：配置环境、修订标识、预约开关和 JVM 启动时间。普通用户访问返回 `403`，未登录返回 `401`。接口不暴露完整 Environment、数据库连接信息或凭据。

用户服务的 `hospital.auth.expires-in-seconds` 通过可刷新的 `TokenPolicy` 管理，范围为 60 至 86400 秒。变更影响新签发令牌，已签发令牌仍按原有过期时间验证。连接池参数和日志级别由配置中心集中管理，本周动态业务验证以预约开关为准，不承诺所有配置项均能热更新。

## 验证脚本

`scripts/config-test.sh` 限定在 dev 环境运行，自动完成以下操作。

1. 登录管理员并读取预约服务实际配置，确认普通用户无法读取诊断接口。
2. 备份 dev 预约配置，发布新的修订标识并关闭预约，轮询服务接口确认新值已读取。
3. 验证预约返回 `503`、记录未增加、进程启动时间不变。
4. 恢复开关并验证请求进入参与方校验，缺失宠物返回 `404`。
5. 用同一用户服务镜像启动不注册服务、不发布端口的临时 test 实例，实际登录获取 JWT，检查有效期为 test 配置值，并与 dev 实例比较。
6. 清理临时实例，恢复原始 dev 配置并等待服务读取；异常退出也执行恢复。验证期间不要同时修改该 Data ID。

脚本成功输出 `PASS` 并退出 `0`，失败输出 `FAIL` 并退出非零。它不会打印密码、管理令牌或业务 JWT。`configs/nacos/` 中的文件仅包含白名单配置键，发布脚本拒绝其他键；完整环境文件仍受 `.gitignore` 保护。

## 验证记录

2026 年 11 月 26 日，Nacos 管理 API 读取到六份环境配置。dev 和临时 test 用户服务的实际登录响应分别返回 `expiresIn=1800` 和 `expiresIn=300`，证明服务读取了不同 Data ID 的参数。

动态预约开关从 `true` 改为 `false` 后，服务诊断接口读取到新修订标识和关闭状态，创建预约返回 `503`，本轮截图中预约数量保持为 35。恢复开关后，相同缺失宠物请求返回 `404`，说明已重新进入参与方校验。变更前、关闭后和重新开放后三次响应的 `startedAt` 一致，服务未重启。验证结束后原始 `dev-v1` 配置已恢复并被服务读取，临时 test 实例已清理。

配置验证脚本通过；预约服务 20 项测试以及鉴权、冒烟、端到端验证通过。截图、各项运行命令与完成结果见[第 10 周作业记录](homework/week-10/index.md)。
