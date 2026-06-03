# LSX 设备基础画像迁移实施计划

日期：2026-06-03

## 目标

把旧 `lsx-device.jar` 已确认存在的 LSX 设备基础画像能力迁移到 JetLinks 2.11：

- 设备表能保存 LSX 扩展字段。
- 设备详情能返回 LSX 字段、`subDomain` 和当前 SIM 卡。
- 属性上报后能同步设备画像和当前 SIM 卡。

本计划只覆盖第一个迁移切片，不包含客户设备、REWEB 调用、设备任务、告警、完整物联网卡平台和历史数据导入。

## 实施范围

### 1. 增加失败测试

状态：已完成。

新增测试文件：

```text
jetlinks-manager/device-manager/src/test/java/org/jetlinks/community/device/service/LsxDevicePropertySynchronizerTest.java
```

测试使用真实设备样例：

```text
deviceId: 869624060285951
iccid: 89861123242042925346
mac: D0:A0:D6:8C:B4:C8
imei: 869624060285951
version: 11.0.0.183(H72SP1C00)
network: 5G
operator: CT
switch_state: 0
ping_retry: 5
```

首次 RED 结果：编译失败，缺少 `DeviceCardEntity`、`DeviceDetail.withCards(...)` 和 LSX 字段，符合预期。

### 2. 扩展设备实体和卡实体

状态：已完成。

修改：

```text
DeviceInstanceEntity.java
DeviceCardEntity.java
LsxDevicePropertySynchronizer.java
```

新增设备字段：

```text
model
firmware_version
mac
imei
adress
t24g_num
t5g_num
rsrp
rsrq
sinr
network
lat
lng
location
passwd
operator
switch_state
sync_flag
ping_addr
ping_retry
online_time
offline_time
```

新增卡表实体：

```text
dev_device_card
```

说明：保留旧版列名 `adress`，避免后续从老库或老 jar 行为映射时增加额外转换。

### 3. 扩展设备详情返回

状态：已完成。

修改：

```text
DeviceDetail.java
LocalDeviceInstanceService.java
```

结果：

- `DeviceDetail` 返回 LSX 字段。
- `DeviceDetail.with(DeviceInstanceEntity)` 复制 LSX 字段。
- `subDomain` 由 `md5(mac)` 动态计算，不落库。
- `LocalDeviceInstanceService` 查询详情时批量加载 `dev_device_card` 并设置到 `cards`。

### 4. 接入属性上报同步

状态：已完成。

修改：

```text
DeviceMessageBusinessHandler.java
```

监听主题：

```text
/device/*/*/message/property/report
device/*/*/message/property/report
```

处理流程：

1. 忽略非 `ReportPropertyMessage`。
2. 按 `deviceId` 查询当前设备。
3. 使用 `LsxDevicePropertySynchronizer.applyReportedProperties(...)` 生成设备更新对象和当前卡。
4. 更新 `dev_device_instance`。
5. 如果上报 `iccid` 合法，则按 `device_id + iccid` upsert `dev_device_card`。
6. 同设备其他卡的 `use_state` 设置为 `0`。

### 5. 验证和运行

状态：已完成。

单元测试：

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

完整打包：

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-21.0.3'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package
```

结果：

```text
BUILD SUCCESS
```

运行状态：

```text
2.11 后端：http://127.0.0.1:18848
PID：35568
健康检查：{"status":"UP"}
```

数据库结构验证：

```text
dev_device_instance：LSX 字段无缺失
dev_device_card：已创建
```

## 下一步

1. 让测试设备真实上报到 2.11，验证字段值从 MQTT/协议链路落库。
2. 迁移 2.11 前端设备详情展示。
3. 迁移 REWEB：先做前端入口和 function 调用，再处理 FRP/域名依赖。
4. 后续再拆客户设备、任务、告警和物联网卡管理模块。
