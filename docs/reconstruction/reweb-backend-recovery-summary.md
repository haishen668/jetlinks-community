# REWEB Backend Recovery Summary

Date: 2026-06-03

## Scope

This document records the backend recovery for the LSX / JetLinks 2.1.1 custom REWEB path.

The old production jar was used only as a read-only reference:

- `C:\Users\Administrator\Downloads\lsx-device.jar`
- extracted working copy: `F:\project\other\jetlinks\.codex_tmp\lsx-device-jar`

## Evidence From Old Jar

The old `device-manager-2.1.1.jar` contains REWEB-related behavior in these classes:

- `DeviceDetail`
  - exposes `port`, `subDomain`, `passwd`, `operator`, `switchState`, `syncFlag`, `pingAddr`, `pingRetry`, `cards`
  - computes `subDomain` from `md5(mac)`
  - accepts `withCards(...)` and `withPort(...)`
- `LocalDeviceInstanceService`
  - queries `DeviceCardEntity` grouped by device
  - attaches cards to `DeviceDetail`
  - calls `getPort(deviceId)` and attaches the port to the detail response
- `DeviceMessageBusinessHandler`
  - subscribes to device property reports
  - stores reported `webpwd` as `passwd`
  - parses `gpsloc` and resolves AMap location
  - synchronizes `switch_state`, `ping_addr`, `ping_retry`, `syncFlag`
  - upserts the active SIM card and marks other cards inactive

No standalone FRP controller or FRP service was found in the recovered backend source. The recovered REWEB path is the old design where device metadata function `reweb` is invoked through the standard function endpoint, and the frontend opens `http://{subDomain}.reweb.wugee.net.cn`.

## Recovered Backend Changes

Files changed:

- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/response/DeviceDetail.java`
  - restored LSX customer-device detail fields needed by the old frontend
  - restored card list and REWEB port response fields
  - restored `subDomain = md5(mac)` behavior
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/service/LocalDeviceInstanceService.java`
  - restored device card grouping
  - restored `withCards(...)` and `withPort(...)` detail assembly
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/service/DeviceMessageBusinessHandler.java`
  - restored property-report synchronization for REWEB password, GPS location, ping config, switch-card sync state, and SIM card state
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/entity/DeviceProductEntity.java`
  - restored `brand` field used by customer device detail

## Verification

Commands run with IDEA JDK 8:

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-1.8.0_412'
& 'C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.3-bin\6actqn1ngkbj8g7k704ro02jj7\apache-maven-3.9.3\bin\mvn.cmd' -q -pl jetlinks-manager\device-manager -am -DskipTests compile
& 'C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.3-bin\6actqn1ngkbj8g7k704ro02jj7\apache-maven-3.9.3\bin\mvn.cmd' -q -pl lsx-device -am -DskipTests clean package
```

Result:

- backend compile passed
- backend package passed
- packaged jar: `F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar`

## Remaining Runtime Dependency

The backend now restores the old response and message synchronization path. Actual remote-web connectivity still depends on the external REWEB/FRP infrastructure behind:

```text
*.reweb.wugee.net.cn
```

That external infrastructure is not present in this source tree.
