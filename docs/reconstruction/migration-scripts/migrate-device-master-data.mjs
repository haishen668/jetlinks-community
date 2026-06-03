import 'dotenv/config';
import mysql from 'mysql2/promise';
import pg from 'pg';

const batchSize = Number(process.env.BATCH_SIZE || 500);
const dryRun = String(process.env.DRY_RUN || 'true').toLowerCase() !== 'false';

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

function normalizeString(value) {
  if (value === undefined || value === null || value === '') return null;
  return String(value);
}

function normalizeNumber(value) {
  if (value === undefined || value === null || value === '') return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function normalizeTime(value) {
  return value || null;
}

function mapDevice(row) {
  return {
    id: normalizeString(row.id),
    name: normalizeString(row.name),
    product_id: normalizeString(row.product_id),
    product_name: normalizeString(row.product_name),
    state: normalizeString(row.state),
    registry_time: normalizeTime(row.registry_time),
    creator_id: normalizeString(row.creator_id),
    create_time: normalizeTime(row.create_time),
    modifier_id: normalizeString(row.modifier_id),
    modify_time: normalizeTime(row.modify_time),
    user_id: normalizeString(row.user_id ?? row.userId),
    mac: normalizeString(row.mac),
    imei: normalizeString(row.imei),
    firmware_version: normalizeString(row.firmware_version ?? row.version),
    model: normalizeString(row.model),
    adress: normalizeString(row.adress ?? row.address),
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

function mapCard(row) {
  const deviceId = normalizeString(row.device_id ?? row.deviceId);
  const iccid = normalizeString(row.iccid);
  return {
    id: normalizeString(row.id) || `${deviceId || 'unknown'}:${iccid || 'unknown'}`,
    device_id: deviceId,
    iccid,
    operator: normalizeString(row.operator),
    msisdn: normalizeString(row.msisdn),
    imsi: normalizeString(row.imsi),
    slot: normalizeNumber(row.slot),
    use_state: normalizeNumber(row.use_state ?? row.useState),
    status: normalizeString(row.status),
    used_flow: normalizeNumber(row.used_flow ?? row.usedFlow),
    create_time: normalizeTime(row.create_time),
    modify_time: normalizeTime(row.modify_time),
    creator_id: normalizeString(row.creator_id),
    modifier_id: normalizeString(row.modifier_id),
  };
}

async function ensureCheckpointTable(client) {
  await client.query(`
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
    )
  `);
}

async function saveCheckpoint(client, taskId, sourceTable, targetTable, lastKey, count, status, message) {
  await client.query(
    `
    insert into migration_checkpoint (
      task_id, source_table, target_table, last_key, migrated_count,
      status, message, create_time, modify_time
    )
    values ($1, $2, $3, $4, $5, $6, $7, now(), now())
    on conflict (task_id) do update set
      last_key = excluded.last_key,
      migrated_count = migration_checkpoint.migrated_count + excluded.migrated_count,
      status = excluded.status,
      message = excluded.message,
      modify_time = now()
    `,
    [taskId, sourceTable, targetTable, lastKey, count, status, message],
  );
}

async function sourceTableExists(tableName) {
  const [rows] = await mysqlPool.query(
    `
    select count(*) as total
    from information_schema.tables
    where table_schema = database()
      and table_name = ?
    `,
    [tableName],
  );
  return Number(rows[0]?.total || 0) > 0;
}

async function migrateDevices() {
  let offset = 0;
  let total = 0;
  const taskId = 'dev_device_instance';

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

    const devices = rows.map(mapDevice).filter((row) => row.id);
    const lastKey = devices.at(-1)?.id || null;
    total += devices.length;

    if (dryRun) {
      console.log(`[dry-run] ${taskId} offset=${offset}, rows=${devices.length}, lastKey=${lastKey}`);
      offset += batchSize;
      continue;
    }

    const client = await pgPool.connect();
    try {
      await client.query('begin');
      await ensureCheckpointTable(client);

      for (const device of devices) {
        await client.query(
          `
          insert into dev_device_instance (
            id, name, product_id, product_name, state, registry_time,
            creator_id, create_time, modifier_id, modify_time, user_id,
            mac, imei, firmware_version, model, adress,
            t24g_num, t5g_num, rsrp, rsrq, sinr, network,
            lat, lng, location, passwd, operator, switch_state,
            sync_flag, ping_addr, ping_retry, online_time, offline_time
          )
          values (
            $1, $2, $3, $4, $5, $6,
            $7, $8, $9, $10, $11,
            $12, $13, $14, $15, $16,
            $17, $18, $19, $20, $21, $22,
            $23, $24, $25, $26, $27, $28,
            $29, $30, $31, $32, $33
          )
          on conflict (id) do update set
            name = excluded.name,
            product_id = excluded.product_id,
            product_name = excluded.product_name,
            state = excluded.state,
            registry_time = excluded.registry_time,
            modifier_id = excluded.modifier_id,
            modify_time = excluded.modify_time,
            user_id = excluded.user_id,
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
            device.creator_id, device.create_time, device.modifier_id, device.modify_time, device.user_id,
            device.mac, device.imei, device.firmware_version, device.model, device.adress,
            device.t24g_num, device.t5g_num, device.rsrp, device.rsrq, device.sinr, device.network,
            device.lat, device.lng, device.location, device.passwd, device.operator, device.switch_state,
            device.sync_flag, device.ping_addr, device.ping_retry, device.online_time, device.offline_time,
          ],
        );
      }

      await saveCheckpoint(client, taskId, taskId, taskId, lastKey, devices.length, 'SUCCESS', null);
      await client.query('commit');
    } catch (error) {
      await client.query('rollback');
      throw error;
    } finally {
      client.release();
    }

    console.log(`migrated ${taskId} offset=${offset}, rows=${devices.length}, lastKey=${lastKey}`);
    offset += batchSize;
  }

  console.log(`done ${taskId} total=${total}, dryRun=${dryRun}`);
}

async function migrateCards() {
  const tableName = 'dev_device_card';
  if (!(await sourceTableExists(tableName))) {
    console.log(`skip ${tableName}: source table does not exist`);
    return;
  }

  let offset = 0;
  let total = 0;

  while (true) {
    const [rows] = await mysqlPool.query(
      `
      select *
      from dev_device_card
      order by id
      limit ? offset ?
      `,
      [batchSize, offset],
    );

    if (!rows.length) break;

    const cards = rows.map(mapCard).filter((row) => row.device_id && row.iccid);
    const lastKey = cards.at(-1)?.id || null;
    total += cards.length;

    if (dryRun) {
      console.log(`[dry-run] ${tableName} offset=${offset}, rows=${cards.length}, lastKey=${lastKey}`);
      offset += batchSize;
      continue;
    }

    const client = await pgPool.connect();
    try {
      await client.query('begin');
      await ensureCheckpointTable(client);

      for (const card of cards) {
        await client.query(
          `
          insert into dev_device_card (
            id, device_id, iccid, operator, msisdn, imsi, slot,
            use_state, status, used_flow, create_time, modify_time,
            creator_id, modifier_id
          )
          values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
          on conflict (id) do update set
            device_id = excluded.device_id,
            iccid = excluded.iccid,
            operator = excluded.operator,
            msisdn = excluded.msisdn,
            imsi = excluded.imsi,
            slot = excluded.slot,
            use_state = excluded.use_state,
            status = excluded.status,
            used_flow = excluded.used_flow,
            modify_time = excluded.modify_time,
            modifier_id = excluded.modifier_id
          `,
          [
            card.id, card.device_id, card.iccid, card.operator, card.msisdn, card.imsi, card.slot,
            card.use_state, card.status, card.used_flow, card.create_time, card.modify_time,
            card.creator_id, card.modifier_id,
          ],
        );
      }

      await saveCheckpoint(client, tableName, tableName, tableName, lastKey, cards.length, 'SUCCESS', null);
      await client.query('commit');
    } catch (error) {
      await client.query('rollback');
      throw error;
    } finally {
      client.release();
    }

    console.log(`migrated ${tableName} offset=${offset}, rows=${cards.length}, lastKey=${lastKey}`);
    offset += batchSize;
  }

  console.log(`done ${tableName} total=${total}, dryRun=${dryRun}`);
}

try {
  await migrateDevices();
  await migrateCards();
} finally {
  await mysqlPool.end();
  await pgPool.end();
}
