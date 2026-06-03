import 'dotenv/config';
import fs from 'node:fs/promises';
import mysql from 'mysql2/promise';
import pg from 'pg';

const outDir = new URL('./inventory-output/', import.meta.url);

const mysqlPool = mysql.createPool({
  host: process.env.MYSQL_HOST,
  port: Number(process.env.MYSQL_PORT || 3306),
  database: process.env.MYSQL_DATABASE,
  user: process.env.MYSQL_USER,
  password: process.env.MYSQL_PASSWORD,
  waitForConnections: true,
  connectionLimit: 2,
});

const pgPool = new pg.Pool({
  host: process.env.PG_HOST,
  port: Number(process.env.PG_PORT || 5432),
  database: process.env.PG_DATABASE,
  user: process.env.PG_USER,
  password: process.env.PG_PASSWORD,
  max: 2,
});

function csvCell(value) {
  if (value === null || value === undefined) return '';
  const text = String(value);
  if (/[",\r\n]/.test(text)) return `"${text.replaceAll('"', '""')}"`;
  return text;
}

async function writeCsv(fileName, rows) {
  const text = rows.map((row) => row.map(csvCell).join(',')).join('\n') + '\n';
  await fs.mkdir(outDir, { recursive: true });
  await fs.writeFile(new URL(fileName, outDir), text, 'utf8');
}

async function mysqlInventory() {
  const [tables] = await mysqlPool.query(`
    select
      table_name,
      table_rows,
      round((data_length + index_length) / 1024 / 1024, 2) as size_mb
    from information_schema.tables
    where table_schema = database()
    order by data_length + index_length desc
  `);

  const [columns] = await mysqlPool.query(`
    select
      table_name,
      column_name,
      column_type,
      is_nullable,
      column_default
    from information_schema.columns
    where table_schema = database()
    order by table_name, ordinal_position
  `);

  await writeCsv('old_mysql_tables.csv', [
    ['table_name', 'table_rows', 'size_mb'],
    ...tables.map((row) => [row.table_name, row.table_rows, row.size_mb]),
  ]);

  await writeCsv('old_mysql_columns.csv', [
    ['table_name', 'column_name', 'column_type', 'is_nullable', 'column_default'],
    ...columns.map((row) => [
      row.table_name,
      row.column_name,
      row.column_type,
      row.is_nullable,
      row.column_default,
    ]),
  ]);
}

async function postgresInventory() {
  const { rows: tables } = await pgPool.query(`
    select
      schemaname,
      relname as table_name,
      n_live_tup as estimated_rows
    from pg_stat_user_tables
    order by relname
  `);

  const { rows: columns } = await pgPool.query(`
    select
      table_name,
      column_name,
      data_type,
      is_nullable,
      column_default
    from information_schema.columns
    where table_schema = 'public'
    order by table_name, ordinal_position
  `);

  await writeCsv('pg_tables.csv', [
    ['schema', 'table_name', 'estimated_rows'],
    ...tables.map((row) => [row.schemaname, row.table_name, row.estimated_rows]),
  ]);

  await writeCsv('pg_columns.csv', [
    ['table_name', 'column_name', 'data_type', 'is_nullable', 'column_default'],
    ...columns.map((row) => [
      row.table_name,
      row.column_name,
      row.data_type,
      row.is_nullable,
      row.column_default,
    ]),
  ]);
}

try {
  await mysqlInventory();
  await postgresInventory();
  console.log(`inventory files written to ${outDir.pathname}`);
} finally {
  await mysqlPool.end();
  await pgPool.end();
}

