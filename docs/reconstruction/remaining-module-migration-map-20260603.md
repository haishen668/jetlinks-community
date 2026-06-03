# 2.11 剩余 LSX 模块迁移地图

日期：2026-06-03

## 背景

当前 2.11 分支已经先补了设备基础画像、当前 SIM 卡、属性上报同步和 REWEB 前端入口。下一步要迁的是老 `lsx-device.jar` 上真正改出来的业务模块：客户、客户设备、设备任务、规则/告警配置。

这份文档的作用是把“功能迁移”和“数据迁移”放在同一张地图上，后续每做一个模块，都能知道需要补哪些代码、迁哪些表、跳过哪些大体量历史数据。

## 已确认证据

来源：

- 旧后端源码：`F:\project\other\jetlinks\jetlinks-community`
- 旧前端恢复源码：`F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1`
- 已恢复接口对齐文档：`F:\project\other\jetlinks\jetlinks-community\docs\reconstruction\frontend-backend-verification.md`
- 2.11 后端工作树：`F:\project\other\jetlinks\worktrees\jetlinks-community-lsx-migration-2.11`
- 2.11 前端工作树：`F:\project\other\jetlinks\worktrees\jetlinks-ui-vue-lsx-ui-migration-2.11`

老 2.1.1 已确认存在：

| 模块 | 老后端证据 | 老前端/API 证据 | 2.11 当前状态 |
| --- | --- | --- | --- |
| 客户 | `CustomerController @RequestMapping("/customer")` | `/customer/**` | 2.11 后端最小接口已迁 |
| 客户设备 | `CustomerDeviceController @RequestMapping("/customer/device")` | `/customer/device/**` | 2.11 后端最小接口已迁，前端页面待迁 |
| 设备任务 | `DeviceJobController @RequestMapping("/deviceJob")`、`DeviceJob @Table("dev_device_job")` | `/deviceJob/**` | 待迁移 |
| REWEB | 标准设备功能接口 `POST /device/instance/{deviceId}/function/reweb` | 设备详情按钮打开 `http://{subDomain}.reweb.wugee.net.cn` | 后端标准接口已具备，前端 2.11 已补最小入口 |
| 物联网卡页面 | 前端存在 `/network/card/**` 调用 | 后端未找到匹配 Controller | 先不迁，除非找到额外后端证据 |

## 关键数据关系

### 客户

老客户模块不是独立业务客户表，而是建立在 JetLinks 用户体系上：

- `s_user`
- `s_user_detail`
- 用户、角色、组织、维度关系表

`CustomerController` 会按当前登录用户的 `tree_path` 查询下级客户，因此迁移时不能只迁 `s_user`，还要保留：

- `s_user_detail.tree_path`
- 用户状态、用户名、姓名、联系方式
- 角色关系
- 组织关系

当前 2.11 已开始补客户模块：

- `CustomerController` 已恢复 `/customer/**` 老接口入口。
- `CustomerDetail` 已恢复，用于客户分页查询响应。
- `UserDetailEntity.treePath` 已恢复，字段为 `s_user_detail.tree_path`。
- `UserDetailService` 已补客户分页查询、无分页查询和保存时的 `treePath` 维护。

仍待验证：

- 用真实 PostgreSQL 数据跑 `/customer/_query`、`/customer/no-paging/_query`。
- 前端 2.11 客户页面还没有迁。
- 权限菜单资源还没有完整对齐。

### 客户设备

老客户设备模块的核心关系是：

```text
dev_device_instance.user_id -> s_user_detail.id
```

旧 `LocalDeviceInstanceService` 查询客户设备时直接 join：

```sql
dev_device_instance t
left join s_user_detail userDetail on userDetail.id = t.user_id
left join dev_device_card deviceCard on deviceCard.device_id = t.id and deviceCard.use_state = 1
```

所以 2.11 后续需要补：

- `DeviceInstanceEntity.userId`
- `DeviceDetail` 或客户设备响应对象里的客户信息
- 客户设备查询服务
- `CustomerDeviceController`
- 前端客户设备列表/详情/导入/导出页面
- 数据迁移脚本中的 `user_id` 字段映射

当前 2.11 已完成后端最小迁移：

- `DeviceInstanceEntity.userId` 映射 `dev_device_instance.user_id`。
- `migrate-device-master-data.mjs` 已把老库 `user_id/userId` 映射到目标 `user_id`。
- `CustomerDevice` 已恢复为客户设备查询响应对象，继承 `DeviceInstanceEntity`，并携带客户信息、当前卡信息和列表序号。
- `DevicePosition` 已恢复为在线设备地图点位统计响应对象。
- `BatchUpdateDeviceRequest` 已恢复，用于客户设备批量修改所属客户、描述和修改人。
- `LocalDeviceInstanceService` 已恢复客户设备分页查询、计数、批量更新、在线点位统计的最小服务方法。
- `CustomerDeviceController` 已恢复 `/customer/device/**` 主要入口：`_query`、`_count`、`_add`、`_update`、`batchUpdate`、`queryPosition`、`getLocation`、`syncState`、`deployAll`、模板下载、导出。
- 已补 `CustomerDeviceModuleContractTest`，用反射锁定老接口路径、请求方法和关键实体契约。

仍待补齐：

- 导入、模板下载、导出的 Excel 细节目前只是保留路由入口，待前端页面和真实数据格式确认后再补完整实现。
- `getLocation` 目前只保留客户端 IP 返回；老版本的百度定位服务在 2.11 中还没有找到等价依赖，待页面真实需要时再补。
- 客户设备页面还没有迁到 2.11 前端。
- 还没有用真实 PostgreSQL 数据冒烟验证 `/customer/device/**`。

### 设备任务

老设备任务实体表：

```text
dev_device_job
```

主要字段：

```text
id
device_id
trigger
mod
period
period_when
period_from
period_to
period_every
period_unit
once_time
actions
state
description
creator_id
create_time
modifier_id
modify_time
start_time
```

迁移原则：

- 迁任务定义和启停状态。
- 不迁任务执行日志。
- `trigger` 和 `actions` 是 JSON 字段，迁移前要抽样确认 2.11 的场景规则模型是否兼容。
- 迁完后要触发或验证规则引擎中的任务实例是否能加载。

### REWEB

REWEB 不需要迁独立表。它依赖设备主数据：

- `mac`：2.11 后端计算 `subDomain` 的来源。
- `passwd`：老设备上报 `webpwd` 映射过来。
- `port`：老接口文档提到过，当前 2.11 还没有确认入库字段；后续如客户设备详情必须展示端口，再补字段。

2.11 前端子模块当前本地提交：

```text
01bfe0a feat: add lsx reweb action
```

外层前端仓库已保存补丁：

```text
docs/reconstruction/patches/0001-feat-add-lsx-reweb-action.patch
```

## 数据迁移顺序

建议顺序：

1. 系统基础数据：用户、角色、组织、权限关系。
2. 产品、协议、接入链配置。
3. 设备主数据和 LSX 字段。
4. 当前 SIM 卡。
5. 客户设备关系字段：重点是 `dev_device_instance.user_id`。
6. 客户设备模块代码和页面。
7. 设备任务定义：`dev_device_job`。
8. 规则、告警、通知配置。

不要提前迁：

- 设备属性历史。
- 事件历史。
- 告警历史。
- 设备任务执行日志。
- 操作日志、登录日志。
- 大体量上报流水。

## 下一轮代码任务

### 后端

1. 用真实 PostgreSQL 数据验证 `/customer/**` 查询和保存接口。
2. 用真实 PostgreSQL 数据验证 `/customer/device/_query`、`/customer/device/_count`、`/customer/device/queryPosition`、`/customer/device/batchUpdate`。
3. 等前端客户设备页面迁移时，按页面实际需要补完整导入、模板下载、导出和 `getLocation` 定位实现。
4. 从 2.1.1 迁 `DeviceJob`、`DeviceJobController`、`DeviceJobService`，先验证 2.11 规则引擎 API 差异。
5. 补设备任务实体序列化和服务契约测试。

### 前端

1. 以 `jetlinks-ui-vue-2.1.1` 的客户、客户设备、设备任务页面为来源，迁到 2.11 对应模块结构。
2. 不直接照搬 2.1.1 构建产物，只把它作为路由/API/交互证据。
3. 每迁一个页面都跑 `corepack pnpm build`。

### 数据脚本

1. 先运行 `inventory.mjs` 导出源/目标表字段。
2. 运行 `migrate-business-modules-skeleton.mjs` 做客户、客户设备、设备任务相关表清点。
3. 确认目标字段后，再把脚本从“只清点”升级为真正 ETL。

## 验收标准

每个模块都必须同时满足：

- 后端编译通过。
- 前端构建通过。
- 数据 dry-run 有数量和样例输出。
- 页面能打开。
- API 冒烟测试通过。
- 文档记录已迁字段、跳过字段和回滚方式。
