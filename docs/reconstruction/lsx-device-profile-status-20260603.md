# LSX 设备基础画像迁移状态

日期：2026-06-03

## 当前结论

第一阶段“LSX 设备基础画像闭环”已经迁移到 2.11 分支 `lsx-migration-2.11`。

本阶段只覆盖设备基础画像和当前 SIM 卡状态：

- 2.11 `dev_device_instance` 已补齐旧 2.1.1 jar 使用的 LSX 扩展字段。
- 2.11 已新增轻量 `dev_device_card` 表，用于保存设备当前 SIM 卡。
- 属性上报 `ReportPropertyMessage` 会同步设备画像字段和当前卡。
- 设备详情 `DeviceDetail` 会返回 LSX 字段、`subDomain = md5(mac)` 和 `cards`。

本阶段没有迁移：

- 客户设备列表。
- REWEB 功能调用。
- 设备任务。
- 告警。
- 完整 `/network/card/**` 物联网卡平台接口。
- 线上历史大体量数据。

## 已修改代码

- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/entity/DeviceInstanceEntity.java`
  - 新增 LSX 字段：`model`、`firmware_version`、`mac`、`imei`、`adress`、`t24g_num`、`t5g_num`、`rsrp`、`rsrq`、`sinr`、`network`、`lat`、`lng`、`location`、`passwd`、`operator`、`switch_state`、`sync_flag`、`ping_addr`、`ping_retry`、`online_time`、`offline_time`。
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/entity/DeviceCardEntity.java`
  - 新增 `dev_device_card` 实体。
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/entity/DeviceDetail.java`
  - 新增 LSX 返回字段、`cards`、`withCards(...)`。
  - 根据 `mac` 计算 `subDomain`。
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/service/LsxDevicePropertySynchronizer.java`
  - 抽出属性上报到设备画像/当前卡的纯映射逻辑。
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/service/DeviceMessageBusinessHandler.java`
  - 订阅 `/device/*/*/message/property/report` 和 `device/*/*/message/property/report`。
  - 上报后更新设备画像，并按 `device_id + iccid` upsert 当前卡。
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/service/LocalDeviceInstanceService.java`
  - 查询设备详情时批量加载 `dev_device_card` 并挂到 `DeviceDetail.cards`。
- `jetlinks-manager/device-manager/src/test/java/org/jetlinks/community/device/service/LsxDevicePropertySynchronizerTest.java`
  - 覆盖真实设备上报样例的字段映射、当前卡、详情返回字段。

## 验证记录

### 单元测试

命令：

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-21.0.3'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -pl jetlinks-manager/device-manager -am -Dtest=LsxDevicePropertySynchronizerTest -DfailIfNoTests=false test
```

结果：

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 后端打包

命令：

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-21.0.3'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package
```

第一次失败原因：

```text
Unable to rename application.jar to application.jar.original
```

原因是旧的 2.11 后端进程占用了 `application.jar`。停止 PID `15924` 后重新打包成功。

最终结果：

```text
BUILD SUCCESS
```

打包产物：

```text
jetlinks-standalone/target/application.jar
大小：214415842 bytes
时间：2026-06-03 19:46:28
```

### 本地 2.11 运行验证

当前 2.11 后端已重新启动：

```text
PID: 46380
URL: http://127.0.0.1:8848
```

健康检查：

```powershell
Invoke-RestMethod -Uri http://127.0.0.1:8848/actuator/health
```

结果：

```json
{"status":"UP"}
```

### 数据库结构验证

数据库：

```text
PostgreSQL/TimescaleDB
database: jetlinks_211_migration
schema: public
```

`dev_device_instance` 已存在的 LSX 字段：

```text
adress
firmware_version
imei
lat
lng
location
mac
model
network
offline_time
online_time
operator
passwd
ping_addr
ping_retry
rsrp
rsrq
sinr
switch_state
sync_flag
t24g_num
t5g_num
```

缺失字段：

```text
无
```

`dev_device_card` 已存在字段：

```text
id
device_id
iccid
operator
msisdn
imsi
slot
use_state
status
used_flow
create_time
modify_time
creator_id
modifier_id
```

## 当前环境状态

- 2.1.1 后端：已停止。
- 2.11 后端：运行中，PID `46380`，端口 `8848`。
- 2.11 前端：沿用之前已启动的 2.11 前端环境；如端口占用或页面异常，下轮先重新确认前端进程。
- 2.11 数据库：`jetlinks_211_migration`，已自动更新 LSX 字段和卡表。

## 下一步建议

1. 用测试设备 `869624060285951` 对 2.11 本地端口上报一次，确认 `dev_device_instance` 字段和 `dev_device_card` 行被真实消息更新。
2. 在 2.11 前端设备详情页接入并展示这些 LSX 字段。
3. 再迁移 REWEB 功能调用：先迁 `subDomain/passwd/port` 的前端入口和 function 调用，再处理 FRP/域名依赖。
4. 最后再考虑客户设备、任务、告警和物联网卡管理模块。
