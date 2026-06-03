import 'dotenv/config';
import mysql from 'mysql2/promise';
import pg from 'pg';

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

const modules = [
  {
    id: 'customer',
    description: '客户模块。老版本基于用户体系和 s_user_detail.tree_path。',
    sourceTables: ['s_user', 's_user_detail', 's_user_role', 's_user_setting', 's_dimension', 's_dimension_user'],
    targetTables: ['s_user', 's_user_detail'],
    requiredColumns: {
      s_user_detail: ['id', 'telephone', 'email', 'avatar', 'description', 'tree_path'],
    },
  },
  {
    id: 'customer-device',
    description: '客户设备模块。核心关系是 dev_device_instance.user_id -> s_user_detail.id。',
    sourceTables: ['dev_device_instance', 'dev_device_card', 's_user_detail'],
    targetTables: ['dev_device_instance', 'dev_device_card'],
    requiredColumns: {
      dev_device_instance: ['id', 'user_id', 'mac', 'imei', 'passwd', 'switch_state', 'sync_flag', 'ping_addr', 'ping_retry'],
      dev_device_card: ['id', 'device_id', 'iccid', 'use_state'],
    },
  },
  {
    id: 'device-job',
    description: '设备任务模块。迁任务定义，不迁执行日志。',
    sourceTables: ['dev_device_job'],
    targetTables: ['dev_device_job'],
    requiredColumns: {
      dev_device_job: [
        'id',
        'device_id',
        'trigger',
        'mod',
        'period',
        'period_when',
        'period_from',
        'period_to',
        'period_every',
        'period_unit',
        'once_time',
        'actions',
        'state',
        'description',
      ],
    },
  },
];

const skippedNamePatterns = [
  /_log$/i,
  /_logs$/i,
  /_history$/i,
  /_record$/i,
  /_records$/i,
  /_message$/i,
  /_messages$/i,
  /_event$/i,
  /_events$/i,
  /_property$/i,
  /_properties$/i,
  /^alarm_record/i,
  /^warning_record/i,
  /^notify_history/i,
];

function shouldSkipTable(tableName) {
  return skippedNamePatterns.some((pattern) => pattern.test(tableName));
}

async function mysqlTableExists(tableName) {
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

async function pgTableExists(tableName) {
  const { rows } = await pgPool.query(
    `
    select count(*)::int as total
    from information_schema.tables
    where table_schema = 'public'
      and table_name = $1
    `,
    [tableName],
  );
  return Number(rows[0]?.total || 0) > 0;
}

async function mysqlColumns(tableName) {
  const [rows] = await mysqlPool.query(
    `
    select column_name
    from information_schema.columns
    where table_schema = database()
      and table_name = ?
    order by ordinal_position
    `,
    [tableName],
  );
  return rows.map((row) => row.column_name);
}

async function pgColumns(tableName) {
  const { rows } = await pgPool.query(
    `
    select column_name
    from information_schema.columns
    where table_schema = 'public'
      and table_name = $1
    order by ordinal_position
    `,
    [tableName],
  );
  return rows.map((row) => row.column_name);
}

async function mysqlCount(tableName) {
  const [rows] = await mysqlPool.query(`select count(*) as total from \`${tableName}\``);
  return Number(rows[0]?.total || 0);
}

async function mysqlSample(tableName) {
  const [rows] = await mysqlPool.query(`select * from \`${tableName}\` limit 1`);
  return rows[0] || null;
}

function missingColumns(actual, required) {
  return required.filter((column) => !actual.includes(column));
}

async function inspectModule(moduleConfig) {
  console.log(`\n# ${moduleConfig.id}`);
  console.log(moduleConfig.description);

  for (const tableName of moduleConfig.sourceTables) {
    if (shouldSkipTable(tableName)) {
      console.log(`skip source ${tableName}: matched history/log skip rule`);
      continue;
    }

    const exists = await mysqlTableExists(tableName);
    if (!exists) {
      console.log(`missing source ${tableName}`);
      continue;
    }

    const columns = await mysqlColumns(tableName);
    const required = moduleConfig.requiredColumns[tableName] || [];
    const missing = missingColumns(columns, required);
    const total = await mysqlCount(tableName);
    const sample = await mysqlSample(tableName);

    console.log(`source ${tableName}: rows=${total}, columns=${columns.length}`);
    if (missing.length) {
      console.log(`source ${tableName}: missing required columns ${missing.join(', ')}`);
    }
    if (sample) {
      const sampleKeys = Object.keys(sample).slice(0, 12);
      console.log(`source ${tableName}: sample keys ${sampleKeys.join(', ')}`);
    }
  }

  for (const tableName of moduleConfig.targetTables) {
    const exists = await pgTableExists(tableName);
    if (!exists) {
      console.log(`missing target ${tableName}`);
      continue;
    }

    const columns = await pgColumns(tableName);
    const required = moduleConfig.requiredColumns[tableName] || [];
    const missing = missingColumns(columns, required);

    console.log(`target ${tableName}: columns=${columns.length}`);
    if (missing.length) {
      console.log(`target ${tableName}: missing required columns ${missing.join(', ')}`);
    }
  }
}

try {
  console.log('business module migration skeleton: inspect only, no writes');
  for (const moduleConfig of modules) {
    await inspectModule(moduleConfig);
  }
} finally {
  await mysqlPool.end();
  await pgPool.end();
}
