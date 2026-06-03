# 老 jar 私有功能迁移清单

日期：2026-06-03

## 目的

这份清单用于回答一个核心问题：线上老 `lsx-device.jar` 到底改了什么，哪些改动已经在 2.1.1 本地环境中复现，迁到 2.11 时应该怎么拆。

状态说明：

- `已确认`：旧 jar、源码或线上环境有证据。
- `已跑通`：本地 2.1.1 已用模拟或真实设备验证。
- `待迁移`：应迁到 2.11。
- `待确认`：证据不足，不能作为已完成需求。
- `不直接迁移`：只属于本地调试或临时适配。

## 总体判断

老 jar 的核心私有能力不是单个 Controller，而是一组组合改造：

1. 老版本 JetLinks 2.1.x 基线。
2. MySQL + Redis + Elasticsearch 运行形态。
3. MQTT_SERVER 网络组件 + mqtt-server-gateway 网关 + jar 类型协议包。
4. 客户、客户设备、设备任务等业务 Controller。
5. 设备详情扩展字段和属性上报同步。
6. REWEB 平台侧字段、功能调用和外部 FRP 基础设施配合。
7. 恢复过的前端页面和旧编译前端路由。

2.11 迁移不能按“复制旧代码”处理，应该按模块逐个重新接入和验证。

## 功能清单

| 模块 | 状态 | 旧环境证据 | 2.1.1 本地证据 | 2.11 迁移动作 | 验收方式 |
| --- | --- | --- | --- | --- | --- |
| `lsx-device` 启动模块 | 已确认，待迁移 | 旧 jar `artifactId=lsx-device`，线上以 `--spring.profiles.active=wj` 启动 | `lsx-device` 模块可打包运行 | 在 2.11 中决定是保留独立启动模块，还是合入 2.11 原启动模块 | 2.11 后端能以独立 profile 启动，健康检查通过 |
| MySQL/Redis/ES 运行形态 | 已确认，待映射 | 线上老 jar 使用 MySQL 5.7、Redis、ES | 本地 MySQL `device`、Redis DB 1、ES 7.17.25 跑通 | 做 MySQL 到 2.11 数据库模型映射，不直接全库导入 | 最小设备、产品、协议、网关数据能在 2.11 运行 |
| MQTT 设备接入 | 已跑通，优先迁移 | 线上 `agent.wugee.net.cn:1883`，`network_config` + `device_gateway` + `dev_protocol` | 真实设备 `869624060285951` 已连接本地 `1883` 并上报 | 对齐 2.11 的网络组件、设备网关、协议管理、运行时注册流程 | 真实设备连 2.11 MQTT 端口，状态 online，能上报属性 |
| jar 类型协议包 | 已跑通，兼容性待确认 | 线上协议文件 `452f67679610c2197e97f6d678a93038.jar` | 本地协议缓存大小 `102512` bytes，协议 ID `1816752822044901376` 可加载 | 先验证旧协议 jar 是否兼容 2.11 的 `ProtocolSupport` API；不兼容时再适配协议代码 | `GET /protocol/supports` 能看到协议，设备认证和解码通过 |
| MQTT 认证格式 | 已跑通，待迁移 | 旧协议/网关使用 secureId/secureKey | 本地验证 `username=<secureId>|<timestamp>`，`password=md5(username|secureKey)` | 在 2.11 设备接入链路中保持同样认证规则，或封装兼容适配层 | 模拟客户端和真实设备都能 CONNACK success |
| 客户模块 | 已确认，待迁移 | `CustomerController` | 前后端接口已对齐 | 按 2.11 权限、用户、组织模型重接 Controller/Service | 客户新增、更新、详情、查询 API 可用 |
| 客户设备模块 | 已确认，待迁移 | `CustomerDeviceController`、客户设备导入导出、定位、状态同步 | 前后端接口已对齐 | 迁 Controller、请求对象、Excel 导入导出、客户设备查询条件 | 客户设备列表、导入、导出、批量更新、定位、状态同步可用 |
| 设备详情扩展字段 | 已确认，优先迁移 | 旧 `DeviceDetail` 暴露品牌、型号、REWEB、SIM、Ping 等字段 | 本地真实设备详情和上报同步已验证部分字段 | 在 2.11 的设备详情 DTO 或扩展响应中补齐字段 | 前端设备详情能看到扩展字段，真实上报后字段变化 |
| 属性上报同步 | 已跑通，优先迁移 | `DeviceMessageBusinessHandler` 同步 `webpwd`、`switch_state`、`ping_addr`、SIM 卡状态等 | 真实设备上报后 MySQL 字段 `rsrp/rsrq/sinr/network/ping` 更新 | 将同步逻辑按 2.11 消息处理链路重写或挂载 | 上报 `REPORT_PROPERTY` 后 DB/ES/前端状态同步 |
| SIM 卡/设备卡片 | 已确认，待迁移 | `DeviceCardEntity`、设备详情 `cards` | 本地清理后保留该设备 `dev_device_card` 相关记录 | 映射 2.11 表结构，确认是否仍用设备卡片实体 | 设备详情能返回 SIM 卡列表，属性上报能更新卡状态 |
| REWEB 平台侧 | 已确认，待迁移 | 旧 jar 和旧前端支持 `reweb` 功能调用，字段 `subDomain/passwd/port` | 文档已拆解平台流程；FRP 在线上确认存在 | 迁字段、前端调用、功能调用入口；FRP 仍作为外部依赖 | 前端调用 `function/reweb` 后设备收到指令，打开 `*.reweb.wugee.net.cn` |
| FRP/REWEB 基础设施 | 已确认，外部依赖 | 线上 `frps`：7000/7001/7002/7003，Nginx 转发 `*.reweb.wugee.net.cn` | 本地未完整复现公网 FRP，只验证平台侧 | 不迁到 Java 平台；保留运维配置和设备侧依赖说明 | 线上或测试 FRP 能路由到设备 Web |
| 设备任务模块 | 已确认，待迁移 | `DeviceJobController`、`dev_device_job` | 前后端接口已对齐 | 迁任务实体、Controller、执行日志查询、启停逻辑 | 任务创建、更新、启用、禁用、删除、执行记录查询可用 |
| 场景解析扩展 | 已确认，待迁移 | `SceneController` 有变量和条件列解析接口 | 前后端接口已对齐 | 对比 2.11 规则引擎 API，移植兼容接口 | 前端规则/场景页面不报接口缺失 |
| 告警扩展 | 已确认，待迁移 | 告警配置、记录、历史、规则绑定接口存在 | 前后端接口已对齐 | 对比 2.11 告警模块，迁缺失接口和字段 | 告警页面可查询、配置、处理 |
| 前端客户设备页面 | 已确认，待迁移或重写 | 旧编译前端有路由和接口调用 | `jetlinks-ui-vue-2.1.1` 已恢复源码 | 不建议直接复制到 2.11；按 2.11 前端架构重接 API 和页面 | 2.11 前端客户设备页面可用 |
| 前端 REWEB 页面逻辑 | 已确认，待迁移或重写 | 旧编译前端读取 `subDomain` 并调用 `function/reweb` | 已恢复前端行为一致 | 按 2.11 设备详情页结构重新接入 | 设备详情页能触发 REWEB |
| 物联网卡完整模块 `/network/card/**` | 待确认，不作为已完成迁移 | 旧编译前端有页面和 API 调用 | 后端源码和旧 jar 文件列表未找到匹配 Controller | 先继续找后端来源；不要盲迁为已确认模块 | 找到 Controller/jar/外部服务证据后再进入迁移 |
| 本地 MQTT NPE 修复 | 不直接迁移，按需评估 | 线上 Linux 不一定触发 | Windows 本地 `connection.getClientAddress()` 为空时需要 | 如果 2.11 本地开发也触发，再按 2.11 代码修 | Windows 本地 MQTT 不因空地址崩溃 |
| `LocalMqttAuthDebugController` | 不直接迁移 | 旧 jar 无此生产接口 | 本地调试设备 deploy 和协议 reload 使用 | 只可作为开发诊断工具，不能直接进入生产 | 迁移阶段可临时启用，正式包必须移除或加保护 |

## 数据迁移分层

迁移数据时按优先级分层，不要一次性迁全库：

### 第一层：接入链配置

目标是让 2.11 能启动 MQTT 网关并加载协议：

- `network_config`
- `device_gateway`
- `dev_protocol`
- `s_file`
- 协议 jar 文件
- 产品 `dev_product`
- 测试设备 `dev_device_instance`

### 第二层：业务主数据

目标是让页面和设备业务能运行：

- 客户用户和客户信息
- 客户设备关系
- 设备卡片
- 设备任务
- 规则、场景、告警配置
- 菜单和权限

### 第三层：运行态和历史数据

最后再处理，因为体量大、模型差异也最大：

- Elasticsearch 设备日志
- 属性历史
- 会话指标
- 告警历史
- 大体量上报数据

## 2.11 迁移验证顺序

1. 2.11 原生后端启动成功。
2. 2.11 独立数据库和中间件跑通。
3. 协议 jar 能加载。
4. MQTT 网关能监听新端口。
5. 测试设备能认证成功。
6. 测试设备状态变 online。
7. 测试设备属性上报能进入 2.11 的 DB/ES。
8. 设备详情能返回 LSX 扩展字段。
9. 客户设备页面能查询这台设备。
10. REWEB 功能调用能发到设备。
11. 设备任务能创建并查询执行日志。
12. 告警和场景页面不缺接口。

## 当前最小验收设备

迁移早期只用一台设备做闭环：

- 设备 ID/IMEI：`869624060285951`
- 产品：`device_test01`
- MAC：`D0:A0:D6:8C:B4:C8`
- 已知上报字段：
  - `iccid`
  - `mac`
  - `imei`
  - `ip`
  - `version`
  - `t24g_num`
  - `t5g_num`
  - `rsrp`
  - `rsrq`
  - `sinr`
  - `network`
  - `slot`
  - `gpsloc`
  - `webpwd`
  - `operator`
  - `switch_state`
  - `ping_addr`
  - `ping_retry`

这台设备是迁移验收锚点。任何 2.11 迁移动作，先证明这台设备能闭环，再考虑导入更多数据。
