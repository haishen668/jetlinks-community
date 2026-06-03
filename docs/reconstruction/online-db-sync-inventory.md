# Online DB Sync Inventory

Updated: 2026-06-03

Purpose: decide which online MySQL tables should be restored to the local development database, and which large historical tables should be skipped or handled separately.

## Result

Online database `device` has 48 tables. Only 3 tables are large enough to matter for local recovery:

| table | rows from information_schema | counted rows | total size | meaning | recommendation |
| --- | ---: | ---: | ---: | --- | --- |
| `notify_history` | 6,471,529 | 8,186,114 | 29,703 MB | notification send history, contains `template`, `context`, `error_stack`, `notify_time` | Skip unless historical notification debugging is required |
| `notify_notifications` | 1,067,108 | 1,177,411 | 1,910.73 MB | user/subscriber notification records, contains message/detail payload | Ask before syncing |
| `alarm_handle_history` | 1,133,568 | 1,169,437 | 201.86 MB | alarm handling history | Ask before syncing |

Everything else is small. The next biggest tables are:

| table | rows | total size |
| --- | ---: | ---: |
| `dev_device_instance` | 4,697 | 5.14 MB |
| `dev_device_card` | 8,117 | 3.55 MB |
| `alarm_record` | 4,610 | 2.28 MB |
| `dev_device_job` | 561 | 0.47 MB |

All remaining tables are below 1 MB each.

## Important Notes

- Device property/report/log time-series data is not mainly in MySQL. Online cleanup scripts target Elasticsearch indices such as:
  - `properties_device_test01_YYYY-M`
  - `device_log_device_test01_YYYY-M`
  - `jetlinks-metrics_YYYY-M`
  - `alarm_metrics_YYYY-M`
  - `rule-engine-execute-log_YYYY-M`
  - `system_logger_YYYY-M`
- Therefore the 31 GB MySQL size is dominated by notification/alarm history, not core device configuration.
- A full scan query on `notify_history` for `MIN/MAX(notify_time)` was too expensive and was killed after it appeared in `SHOW FULL PROCESSLIST`. Avoid full scans on this table.

## Suggested Sync Set

Recommended default: restore all tables except these large historical tables:

```text
notify_history
notify_notifications
alarm_handle_history
```

If more runtime fidelity is needed:

- Include `notify_notifications` if the local frontend must show historical station messages/notifications.
- Include `alarm_handle_history` if the local frontend must show old alarm handling timelines.
- Keep skipping `notify_history` unless there is a concrete need to debug historical notification delivery.

## Tables Safe To Sync By Default

These are small and useful for matching online behavior:

```text
alarm_config
alarm_level
alarm_record
certificate_info
device_gateway
dev_device_card
dev_device_instance
dev_device_job
dev_device_tags
dev_metadata_mapping
dev_product
dev_product_category
dev_protocol
dev_transparent_codec
network_config
notify_channel
notify_config
notify_subscriber_channel
notify_subscriber_provider
notify_subscribers
notify_template
rule_instance
rule_scene
s_alarm_rule_bind
s_autz_setting_info
s_config
s_dictionary
s_dictionary_item
s_dimension
s_dimension_type
s_dimension_user
s_file
s_menu
s_menu_bind
s_object_related
s_object_relation
s_organization
s_permission
s_role
s_system
s_third_party_user_bind
s_user
s_user_detail
s_user_settings
thing_property_metric
```

## 2026-06-03 Local Sync Execution

User decision: skip all three large historical tables for the first local recovery.

Skipped tables:

```text
device.notify_history
device.notify_notifications
device.alarm_handle_history
```

Local backup before import:

```text
F:\project\other\jetlinks\runtime\db-backups\local_device_before_online_sync_20260603_144235.sql.zip
```

Imported dump:

```text
F:\project\other\jetlinks\runtime\db-sync\device_config_skip_history_20260603_1442.sql.gz
sha256 = 097dad8fb906a059c96fce797b6a8049bda9d29ecf3415b250028f7ba92e7279
```

Post-import verification:

| check | result |
| --- | ---: |
| table count | 48 |
| `s_user` | 139 |
| `dev_device_instance` | 5103 |
| `dev_product` | 3 |
| `network_config` | 1 |
| `device_gateway` | 1 |
| `dev_protocol` | 1 |
| `notify_history` | 0 |
| `notify_notifications` | 0 |
| `alarm_handle_history` | 0 |

After the import, the online protocol file rows were adapted for local runtime:

- `s_file.server_node_id` was set to local `lsx-device:8848` for protocol jar files.
- `dev_protocol.configuration` was changed from the online file URL form back to local file-id loading.
- Missing protocol jar path aliases referenced by online `s_file.path` were copied under `jetlinks-community\data\files`.

Current local service status after sync:

| service | status |
| --- | --- |
| Backend HTTP | `http://127.0.0.1:8848/actuator/health` returns `{"status":"UP"}` |
| Frontend proxy | `http://127.0.0.1:5173/api/actuator/health` returns `{"status":"UP"}` |
| MQTT | port `1883` is listening and startup log contains `startup mqtt server [1816751690245521408] on port :1883` |
