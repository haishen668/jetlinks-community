# JetLinks 2.1.1 本地 MQTT 复现计划

本文档用于把线上老 jar 已验证可用的 MQTT 接入链，复原到本地 `lsx-device-2.1.1` 开发环境。目标不是改线上老 jar，而是让新源码环境具备相同的协议包、网络组件和网关配置，方便后续二次开发。

## 当前结论

- 后端基线：`jetlinks-community` 分支 `lsx-device-2.1.1`。
- 运行配置：`lsx-device/src/main/resources/application-wj.yml`，默认后端端口 `8848`。
- 数据库类型：老版本使用 MySQL，不使用 2.11 的 PostgreSQL。
- 线上 MQTT 监听：`1883`。
- 线上接入链：
  - `network_config.id = 1816751690245521408`，`type = MQTT_SERVER`，名称 `MQTT直连`。
  - `device_gateway.id = 1816753013292580864`，`provider = mqtt-server-gateway`，名称 `MQTT组件`。
  - `dev_protocol.id = 1816752822044901376`，`type = jar`，名称 `标准协议`。
  - 协议文件 `s_file.id = 452f67679610c2197e97f6d678a93038`，名称 `lsx-official-protocol.jar`。

## 本地已放置的协议文件

运行路径：

```text
F:\project\other\jetlinks\jetlinks-community\data\files\20250307\452f67679610c2197e97f6d678a93038.jar
```

证据路径：

```text
F:\project\other\jetlinks\evidence\online\protocols\lsx-official-protocol.jar
```

校验值：

```text
sha256 = fb0c6144ad056326e26eb829c13759b5080da095c7bb02386c7f064ac059f24e
md5    = 24504ceb0d6570b84b86e6180d9fca9f
size   = 102512 bytes
```

## 前置条件

1. 本地安装并启动 MySQL 5.7 系列，数据库名使用 `device`。
2. 本地启动 Redis，按 `application-wj.yml` 使用 `127.0.0.1:6379`。
3. 本地启动 Elasticsearch 7.x。线上运行版本是 7.17.x，本地优先保持同大版本。
4. IDEA 使用 JDK 8。
5. 从 `F:\project\other\jetlinks\jetlinks-community` 作为工作目录启动后端，否则 `./data/files` 相对路径会不一致。

创建数据库示例：

```powershell
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS device DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
```

## 执行顺序

第一次本地启动建议先让 EasyORM 建表，然后再导入种子数据。

1. 在 IDEA 中使用 JDK 8 打开 `jetlinks-community`。
2. 启动一次 `lsx-device`，配置 profile 为 `wj`。
3. 等待基础表创建完成后停止后端。
4. 导入必须种子：

```powershell
cd F:\project\other\jetlinks\jetlinks-community
mysql -udevice -p device < docs\reconstruction\sql\001-local-mqtt-protocol-gateway-seed.sql
```

5. 可选：导入本地测试产品和测试设备：

```powershell
cd F:\project\other\jetlinks\jetlinks-community
mysql -udevice -p device < docs\reconstruction\sql\002-local-mqtt-test-device-seed.sql
```

6. 重新启动后端。

## 启动命令

开发运行：

```powershell
cd F:\project\other\jetlinks\jetlinks-community
.\mvnw.cmd -pl lsx-device -am spring-boot:run -Dspring-boot.run.profiles=wj
```

打包后运行：

```powershell
cd F:\project\other\jetlinks\jetlinks-community
.\mvnw.cmd -pl lsx-device -am -DskipTests package
java -jar lsx-device\target\lsx-device.jar --spring.profiles.active=wj
```

如果本地 `8848` 已被占用，可以通过命令改后端端口：

```powershell
java -jar lsx-device\target\lsx-device.jar --spring.profiles.active=wj --server.port=18848
```

端口改变后还要同步修改 `001-local-mqtt-protocol-gateway-seed.sql` 里的 `s_file.server_node_id`：

```text
lsx-device:8848  ->  lsx-device:18848
```

原因：`application-wj.yml` 中的 `jetlinks.server-id` 默认是 `${spring.application.name}:${server.port}`，文件管理器会用这个值判断协议 jar 是否在本节点。

## MQTT 验证

后端重新启动后，先确认 `1883` 已监听：

```powershell
netstat -ano | findstr :1883
```

如果导入了可选测试设备，测试身份为：

```text
productId = local_mqtt_test_product
deviceId  = local_mqtt_test_001
secureId  = localTest01
secureKey = localTestSecureKey
```

官方协议认证规则：

```text
clientId = deviceId
username = secureId + "|" + 当前毫秒时间戳
password = md5(secureId + "|" + 当前毫秒时间戳 + "|" + secureKey)
```

时间戳需要在约 5 分钟窗口内，MD5 结果大小写不敏感。后续可以用 MQTTX、mosquitto 或脚本连接 `127.0.0.1:1883` 验证认证和上报链路。

## 为什么先做最小种子

线上数据库约 31GB，完整恢复成本较高。本地开发第一步只需要复现设备接入链的关键数据：

1. `s_file` 让协议 jar 可被本地文件管理器读取。
2. `dev_protocol` 让 JetLinks 注册 jar 协议。
3. `network_config` 让 MQTT server 绑定 `1883`。
4. `device_gateway` 让设备网关把 MQTT 网络和协议关联起来。
5. 可选 `dev_product` 和 `dev_device_instance` 只用于本地连通性测试。

如果这条链能在本地跑通，再按功能逐步补充真实产品、设备、菜单、权限和前端源码。

## 后续待办

- 实际启动本地 MySQL/Redis/Elasticsearch 后执行种子 SQL。
- 验证 `1883` 是否启动，记录日志中的协议加载和 MQTT server 启动信息。
- 用本地测试设备进行一次 MQTT 登录验证。
- 如果需要复现真实设备，再从线上只导出指定产品和设备相关行，不做全量 31GB 恢复。

## 2026-06-03 本地执行状态

- 小皮 MySQL 曾出现面板密码与实际 `root@localhost` 认证不一致的问题。
- 已在用户确认后重置本地小皮 MySQL 5.7 root 密码，并验证可登录。
- 小皮服务恢复时会拉起 MySQL 8.0.12；为贴近线上老环境，当前手动启动的是 `D:\phpstudy_pro\Extensions\MySQL5.7.26\bin\mysqld.exe`。
- 当前 3306 监听进程已验证为 MySQL 5.7.26。
- 已创建本地数据库 `device`，字符集 `utf8`，排序规则 `utf8_general_ci`。
- 已按 `application-wj.yml` 创建并授权后端数据库用户。
- 已验证后端数据库用户能连接 `device` 库。
- 当前 `device` 库表数量为 0，必须先启动后端让 EasyORM 自动建表，然后才能导入 `docs/reconstruction/sql/*.sql`。
- 当前本机 `6379`、`9200`、`8848`、`1883` 未监听，说明 Redis、Elasticsearch、后端和 MQTT server 尚未启动。
- 当前命令行 `java -version` 是 Java 8，但 `mvnw.cmd` 因 `JAVA_HOME` 未设置无法运行；需要配置真正的 JDK8 路径，或在 IDEA 中使用已配置的 JDK8 启动。

## 2026-06-03 本地跑通状态

- 已下载 Redis Windows 5.0.14.1 到 `F:\project\other\jetlinks\runtime`，并使用 `--requirepass` 启动，`redis-cli ping` 返回 `PONG`。
- 已下载并解压 Elasticsearch 7.17.25 到 `F:\project\other\jetlinks\runtime\elasticsearch-7.17.25`，`http://127.0.0.1:9200` 返回版本 `7.17.25`。
- 已使用已有打包产物 `F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar` 启动后端。
- 后端已完成首次启动并创建 48 张 MySQL 表。
- 已导入 `001-local-mqtt-protocol-gateway-seed.sql`，并确认 `network_config`、`device_gateway`、`dev_protocol`、`s_file` 各写入 1 条目标记录。
- 已导入 `002-local-mqtt-test-device-seed.sql`，并确认测试产品和测试设备各写入 1 条目标记录。
- 后端重启后已启动 MQTT server，日志出现 `startup mqtt server [1816751690245521408] on port :1883`。
- 当前端口已验证：
  - `3306` MySQL 5.7.26
  - `6379` Redis 5.0.14.1
  - `9200` Elasticsearch 7.17.25
  - `8848` 后端 HTTP
  - `1883` MQTT server
- 后端 health 已返回 `{"status":"UP"}`。
- 使用本地测试设备发起 MQTT CONNECT 时，网络层已连接到 `1883`，但 CONNACK 返回认证失败。
- 认证失败的根因不是密码公式。协议 jar 反编译确认公式为 `username = secureId + "|" + timestamp`，`password = md5(username + "|" + secureKey)`。
- 当前更可能的问题是：直接 SQL 插入的测试设备没有经过平台设备部署流程进入运行时 `DeviceRegistry`。后续应通过 UI/API 调用 `POST /device-instance/{deviceId}/deploy`，或先处理本地登录验证码后再走 API 部署。
- 本地登录接口当前需要验证码，直接调用 `POST /authorize/login` 会返回 400；这属于后续处理项，不影响协议、网关和端口链路已经复现。
