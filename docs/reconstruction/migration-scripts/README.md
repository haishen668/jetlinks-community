# MySQL 到 PostgreSQL 迁移脚本示例

这些脚本是迁移样板，用来把 JetLinks 2.1.1 老 MySQL 中的关键业务数据迁到 2.11 PostgreSQL/TimescaleDB。

默认原则：

- 只迁业务配置和主数据。
- 默认跳过告警历史、日志、设备上报历史、消息流水等大体量运行数据。
- 脚本默认 `DRY_RUN=true`，不会写目标库。
- 真正执行前必须先做源库备份和目标库快照。

## 准备

建议在仓库外的运行目录安装依赖：

```powershell
mkdir F:\project\other\jetlinks\runtime\migration-tools
cd F:\project\other\jetlinks\runtime\migration-tools
npm init -y
npm i mysql2 pg dotenv
```

复制脚本：

```powershell
copy F:\project\other\jetlinks\worktrees\jetlinks-community-lsx-migration-2.11\docs\reconstruction\migration-scripts\*.mjs .
copy F:\project\other\jetlinks\worktrees\jetlinks-community-lsx-migration-2.11\docs\reconstruction\migration-scripts\.env.example .env
```

编辑 `.env` 后先清点表：

```powershell
node inventory.mjs
```

先 dry-run 设备主数据迁移：

```powershell
$env:DRY_RUN='true'
node migrate-device-master-data.mjs
```

确认映射后再执行：

```powershell
$env:DRY_RUN='false'
node migrate-device-master-data.mjs
```

## 文件说明

- `.env.example`：连接信息模板。
- `inventory.mjs`：导出源 MySQL 和目标 PostgreSQL 表/字段清点，辅助做表映射。
- `migrate-device-master-data.mjs`：示例迁移 `dev_device_instance` 和 `dev_device_card`。
- `migrate-business-modules-skeleton.mjs`：客户、客户设备、设备任务的清点骨架，只检查表、字段、数量和样例，不写目标库。

## 继续扩展的模块

后续迁移客户、客户设备、设备任务、规则、告警配置、REWEB 时，建议按 `migrate-device-master-data.mjs` 的结构复制新脚本：

1. 一个脚本只负责一组相关表。
2. 先 `DRY_RUN=true` 输出数量和样例。
3. 写入时使用事务。
4. 每批完成后写入 `migration_checkpoint`。
5. 每个模块补 SQL 数量校验和 API 校验。

客户、客户设备、设备任务在写入前，先跑：

```powershell
node migrate-business-modules-skeleton.mjs
```

这个脚本只做清点，不会写 PostgreSQL。它会重点检查：

- `s_user_detail.tree_path`
- `dev_device_instance.user_id`
- `dev_device_job` 的任务定义字段
- 目标库是否已经具备对应字段
