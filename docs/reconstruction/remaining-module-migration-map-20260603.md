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
| 客户 | `CustomerController @RequestMapping("/customer")` | `/customer/**` | 待迁移 |
| 客户设备 | `CustomerDeviceController @RequestMapping("/customer/device")` | `/customer/device/**` | 待迁移 |
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

当前 2.11 设备主数据脚本还没有写入 `user_id`，这是下一轮必须补的点。

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

1. 在 2.11 `DeviceInstanceEntity` 补 `userId`，数据库字段为 `user_id`。
2. 从 2.1.1 迁 `CustomerController`，优先保留查询、创建、更新、当前客户详情。
3. 从 2.1.1 迁 `CustomerDeviceController`，优先保留查询、详情、统计、批量更新。
4. 从 2.1.1 迁 `DeviceJob`、`DeviceJobController`、`DeviceJobService`，先验证 2.11 规则引擎 API 差异。
5. 补单元测试或 slice 测试，至少覆盖客户设备查询条件和设备任务实体序列化。

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
