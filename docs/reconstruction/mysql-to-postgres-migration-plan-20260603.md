# MySQL 到 PostgreSQL/TimescaleDB 数据迁移方案

日期：2026-06-03

## 目标

把线上老 `lsx-device.jar` 对应的 JetLinks 2.1.1 MySQL 业务数据，按模块迁移到当前 2.11 分支使用的 PostgreSQL/TimescaleDB 数据库。

这不是全库原样导入。2.1.1 到 2.11 的表结构、字段、模块边界和部分存储方式差异很大，必须用“表清点 -> 字段映射 -> 分批 ETL -> 校验”的方式迁移。

## 当前边界

源库：

- JetLinks 2.1.1 老 jar 对应的 MySQL。
- 本地开发可用小皮 MySQL 做试迁移。
- 线上 MySQL 体积很大，之前已确认不适合直接备份到本机。

目标库：

- JetLinks 2.11 分支 `lsx-migration-2.11`。
- PostgreSQL/TimescaleDB。
- 当前开发库：`jetlinks_211_migration`。

不改动：

- 老 `lsx-device.jar`。
- 老线上服务。
- 老 MySQL 原始数据。

## 数据分类

### 必须迁移

这些数据决定平台是否能继续管理设备和业务模块，应优先迁移：

| 类别 | 典型表/对象 | 迁移说明 |
| --- | --- | --- |
| 系统基础数据 | 用户、角色、组织、菜单、权限、字典、系统配置 | 先对比 2.11 原生权限模型，避免覆盖 2.11 初始化菜单和权限。 |
| 产品和协议 | `dev_product`、`dev_protocol`、协议 jar 文件、产品物模型 | 先迁产品，再迁设备。协议 jar 需要单独验证 2.11 API 兼容性。 |
| 接入链配置 | `network_config`、`device_gateway` | 保证 MQTT 网关、网络组件、协议绑定可以恢复。 |
| 设备主数据 | `dev_device_instance` | 需要映射 LSX 扩展字段，包括 `mac`、`imei`、`firmware_version`、`rsrp`、`rsrq`、`sinr`、`network` 等。 |
| 当前 SIM 卡 | `dev_device_card` | 2.11 已补目标表。迁当前状态即可，历史流量记录默认不迁。 |
| 客户和客户设备 | 客户表、客户设备关系表、导入导出相关表 | 后续迁客户设备模块时再精确映射。 |
| 设备任务 | `dev_device_job` 等 | 迁定义和当前状态；执行日志默认跳过。 |
| 规则和告警配置 | 场景、规则、告警规则、通知模板 | 迁配置，不迁历史告警流水。 |
| REWEB 所需字段 | `subDomain` 依赖的 `mac`、`passwd`、`port` 等 | `subDomain` 在 2.11 里由 `mac` 计算；`passwd/port` 需要保留。 |

### 默认跳过

用户已确认日志、告警记录和大体量上报数据可以跳过。下面数据默认不进入第一轮迁移：

| 类别 | 处理方式 |
| --- | --- |
| 设备属性历史、事件历史、消息明细 | 跳过，必要时单独归档到冷库。 |
| 上报流水、运行日志、调试日志 | 跳过。 |
| 告警历史、告警处理记录 | 跳过，但告警规则配置要保留。 |
| 操作日志、登录日志、审计日志 | 跳过。 |
| 任务执行日志 | 跳过，只迁任务定义。 |
| Elasticsearch 历史索引 | 第一轮不迁。 |
| TimescaleDB 超表历史数据 | 第一轮不迁。 |

常见跳过表名规则：

```text
*_log
*_logs
*_history
*_record
*_records
*_detail
*_details
*_message
*_messages
*_event
*_events
*_property
*_properties
*_metric
*_metrics
*_trace
*_debug
alarm_record*
warning_record*
notify_history*
```

注意：这里跳过的是“历史记录/流水”。告警规则、通知模板、场景规则这类功能配置不能因为名字里有 alarm 就直接跳过。

## 迁移阶段

### 第 0 阶段：冻结和备份

1. 选择迁移窗口，停止会写入源库的业务操作。
2. 线上 MySQL 在服务器本地做 `mysqldump` 或物理备份。
3. 目标 PostgreSQL 先做快照：

```powershell
pg_dump -h 49.234.53.230 -p 5433 -U postgres -d jetlinks_211_migration -Fc -f jetlinks_211_migration_before_lsx_restore.dump
```

4. 保留目标 2.11 应用自动初始化出来的表结构，不从 MySQL 直接导 DDL。

### 第 1 阶段：表清点和映射

源 MySQL 表清点：

```sql
select
  table_name,
  table_rows,
  round((data_length + index_length) / 1024 / 1024, 2) as size_mb
from information_schema.tables
where table_schema = database()
order by data_length + index_length desc;
```

源 MySQL 字段清点：

```sql
select
  table_name,
  column_name,
  column_type,
  is_nullable,
  column_default
from information_schema.columns
where table_schema = database()
order by table_name, ordinal_position;
```

目标 PostgreSQL 表清点：

```sql
select
  schemaname,
  relname as table_name,
  n_live_tup as estimated_rows
from pg_stat_user_tables
order by relname;
```

目标 PostgreSQL 字段清点：

```sql
select
  table_name,
  column_name,
  data_type,
  is_nullable,
  column_default
from information_schema.columns
where table_schema = 'public'
order by table_name, ordinal_position;
```

输出物：

- `old_mysql_tables.csv`
- `old_mysql_columns.csv`
- `pg_tables.csv`
- `pg_columns.csv`
- `table-mapping-20260603.md`

### 第 2 阶段：迁系统和接入链配置

优先迁：

1. 用户、组织、角色、权限关系。
2. 字典和系统配置。
3. 产品、物模型、协议支持。
4. 网络组件和设备网关。

校验重点：

- 2.11 可以登录。
- 产品可见。
- 协议支持可加载。
- MQTT 网关配置存在但不一定立即启用。

### 第 3 阶段：迁设备主数据和 LSX 字段

当前 2.11 已补齐 `dev_device_instance` 的 LSX 字段，设备主数据可以按字段映射导入。

关键字段：

```text
id
name
product_id
product_name
state
registry_time
creator_id
create_time
modifier_id
modify_time
mac
imei
firmware_version
model
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

注意：

- 老库如果字段叫 `version`，目标库映射为 `firmware_version`。
- 老库如果字段叫 `webpwd`，目标库映射为 `passwd`。
- 老库如果只有 `gpsloc`，需要解析成 `lat/lng/location`，不能直接塞一个字符串。
- `subDomain` 不入库，由 2.11 `DeviceDetail` 按 `mac` 动态计算。

### 第 4 阶段：迁当前 SIM 卡

目标表为 `dev_device_card`。第一轮只迁当前卡状态：

```text
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

不要迁大体量流量历史，除非后续业务明确需要。

### 第 5 阶段：迁客户、任务、REWEB、规则配置

这些模块依赖后续代码迁移进度，应按模块独立验收：

1. 客户模块。
2. 客户设备模块。
3. 设备任务模块。
4. REWEB 调用入口。
5. 场景、告警规则、通知配置。

每迁一个模块，都要补：

- 表映射。
- API 冒烟测试。
- 前端页面可用性验证。
- 可回滚说明。

## 脚本目录建议

迁移脚本不要直接放到生产代码路径里执行。建议放在本地工作目录：

```powershell
mkdir F:\project\other\jetlinks\runtime\migration-tools
cd F:\project\other\jetlinks\runtime\migration-tools
npm init -y
npm i mysql2 pg dotenv
```

本仓库已提供可复制的脚本样板：

```text
docs/reconstruction/migration-scripts/.env.example
docs/reconstruction/migration-scripts/inventory.mjs
docs/reconstruction/migration-scripts/migrate-device-master-data.mjs
docs/reconstruction/migration-scripts/README.md
```

推荐先把这些脚本复制到 `F:\project\other\jetlinks\runtime\migration-tools`，再编辑 `.env` 执行。这样脚本运行产物和本地密码不会进入 Git。

`.env` 示例：

```dotenv
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=device
MYSQL_USER=root
MYSQL_PASSWORD=your_mysql_password

PG_HOST=49.234.53.230
PG_PORT=5433
PG_DATABASE=jetlinks_211_migration
PG_USER=postgres
PG_PASSWORD=your_pg_password

BATCH_SIZE=500
DRY_RUN=true
```

## Node.js ETL 示例

文件名：`migrate-device-master-data.mjs`

```js
import 'dotenv/config';
import mysql from 'mysql2/promise';
import pg from 'pg';

const batchSize = Number(process.env.BATCH_SIZE || 500);
const dryRun = String(process.env.DRY_RUN || 'true') === 'true';

const mysqlPool = mysql.createPool({
  host: process.env.MYSQL_HOST,
  port: Number(process.env.MYSQL_PORT || 3306),
  database: process.env.MYSQL_DATABASE,
  user: process.env.MYSQL_USER,
  password: process.env.MYSQL_PASSWORD,
  waitForConnections: true,
  connectionLimit: 4,
});

const pgPool = new pg.Pool({
  host: process.env.PG_HOST,
  port: Number(process.env.PG_PORT || 5432),
  database: process.env.PG_DATABASE,
  user: process.env.PG_USER,
  password: process.env.PG_PASSWORD,
  max: 4,
});

function normalizeTime(value) {
  return value || null;
}

function normalizeString(value) {
  if (value === undefined || value === null || value === '') return null;
  return String(value);
}

function mapDevice(row) {
  return {
    id: row.id,
    name: row.name,
    product_id: row.product_id,
    product_name: row.product_name,
    state: row.state,
    registry_time: normalizeTime(row.registry_time),
    creator_id: row.creator_id,
    create_time: normalizeTime(row.create_time),
    modifier_id: row.modifier_id,
    modify_time: normalizeTime(row.modify_time),
    mac: normalizeString(row.mac),
    imei: normalizeString(row.imei),
    firmware_version: normalizeString(row.firmware_version ?? row.version),
    model: normalizeString(row.model),
    adress: normalizeString(row.adress),
    t24g_num: normalizeString(row.t24g_num),
    t5g_num: normalizeString(row.t5g_num),
    rsrp: normalizeString(row.rsrp),
    rsrq: normalizeString(row.rsrq),
    sinr: normalizeString(row.sinr),
    network: normalizeString(row.network),
    lat: normalizeString(row.lat),
    lng: normalizeString(row.lng),
    location: normalizeString(row.location ?? row.gpsloc),
    passwd: normalizeString(row.passwd ?? row.webpwd),
    operator: normalizeString(row.operator),
    switch_state: normalizeString(row.switch_state),
    sync_flag: normalizeString(row.sync_flag),
    ping_addr: normalizeString(row.ping_addr),
    ping_retry: normalizeString(row.ping_retry),
    online_time: normalizeTime(row.online_time),
    offline_time: normalizeTime(row.offline_time),
  };
}

async function migrateDevices() {
  let offset = 0;
  let total = 0;

  while (true) {
    const [rows] = await mysqlPool.query(
      `
      select *
      from dev_device_instance
      order by id
      limit ? offset ?
      `,
      [batchSize, offset],
    );

    if (!rows.length) break;

    const devices = rows.map(mapDevice);
    total += devices.length;

    if (dryRun) {
      console.log(`[dry-run] dev_device_instance batch offset=${offset}, rows=${devices.length}`);
      offset += batchSize;
      continue;
    }

    const client = await pgPool.connect();
    try {
      await client.query('begin');

      for (const device of devices) {
        await client.query(
          `
          insert into dev_device_instance (
            id, name, product_id, product_name, state, registry_time,
            creator_id, create_time, modifier_id, modify_time,
            mac, imei, firmware_version, model, adress,
            t24g_num, t5g_num, rsrp, rsrq, sinr, network,
            lat, lng, location, passwd, operator, switch_state,
            sync_flag, ping_addr, ping_retry, online_time, offline_time
          )
          values (
            $1, $2, $3, $4, $5, $6,
            $7, $8, $9, $10,
            $11, $12, $13, $14, $15,
            $16, $17, $18, $19, $20, $21,
            $22, $23, $24, $25, $26, $27,
            $28, $29, $30, $31, $32
          )
          on conflict (id) do update set
            name = excluded.name,
            product_id = excluded.product_id,
            product_name = excluded.product_name,
            state = excluded.state,
            registry_time = excluded.registry_time,
            modifier_id = excluded.modifier_id,
            modify_time = excluded.modify_time,
            mac = excluded.mac,
            imei = excluded.imei,
            firmware_version = excluded.firmware_version,
            model = excluded.model,
            adress = excluded.adress,
            t24g_num = excluded.t24g_num,
            t5g_num = excluded.t5g_num,
            rsrp = excluded.rsrp,
            rsrq = excluded.rsrq,
            sinr = excluded.sinr,
            network = excluded.network,
            lat = excluded.lat,
            lng = excluded.lng,
            location = excluded.location,
            passwd = excluded.passwd,
            operator = excluded.operator,
            switch_state = excluded.switch_state,
            sync_flag = excluded.sync_flag,
            ping_addr = excluded.ping_addr,
            ping_retry = excluded.ping_retry,
            online_time = excluded.online_time,
            offline_time = excluded.offline_time
          `,
          [
            device.id, device.name, device.product_id, device.product_name, device.state, device.registry_time,
            device.creator_id, device.create_time, device.modifier_id, device.modify_time,
            device.mac, device.imei, device.firmware_version, device.model, device.adress,
            device.t24g_num, device.t5g_num, device.rsrp, device.rsrq, device.sinr, device.network,
            device.lat, device.lng, device.location, device.passwd, device.operator, device.switch_state,
            device.sync_flag, device.ping_addr, device.ping_retry, device.online_time, device.offline_time,
          ],
        );
      }

      await client.query('commit');
    } catch (error) {
      await client.query('rollback');
      throw error;
    } finally {
      client.release();
    }

    console.log(`migrated dev_device_instance offset=${offset}, rows=${devices.length}`);
    offset += batchSize;
  }

  console.log(`done dev_device_instance total=${total}, dryRun=${dryRun}`);
}

try {
  await migrateDevices();
} finally {
  await mysqlPool.end();
  await pgPool.end();
}
```

运行：

```powershell
node migrate-device-master-data.mjs
```

先 dry-run：

```powershell
$env:DRY_RUN='true'
node migrate-device-master-data.mjs
```

确认后执行：

```powershell
$env:DRY_RUN='false'
node migrate-device-master-data.mjs
```

## SIM 卡迁移示例

```sql
insert into dev_device_card (
  id,
  device_id,
  iccid,
  operator,
  msisdn,
  imsi,
  slot,
  use_state,
  status,
  used_flow,
  create_time,
  modify_time
)
values (...)
on conflict (device_id, iccid) do update set
  operator = excluded.operator,
  msisdn = excluded.msisdn,
  imsi = excluded.imsi,
  slot = excluded.slot,
  use_state = excluded.use_state,
  status = excluded.status,
  used_flow = excluded.used_flow,
  modify_time = excluded.modify_time;
```

如果目标库还没有 `(device_id, iccid)` 唯一约束，先用 `id` 做 upsert，或者在确认业务规则后补唯一索引。

## 断点表

建议目标库增加一张迁移断点表，避免大批量迁移中断后重头开始：

```sql
create table if not exists migration_checkpoint (
  task_id varchar(128) primary key,
  source_table varchar(128) not null,
  target_table varchar(128) not null,
  last_key varchar(256),
  migrated_count bigint not null default 0,
  status varchar(32) not null,
  message text,
  create_time timestamp,
  modify_time timestamp
);
```

## 校验清单

每一批迁移后都要留下校验结果。

数量校验：

```sql
-- MySQL
select count(*) from dev_device_instance;

-- PostgreSQL
select count(*) from dev_device_instance;
```

关键设备校验：

```sql
select
  id,
  name,
  product_id,
  state,
  mac,
  imei,
  firmware_version,
  rsrp,
  rsrq,
  sinr,
  network,
  passwd,
  ping_addr,
  ping_retry
from dev_device_instance
where id = '869624060285951';
```

SIM 卡校验：

```sql
select *
from dev_device_card
where device_id = '869624060285951';
```

孤儿数据校验：

```sql
select c.*
from dev_device_card c
left join dev_device_instance d on d.id = c.device_id
where d.id is null;
```

API 校验：

```powershell
Invoke-RestMethod -Uri http://127.0.0.1:8848/actuator/health
```

页面校验：

- 2.11 前端能登录。
- 设备列表能看到迁入设备。
- 设备详情能看到 `LSX 设备信息`。
- 当前 SIM 卡能显示。
- 不要求第一轮验证真实设备上报到 2.11。

## 回滚方案

目标库回滚：

```powershell
pg_restore -h 49.234.53.230 -p 5433 -U postgres -d jetlinks_211_migration --clean --if-exists jetlinks_211_migration_before_lsx_restore.dump
```

单模块回滚：

1. 每个迁移脚本只操作一组表。
2. 每次执行前记录目标表原始数量。
3. 新迁入数据保留 `create_time/modify_time` 或批次标识。
4. 必要时按本批设备 ID、客户 ID、任务 ID 删除。

## 下一步执行顺序

1. 从老 MySQL 导出表清点和字段清点。
2. 从 2.11 PostgreSQL 导出字段清点。
3. 写 `table-mapping-20260603.md`，先只覆盖必须迁移表。
4. 先跑 `dev_product`、`dev_protocol`、`network_config`、`device_gateway` 的 dry-run。
5. 再跑 `dev_device_instance` 和 `dev_device_card` 的 dry-run。
6. 用设备 `869624060285951` 做详情页和 SQL 校验。
7. 确认后再进入客户设备、任务、REWEB、规则配置模块。
