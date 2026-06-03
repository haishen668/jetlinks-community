# LSX JetLinks 恢复与迁移文档入口

日期：2026-06-03

## 一句话结论

当前 2.1.1 本地环境不是最终目标，而是用来复现线上老 `lsx-device.jar` 行为的基准环境。真正目标是把老 jar 中的私有改动和业务功能完整识别出来，再按 2.11 的架构重新迁移。

## 为什么先做 2.1.1

线上老 jar 已经证明设备能接入、能上报、REWEB 相关业务能工作。直接迁到 2.11 曾遇到这些问题：

- 数据库体系不一致：老 jar 使用 MySQL，2.11 环境最初走 PostgreSQL/TimescaleDB。
- 版本跨度大：老 jar 基于 JetLinks 2.1.x 私有改造，公开源码没有完全一致的 2.1.1 标签。
- 功能差距大：客户设备、REWEB、设备任务、属性同步、前端页面和数据库表都有私有改造。
- 证据不足：如果直接在 2.11 上改，很容易不知道问题来自迁移错误、版本差异，还是旧 jar 本来就有的私有逻辑。

所以当前策略是：

1. 先在 2.1.1 上复原老 jar 行为。
2. 用真实设备验证关键链路。
3. 把老 jar 的修改拆成可迁移模块。
4. 再逐模块迁到 2.11。

## 当前可运行基准

后端：

- 仓库：`F:\project\other\jetlinks\jetlinks-community`
- 分支：`lsx-device-2.1.1`
- 启动模块：`lsx-device`
- Profile：`wj`
- HTTP：`8848`
- MQTT：`1883`
- JDK：`C:\Users\Administrator\.jdks\corretto-1.8.0_412`

前端：

- 仓库：`F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1`
- 分支：`lsx-ui-2.1.1-recovery`
- 访问：`http://127.0.0.1:5173/index.html`

本地数据库：

- MySQL 库：`device`
- 当前已经清成最小测试库，只保留设备 `869624060285951`
- 清理前备份：`F:\project\other\jetlinks\runtime\db-backups\before-delete-devices-20260603-171037.sql`

真实测试设备：

- ID/IMEI：`869624060285951`
- 产品：`device_test01`
- MAC：`D0:A0:D6:8C:B4:C8`
- MQTT 本地接入已跑通
- 属性上报已进入日志、MySQL 和 Elasticsearch

## 必读顺序

1. `docs/reconstruction/2.11-migration-prep.md`
   - 迁移前置清单、环境冻结要求、迁移路线。

2. `docs/reconstruction/old-jar-feature-inventory.md`
   - 老 jar 私有功能清单，说明哪些功能已确认、如何迁到 2.11。

3. `docs/reconstruction/2.11-migration-worktrees-20260603.md`
   - 已创建的 2.11 后端/前端迁移 worktree、分支、tag、JDK/Node 基线。

4. `docs/reconstruction/local-mqtt-connection-status-20260603.md`
   - 本地 MQTT 和真实设备跑通证据、数据库清理记录、协议 jar 状态。

5. `docs/reconstruction/frontend-backend-verification.md`
   - 前后端接口对齐清单，尤其是客户设备、设备任务、REWEB 和物联网卡缺口。

6. `docs/reconstruction/frontend-2.11-migration-plan.md`
   - 前端迁到 2.11 的策略。注意：前端要迁移，但当前本地 Vite 代理和生成文件不等于业务迁移成果。

7. `docs/reconstruction/reweb-backend-recovery-summary.md`
   - REWEB 平台侧流程和 FRP 边界。

8. `docs/reconstruction/lsx-device-status.md`
   - 2.1.1 源码恢复总状态、旧 jar 指纹、公开源码基线判断。

9. `F:\project\other\jetlinks\docs\online-env-notes.md`
   - 线上环境、MySQL、Nginx、FRP、协议链路事实。

## 文档分类

### 事实源

这些文档用于回答“当前确认了什么”：

- `lsx-device-status.md`
- `local-mqtt-connection-status-20260603.md`
- `frontend-backend-verification.md`
- `frontend-2.11-migration-plan.md`
- `2.11-migration-worktrees-20260603.md`
- `reweb-backend-recovery-summary.md`
- `F:\project\other\jetlinks\docs\online-env-notes.md`

### 操作记录

这些文档用于回答“当时怎么跑通的”：

- `local-mqtt-reproduction-plan.md`
- `online-db-sync-inventory.md`
- `lsx-device-local-env.md`

### 证据和差异分析

这些文档用于回答“老 jar 和源码差在哪”：

- `lsx-device-diff-notes.md`
- `old-jar-feature-inventory.md`
- `jetlinks-ui-vue-2.1.1/docs/reconstruction/lsx-ui-2.1.1-recovery-plan.md`
- `jetlinks-ui-vue-2.1.1/docs/reconstruction/reweb-frontend-recovery-summary.md`

### 历史摘要

- `F:\project\other\jetlinks\docs\session-summary.md`

注意：`session-summary.md` 是更早一次会话针对 2.11 环境的压缩摘要，其中 PostgreSQL、端口、前端 9100 等内容不能直接覆盖当前 2.1.1 可运行基准。它可以作为历史线索，但不是当前事实源。

## 当前应保护的东西

不要破坏这些内容：

- 线上老 jar：`C:\Users\Administrator\Downloads\lsx-device.jar` 和线上 `/mnt/device/lsx-device.jar`
- 2.1.1 可运行后端分支：`lsx-device-2.1.1`
- 2.1.1 恢复前端分支：`lsx-ui-2.1.1-recovery`
- 本地最小 MySQL 测试库和清理前备份
- 协议 jar：
  - `data/files/20250307/452f67679610c2197e97f6d678a93038.jar`
  - `data/protocols/1816752822044901376_452f67679610c2197e97f6d678a93038.jar`
- 当前真实设备验证证据

## 当前不要盲目迁移的东西

- 本地调试接口 `LocalMqttAuthDebugController`
- 本地 Vite `allowedHosts` 和代理地址。它们只是本机联调配置，不代表前端业务功能；前端业务页面和 API wrapper 仍然必须迁到 2.11。
- 当前只保留一台设备的本地测试库状态
- 物联网卡完整模块 `/network/card/**`
- 临时 PID、临时日志、临时端口状态

## 迁移工作原则

迁移到 2.11 时，每个功能都要满足“三段证据”：

1. 老 jar 或线上环境证明它存在。
2. 2.1.1 本地源码能解释它的实现方式。
3. 2.11 上有独立验证方式证明它迁移成功。

如果只有前端页面，没有后端 Controller 或运行时证据，只能标记为“待确认”，不能当作已完成模块。

## 下一步

1. 提交或 tag 当前 2.1.1 后端和前端状态。
2. 重新导出当前最小 MySQL 库。
3. 新建 2.11 独立迁移分支和独立数据库。
4. 先跑通 2.11 原生环境。
5. 从设备详情字段和属性上报同步开始迁移。
6. 再迁客户设备、REWEB、设备任务、告警场景。
