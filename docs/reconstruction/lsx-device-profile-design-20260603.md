# LSX 设备基础画像迁移设计

日期：2026-06-03

## 目标

把老 `lsx-device.jar` 中已经在 2.1.1 本地复现的设备基础画像能力迁到 2.11：设备表能保存 LSX 扩展字段，设备详情接口能返回这些字段，属性上报后能同步设备画像和当前 SIM 卡。

本设计只覆盖第一个迁移切片，不覆盖客户设备列表、REWEB 调用、设备任务、告警和完整物联网卡平台。

## 证据来源

2.1.1 本地真实设备：

```text
deviceId  869624060285951
product   device_test01
name      manner+城创中心店
mac       D0:A0:D6:8C:B4:C8
imei      869624060285951
```

2.1.1 已同步字段：

```text
firmware_version  11.0.0.183(H72SP1C00)
t24g_num          0
t5g_num           0
rsrp              -83
rsrq              -4
sinr              30
network           5G
passwd            testpaddword
operator          CT
switch_state      0
sync_flag         1
ping_addr         127.0.0.1
ping_retry        5
```

2.11 当前状态：

- `dev_device_instance` 只有 2.11 原生字段。
- `DeviceDetail` 只返回原生设备字段。
- 没有 `DeviceCardEntity` 或 `dev_device_card`。
- `DeviceMessageBusinessHandler` 还没有处理 `ReportPropertyMessage` 同步画像字段。

## 设计

### 设备实体扩展

在 2.11 `DeviceInstanceEntity` 中补齐 LSX 字段，使用和 2.1.1 相同的列名，降低后续数据映射成本：

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

说明：

- 先保留 2.1.1 中拼写为 `adress` 的列名，避免后续老库映射时产生额外转换。
- `subDomain` 不落库，由设备详情根据 `mac` 动态计算。
- `brand` 仍属于产品维度，后续产品画像切片再处理。

### 设备卡实体

新增轻量 `DeviceCardEntity`，只覆盖当前设备 SIM 卡状态：

```text
dev_device_card:
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
```

本切片只同步当前上报的 `iccid` 和 `slot`，并把同设备其他卡的 `use_state` 置为 `0`。不实现 `/network/card/**` 平台管理接口。

### 详情响应

在 2.11 `DeviceDetail` 中补齐老前端需要的字段：

```text
firmwareVersion
mac
imei
t24gNum
t5gNum
rsrp
rsrq
sinr
network
model
port
subDomain
lat
lng
location
passwd
operator
switchState
syncFlag
pingAddr
pingRetry
cards
```

`DeviceDetail.with(DeviceInstanceEntity)` 从实体复制字段，并在 `mac` 存在时计算：

```text
subDomain = md5(mac)
```

### 属性上报同步

在 `DeviceMessageBusinessHandler` 订阅 `ReportPropertyMessage`：

```text
/device/*/*/message/property/report
device/*/*/message/property/report
```

同步规则：

- 将属性 JSON 映射到 `DeviceInstanceEntity` 的同名字段。
- `version` 映射为 `firmwareVersion`。
- `webpwd` 映射为 `passwd`。
- `switch_state` 映射为 `switchState`。
- `ping_addr` 映射为 `pingAddr`。
- `ping_retry` 映射为 `pingRetry`。
- `iccid` 长度大于 10 时更新 `dev_device_card`。

不在本切片中发送下行指令，也不建立 REWEB 隧道。

## 验收标准

1. 单元测试能证明 LSX 属性上报会更新设备画像实体和当前卡片实体。
2. `device-manager` 模块能通过测试。
3. 2.11 后端能重新打包。
4. 2.11 独立库启动后 `dev_device_instance` 自动出现 LSX 扩展列，`dev_device_card` 表存在。
5. `/device/instance/{deviceId}/detail` 能返回 LSX 扩展字段。

## 不做范围

- 不迁客户设备 Controller。
- 不迁 REWEB 功能调用。
- 不迁设备任务。
- 不实现完整物联网卡平台。
- 不导入线上大体量历史数据。
