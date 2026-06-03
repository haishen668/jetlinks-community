# Local MQTT Connection Status - 2026-06-03

## Result

Local JetLinks 2.1.1 can accept the simulated MQTT device connection.

Verified target:

- Backend profile: `wj`
- HTTP port: `8848`
- MQTT port: `1883`
- Device ID: `869624060052013`
- Product ID: `device_test01`
- Protocol ID: `1816752822044901376`

Verification evidence:

- `GET /protocol/supports` returned protocol `1816752822044901376`.
- Product deploy returned success.
- Device deploy returned success.
- Local auth debug returned `secureIdMatches=true` and `passwordMatches=true`.
- Node MQTT client returned `{"label":"connect","sessionPresent":false,"returnCode":0}`.
- While the test client stayed connected, device detail showed `state.value=online` and address `/127.0.0.1:<port>`.

## What Was Fixed Locally

1. MQTT gateway Windows/null address NPE

   File:

   - `jetlinks-components/network-component/mqtt-component/src/main/java/org/jetlinks/community/network/mqtt/gateway/device/MqttServerDeviceGateway.java`

   Change:

   - Replaced direct `connection.getClientAddress().toString()` with `String.valueOf(connection.getClientAddress())`.

2. Local-only MQTT auth/protocol/device deploy debug controller

   File:

   - `lsx-device/src/main/java/org/jetlinks/community/standalone/web/LocalMqttAuthDebugController.java`

   Scope:

   - Active only under Spring profile `wj`.
   - Adds a masked MQTT auth diagnostic endpoint.
   - Adds a local protocol reload endpoint.
   - Adds a local device deploy endpoint for imported devices that exist in MySQL but have not been loaded into the runtime registry.

   Note:

   - `Values.getValue(String)` returns a JetLinks `Value` wrapper. The debug endpoint must use `Value::asString`; using `String::valueOf` prints the wrapper object and produces misleading output.

3. Missing/bad local protocol jar cache

   Correct source jar copied from the online server to:

   - `data/files/20250307/452f67679610c2197e97f6d678a93038.jar`

   Correct runtime cache jar:

   - `data/protocols/1816752822044901376_452f67679610c2197e97f6d678a93038.jar`

   Expected size:

   - `102512` bytes

   Previous bad local file:

   - `data/files/20250307/452f67679610c2197e97f6d678a93038.jar.bad-12bytes-20260603`

## Current Build

Built jar:

- `lsx-device/target/lsx-device.jar`

Build command:

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-1.8.0_412'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -pl lsx-device -am -DskipTests package
```

Build result:

- `BUILD SUCCESS`
- Finished at `2026-06-03T15:42:55+08:00`

## Local Restart Steps

Start backend:

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-1.8.0_412'
$java="$env:JAVA_HOME\bin\java.exe"
$out='F:\project\other\jetlinks\runtime\logs\lsx-device-local.out.log'
$err='F:\project\other\jetlinks\runtime\logs\lsx-device-local.err.log'
$args=@('-jar','lsx-device\target\lsx-device.jar','--spring.profiles.active=wj')
Start-Process -FilePath $java -ArgumentList $args -WorkingDirectory 'F:\project\other\jetlinks\jetlinks-community' -RedirectStandardOutput $out -RedirectStandardError $err -WindowStyle Hidden
```

Check health:

```powershell
Invoke-WebRequest -UseBasicParsing 'http://127.0.0.1:8848/actuator/health'
Get-NetTCPConnection -LocalPort 1883,8848 -State Listen
```

If protocol support is empty, ensure the cache jar exists and is `102512` bytes, then call the local debug reload endpoint:

```powershell
Invoke-WebRequest -UseBasicParsing -Method POST 'http://127.0.0.1:8848/_debug/mqtt-auth/reload-protocol?id=1816752822044901376'
Invoke-WebRequest -UseBasicParsing 'http://127.0.0.1:8848/protocol/supports' -Headers @{ 'X-Access-Token'='<token>'; Accept='application/json' }
```

Deploy product and device through the normal authenticated API:

```powershell
Invoke-WebRequest -UseBasicParsing -Method POST 'http://127.0.0.1:8848/device-product/device_test01/deploy' -Headers @{ 'X-Access-Token'='<token>'; Accept='application/json' }
Invoke-WebRequest -UseBasicParsing -Method POST 'http://127.0.0.1:8848/device-instance/869624060052013/deploy' -Headers @{ 'X-Access-Token'='<token>'; Accept='application/json' }
```

For local development only, imported devices can also be loaded into the runtime registry through the `wj` debug endpoint:

```powershell
Invoke-WebRequest -UseBasicParsing -Method POST 'http://127.0.0.1:8848/_debug/mqtt-auth/deploy-device?id=869624060285951'
Invoke-WebRequest -UseBasicParsing 'http://127.0.0.1:8848/_debug/mqtt-auth?deviceId=869624060285951'
```

## Real Device Verification: 869624060285951

Date:

- `2026-06-03 16:57 +08:00`

Problem observed before local deploy:

- Device reached local MQTT `172.16.21.65:1883`.
- TCP connections from the device were only `TimeWait`.
- Log repeatedly showed `mqtt client [869624060285951] disconnected`.
- `GET /_debug/mqtt-auth?deviceId=869624060285951` returned `found=false`.
- MySQL had the row in `dev_device_instance`, but the runtime registry had not loaded it.

Fix applied locally:

```powershell
Invoke-WebRequest -UseBasicParsing -Method POST 'http://127.0.0.1:8848/_debug/mqtt-auth/deploy-device?id=869624060285951'
```

Deploy result:

```json
{"id":"869624060285951","total":1,"success":true}
```

Verification after deploy:

- `GET /_debug/mqtt-auth?deviceId=869624060285951` returned `hasSecureId=true`, `hasProductId=true`, `hasProtocol=true`.
- TCP showed an established connection from `192.168.110.218` to local `172.16.21.65:1883`.
- MySQL `dev_device_instance.state` changed to `online`.
- Business log recorded a real property report:

```text
device property report: {"iccid":"89861123242042925346","mac":"D0:A0:D6:8C:B4:C8","imei":"869624060285951","ip":"192.168.110.218","version":"11.0.0.183(H72SP1C00)","t24g_num":0,"t5g_num":0,"rsrp":-83,"rsrq":-5,"sinr":30,"network":"5G","slot":0,"gpsloc":"5CC300","webpwd":"testpaddword","operator":"","switch_state":0,"ping_addr":"127.0.0.1\n","ping_retry":5}
```

Elasticsearch evidence:

- `device_log_device_test01_2026-6`: `type=online`
- `device_log_device_test01_2026-6`: `type=reportProperty`
- `properties_device_test01_2026-6`: property snapshot document
- `device_session_metric_2026-6`: session metric document

## Local MySQL Device Cleanup

Date:

- `2026-06-03 17:10 +08:00`

Goal:

- Keep only the real local test device `869624060285951` in the local MySQL `device` database.

Backup before deletion:

- `F:\project\other\jetlinks\runtime\db-backups\before-delete-devices-20260603-171037.sql`
- Backup size: about `4.55 MB`

Tables backed up:

- `dev_device_instance`
- `dev_device_card`
- `dev_device_job`
- `dev_device_tags`
- `dev_metadata_mapping`
- `dev_transparent_codec`
- `thing_property_metric`

Rows before cleanup:

- `dev_device_instance`: `5103`
- `dev_device_card`: `8154`
- `dev_device_job`: `593`
- `dev_device_tags`: `0`
- `dev_metadata_mapping`: `0`
- `dev_transparent_codec`: `0`
- `thing_property_metric`: `0`

Rows after cleanup:

- `dev_device_instance`: `1`
- `dev_device_card`: `1`
- `dev_device_job`: `0`
- `dev_device_tags`: `0`
- `dev_metadata_mapping`: `0`
- `dev_transparent_codec`: `0`
- `thing_property_metric`: `0`

Remaining device:

- ID/IMEI: `869624060285951`
- Name: `manner+城创中心店`
- Product: `device_test01`
- MAC: `D0:A0:D6:8C:B4:C8`

Post-cleanup verification:

- MQTT connection is established from `192.168.110.225` to local `172.16.21.65:1883`.
- MySQL `dev_device_instance.state` is `online`.
- Elasticsearch has fresh `online` and `reportProperty` records after the device reboot.

## MQTT Simulation Shape

MQTT auth format verified locally:

- `clientId`: device ID
- `username`: `<secureId>|<timestamp>`
- `password`: `md5("<username>|<secureKey>")`
- MQTT protocol version: `4`

Successful result:

```json
{"label":"connect","sessionPresent":false,"returnCode":0}
```

## Notes

- The old production jar was not modified.
- The copied protocol jar is evidence from the online runtime and is required for this local reconstruction.
- The local debug endpoints should stay limited to profile `wj` and should not be enabled in production unless intentionally reviewed.
