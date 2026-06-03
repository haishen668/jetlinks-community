# 前后端恢复校验记录

日期：2026-06-03

## 范围

本文记录 LSX JetLinks 2.1.1 恢复工作中，前端源码、旧编译前端、后端源码和旧后端 jar 的接口对齐情况。

已校验对象：

- 后端：`F:\project\other\jetlinks\jetlinks-community`，分支 `lsx-device-2.1.1`
- 前端：`F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1`，分支 `lsx-ui-2.1.1-recovery`
- 旧编译前端证据：`F:\project\other\jetlinks\html\assets`
- 旧后端 jar 证据：`C:\Users\Administrator\Downloads\lsx-device.jar`

## 总结

客户设备、客户、设备任务、REWEB 相关路径已经在已恢复前端和后端之间对齐。

旧编译前端和已恢复前端源码中都存在物联网卡页面，但当前后端源码和旧 `lsx-device.jar` 文件列表中没有找到匹配的 `/network/card/**` 或 `/network/flow/**` 后端 Controller。因此物联网卡模块目前应视为“前端存在、后端证据不足”的未完成模块，而不是已确认完整支持。

## 已匹配接口

| 模块 | 前端路径 | 后端证据 | 状态 |
| --- | --- | --- | --- |
| 客户设备查询 | `POST /customer/device/_query` | `CustomerDeviceController @PostMapping("/_query")` | 已匹配 |
| 客户设备位置 | `POST /customer/device/queryPosition` | `CustomerDeviceController @PostMapping("/queryPosition")` | 已匹配 |
| 客户设备当前位置 | `GET /customer/device/getLocation` | `CustomerDeviceController @GetMapping("/getLocation")` | 已匹配 |
| 客户设备状态同步 | `GET /customer/device/syncState` | `CustomerDeviceController @GetMapping("/syncState")` | 已匹配 |
| 客户设备批量激活 | `GET /customer/device/deployAll` | `CustomerDeviceController @GetMapping("/deployAll")` | 已匹配 |
| 客户设备更新 | `POST /customer/device/_update` | `CustomerDeviceController @PostMapping("/_update")` | 已匹配 |
| 客户设备新增 | `PATCH /customer/device/_add` | `CustomerDeviceController @PatchMapping("/_add")` | 已匹配 |
| 客户设备批量更新 | `PATCH /customer/device/batchUpdate` | `CustomerDeviceController @PatchMapping("/batchUpdate")` | 已匹配 |
| 客户设备数量 | `POST /customer/device/_count` | `CustomerDeviceController @PostMapping("/_count")` | 已匹配 |
| 客户设备导入 | `GET /customer/device/{productId}/import` | `CustomerDeviceController @GetMapping("/{productId}/import")` | 已匹配 |
| 客户设备模板 | `GET /customer/device/{productId}/template.{format}` | `CustomerDeviceController @GetMapping("/{productId}/template.{format}")` | 已匹配 |
| 客户设备导出 | `GET /customer/device/export.{format}` | `CustomerDeviceController @GetMapping("/export.{format}")` | 已匹配 |
| 设备功能执行 | `POST /device/instance/{deviceId}/function/{functionId}` | `DeviceInstanceController @PostMapping("/{deviceId:.+}/function/{functionId}")` | 已匹配 |
| 客户创建 | `POST /customer/_create` | `CustomerController @PostMapping("/_create")` | 已匹配 |
| 客户更新 | `PUT /customer/{userId}/_update` | `CustomerController @PutMapping("/{userId}/_update")` | 已匹配 |
| 客户详情 | `GET /customer/{userId}` | `CustomerController @GetMapping("/{userId}")` | 已匹配 |
| 客户查询 | `POST /customer/_query` | `CustomerController @PostMapping("/_query")` | 已匹配 |
| 客户无分页查询 | `POST /customer/no-paging/_query` | `CustomerController @PostMapping("/no-paging/_query")` | 已匹配 |
| 当前客户详情 | `GET /customer` | `CustomerController @GetMapping` | 已匹配 |
| 当前客户保存 | `PUT /customer` | `CustomerController @PutMapping` | 已匹配 |
| 设备任务查询 | `POST /deviceJob/{deviceId}/_query` | `DeviceJobController @PostMapping("/{deviceId}/_query")` | 已匹配 |
| 设备任务创建 | `POST /deviceJob` | `DeviceJobController @PostMapping` | 已匹配 |
| 设备任务更新 | `PUT /deviceJob/{id}` | `DeviceJobController @PutMapping("/{id}")` | 已匹配 |
| 设备任务禁用 | `PUT /deviceJob/{id}/_disable` | `DeviceJobController @PutMapping("/{id}/_disable")` | 已匹配 |
| 设备任务启用 | `PUT /deviceJob/{id}/_enable` | `DeviceJobController @PutMapping("/{id}/_enable")` | 已匹配 |
| 设备任务删除 | `DELETE /deviceJob/{id}` | `DeviceJobController @DeleteMapping("/{id}")` | 已匹配 |
| 设备任务执行日志 | `POST /deviceJob/{id}/execute/_query` | `DeviceJobController @PostMapping("/{id}/execute/_query")` | 已匹配 |
| 场景变量解析 | `POST /scene/parse-variables` | `SceneController @PostMapping("/parse-variables")` | 已匹配 |
| 场景条件列解析 | `POST /scene/parse-term-column` | `SceneController @PostMapping("/parse-term-column")` | 已匹配 |
| 告警默认级别 | `GET/PATCH /alarm/config/default/level` | `AlarmConfigController` 中存在对应映射 | 已匹配 |
| 告警目标类型支持 | `GET /alarm/config/target-type/supports` | `AlarmConfigController @GetMapping("/target-type/supports")` | 已匹配 |
| 告警记录 | `/alarm/record/**` | `AlarmRecordController @RequestMapping("/alarm/record")` | 已匹配 |
| 告警历史 | `/alarm/history/**` | `AlarmHistoryController @RequestMapping("/alarm/history")` | 已匹配 |
| 告警规则绑定 | `/alarm/rule/bind/**` | `AlarmRuleBindController @RequestMapping("/alarm/rule/bind")` | 已匹配 |

## 客户设备字段对齐

已恢复的客户设备详情页读取这些自定义字段：

- `brand`
- `model`
- `operator`
- `cards[].iccid`
- `switchState`
- `syncFlag`
- `pingAddr`
- `pingRetry`
- `subDomain`

后端 `DeviceDetail` 已暴露对应字段：

- `brand`
- `model`
- `port`
- `subDomain`
- `passwd`
- `operator`
- `switchState`
- `syncFlag`
- `pingAddr`
- `pingRetry`
- `cards`

后端 `DeviceDetail.with(DeviceInstanceEntity)` 会填充运行时值，并使用 `md5(mac)` 计算 `subDomain`。

后端 `LocalDeviceInstanceService` 会通过 `queryDeviceCardsGroup(...)` 和 `withCards(...)` 将 SIM 卡列表挂到 `DeviceDetail`。

后端 `DeviceMessageBusinessHandler` 会将设备属性上报同步到这些字段：

- `webpwd` -> `passwd`
- `switch_state` -> `switchState`
- `ping_addr` -> `pingAddr`
- `ping_retry` -> `pingRetry`
- SIM 卡属性 -> `DeviceCardEntity`

## REWEB 流程

已恢复前端与旧编译前端行为一致：

1. 读取 `current.subDomain`
2. 将该值设置为 `reweb` 功能的第一个入参
3. 调用 `POST /device/instance/{deviceId}/function/reweb`
4. 调用成功后打开 `http://{subDomain}.reweb.wugee.net.cn`

后端通过 `DeviceInstanceController.invokedFunction(...)` 和 `LocalDeviceInstanceService.invokeFunction(...)` 承接该请求。

目前没有发现平台侧 FRP Controller。REWEB 隧道建立仍属于外部或设备侧依赖，详见 `reweb-backend-recovery-summary.md`。

## 未匹配或未完成区域

### 物联网卡模块

旧编译前端包含 `cardManagement.1753375679050.js` 和 `platform.1753375679050.js`，其中存在这些接口调用：

- `POST /network/card/_query`
- `GET /network/card/{id}/_activation`
- `GET /network/card/{id}/_deactivate`
- `GET /network/card/{id}/_resumption`
- `DELETE /network/card/{id}`
- `GET /network/card/state/_sync`
- `POST /network/card/unbounded/device/_query`
- `GET /network/card/{cardId}/{deviceId}/_bind`
- `POST /network/card/download.{format}/_query`
- `GET /network/card/template.{format}`
- `POST /network/card/recharge/_log`
- `POST /network/card/_recharge`
- `POST /network/card/platform/_query`
- `GET /network/card/platform/{id}`
- `POST /network/card/platform`
- `PATCH /network/card/platform`
- `DELETE /network/card/platform/{id}`

已恢复前端源码中也存在这些调用，位置在 `src/api/iot-card`。

当前后端校验结果：

- 在 `jetlinks-community` 中没有找到 `/network/card/**` 或 `/network/flow/**` Controller。
- 在旧 `lsx-device.jar` 文件列表中没有找到匹配的 `NetworkCard`、`CardController`、`FlowController` 或类似业务类。
- 只找到 `DeviceCardEntity`，用于客户设备详情和属性上报同步链路。

结论：物联网卡页面在前端存在，但当前恢复出的后端没有匹配证据。它应被视为缺失后端模块，或者来自另一个 jar、另一个仓库、数据库侧集成的功能。

## 验证命令

后端使用 IDEA JDK 8：

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-1.8.0_412'
& 'C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.3-bin\6actqn1ngkbj8g7k704ro02jj7\apache-maven-3.9.3\bin\mvn.cmd' -q -pl lsx-device -am -DskipTests clean package
```

前端：

```powershell
pnpm.cmd run build
```

2026-06-03 最近一次验证结果：

- 后端打包通过，退出码为 `0`
- 后端打包产物：`F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar`
- 当时记录的 jar 大小：`216313393`
- 当时记录的 jar 时间：`2026-06-03 12:35:59`
- 前端构建通过，退出码为 `0`
- 前端输出目录：`F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1\dist`
- 当时记录的 `dist/index.html` 时间：`2026-06-03 12:36:28`
- 前端构建仍有 Metadata 组件相关的 Rollup 循环 re-export 警告，但没有导致构建失败

注意：后端 jar 后续又因为本地 MQTT 联调重新打包过，最新 MQTT 状态见 `local-mqtt-connection-status-20260603.md`。
