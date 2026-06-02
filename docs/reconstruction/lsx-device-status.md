# LSX Device Reconstruction Status

Date: 2026-06-02

## Goal

Reconstruct a maintainable source project for the existing modified runtime jar:

```text
C:\Users\Administrator\Downloads\lsx-device.jar
```

The goal is second development and modification, not just deploying the old jar.

## Current Repository State

Backend repository:

```text
F:\project\other\jetlinks\jetlinks-community
```

Private remote:

```text
origin  https://github.com/haishen668/jetlinks-community
```

Official upstream added locally:

```text
upstream  https://gitee.com/jetlinks/jetlinks-community.git
```

The official `2.1` branch was fetched and pushed to the private repository as:

```text
origin/2.1
```

The current working branch is:

```text
lsx-device-2.1.1
```

It was created from:

```text
2.1 / 7f69c8f4878799c562cc954835695f18360af180
```

## Preserved Previous Work

Before switching away from the original `2.11` branch, local edits were saved:

```text
stash@{0}: On 2.11: codex-save-2.11-before-switch-to-2.1
```

Those edits were not discarded.

## Jar Fingerprint

The old jar contains this Maven metadata:

```text
groupId=org.jetlinks.community
artifactId=lsx-device
version=2.1.1
parent=org.jetlinks.community:jetlinks-community:2.1.1
```

The jar build timestamp in `pom.properties` is:

```text
Wed May 13 21:24:03 CST 2026
```

Important dependency versions observed inside the jar:

```text
spring-boot                 2.7.11
spring-data-redis           2.7.11
jetlinks-core               1.2.2
jetlinks-supports           1.2.2
hsweb-*                     4.0.17
hsweb-easy-orm-*            4.1.2
r2dbc-mysql                 0.9.1
r2dbc-postgresql            0.9.2.RELEASE
vertx-mqtt                  4.3.8
netty-codec-mqtt            4.1.89.Final
```

The main class bytecode is Java 8:

```text
major version: 52
```

## Public Source Search Result

Public GitHub/Gitee tags include `2.1.0` and `2.2.0`, but no public `2.1.1` tag was found.

The public `2.1` branch is still `2.1.0-SNAPSHOT`, so the jar appears to be a private or internal `2.1.1` build based on the public `2.1` line.

## Current Working Tree Changes

Implementation started before this plan document was requested.

Current changes made so far:

```text
Created branch: lsx-device-2.1.1
Copied jetlinks-standalone to lsx-device
Removed copied lsx-device/target build output
Changed root pom.xml version from 2.1.0-SNAPSHOT to 2.1.1
Added root module: lsx-device
Changed root dependency properties toward jar fingerprint:
  spring.boot.version       2.7.11
  hsweb.framework.version   4.0.17
  easyorm.version           4.1.2
  jetlinks.version          1.2.2
Changed child POM parent versions from 2.1.0-SNAPSHOT to 2.1.1
Replaced lsx-device/pom.xml with the pom.xml extracted from lsx-device.jar
Copied jar resources into lsx-device/src/main/resources
Removed resource files not present in the old jar from lsx-device
Restored jar configuration resources into lsx-device/src/main/resources
Aligned dependency versions until BOOT-INF/lib diff is zero
Restored old jar custom source classes for auth, customer device, notify channel, upload config, alarm/device jobs, and scene action support
Aligned selected old jar member signatures for customer device deployment, device address sync, REWEB-related fields, customer device queries, and scene trigger metadata parsing
```

The reconstruction and source-level alignment commits are intended to be kept on the private `lsx-device-2.1.1` branch.

## Current Verification

Build command:

```text
.\mvnw.cmd -pl lsx-device -am -DskipTests clean package
```

Build environment:

```text
JAVA_HOME=C:\Users\Administrator\.jdks\corretto-1.8.0_412
```

Result:

```text
BUILD SUCCESS
```

Latest verified command time:

```text
2026-06-02 19:29 CST
```

Built artifact:

```text
F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar
```

Latest built artifact SHA-256:

```text
A9CD5C1AAB2BAA80355DE5DFDF7C269800C00A715F196B043B2C22D48E5FE3EA
```

Outer package alignment evidence:

```text
BOOT-INF/lib old=357 new=357 missing=0 extra=0
BOOT-INF/classes files old=14 new=14
JetLinksApplication bytecode major version=52
```

Configuration alignment:

```text
lsx-device/src/main/resources/application.yml matches old jar application.yml by SHA-256.
lsx-device/src/main/resources/application-wj.yml matches old jar application-wj.yml by SHA-256.
The old production-sensitive values are present in source because production deployment will use this branch directly.
Do not print or share the raw values in chat or documentation.
```

Nested module class-list audit:

```text
authentication-manager-2.1.1.jar old=70  new=72  missing-in-new=0  extra-in-new=2
device-manager-2.1.1.jar        old=210 new=212 missing-in-new=0  extra-in-new=2
rule-engine-manager-2.1.1.jar   old=128 new=151 missing-in-new=0  extra-in-new=23
notify-manager-2.1.1.jar        old=43  new=69  missing-in-new=0  extra-in-new=26
io-component-2.1.1.jar          old=32  new=32  missing-in-new=0  extra-in-new=0
```

Meaning:

```text
For the audited custom modules, every class present in the old jar is now present in the rebuilt new jar.
The rebuilt jar still contains some extra upstream classes that are not in the old runtime jar.
```

Selected member-signature audit:

```text
Audit method: javap -private on 27 selected custom classes.
Compared: all fields plus non-private source-visible methods and constructors.
Ignored: compiler-generated lambda$ and access$ bridge methods.
Result: TOTAL missing-members=0 extra-members=8
```

Important classes covered:

```text
CustomerDetail, UserDetail, UserDetailEntity, UserDetailService, TermParseUtil, CustomerController
CustomerDevice, DeviceCardEntity, DeviceInstanceEntity, DevicePosition, DeviceStateInfo, LocalDeviceInstanceService
CustomerDeviceController, CustomerDeviceExcelImporter, CustomerDeviceExcelInfo, CustomerDeviceWrapper, BatchUpdateDeviceRequest
AlarmHandleHistoryInfo, DeviceJob, DeviceJobLog, DeviceTrigger, SceneAction, DeviceJobService, DeviceJobController, AlarmHandleExcelInfo
NotifyChannelEntity, UploadProperties
```

Meaning:

```text
For the selected custom classes audited from the old jar, no old field or non-private method signature is missing in the rebuilt jar.
The remaining extra members are from the rebuilt source retaining additional upstream/compatibility members.
```

## Jar Extraction Work Areas

Temporary analysis files live outside the repository source tree:

```text
F:\project\other\jetlinks\.codex_tmp\lsx-fingerprint
F:\project\other\jetlinks\.codex_tmp\lsx-classes
F:\project\other\jetlinks\.codex_tmp\lsx-device-jars
```

They are analysis/extraction scratch directories, not source code.

## Runtime Configuration Evidence

The old jar includes `application.yml` and `application-wj.yml`.

Important finding: the old jar is configured primarily for MySQL + Redis, not the current failed PostgreSQL `jetlinks` database setup.

Observed old jar profile examples:

```text
application.yml:
  spring.profiles.active: dev
  spring.redis.host: remote Redis host from jar
  spring.r2dbc.url: r2dbc:mysql://124.71.164.4:3306/device_wj?ssl=false&serverZoneId=Asia/Shanghai
  easyorm.default-schema: device_wj
  easyorm.dialect: mysql
  tdengine.enabled: false

application-wj.yml:
  spring.config.activate.on-profile: wj
  spring.application.name: lsx-device
  spring.redis.host: 127.0.0.1
  spring.redis.database: 1
  spring.r2dbc.url: r2dbc:mysql://127.0.0.1:3306/device?ssl=false&serverZoneId=Asia/Shanghai
  spring.r2dbc.username: device
  easyorm.default-schema: device
  easyorm.dialect: mysql
  tdengine.enabled: false
```

Sensitive credentials are present in the jar configs and have now been restored into source resources for production parity.
Treat these files as private repository material and do not publish raw secrets outside the private repo.

## Immediate Next Step

Current branch direction:

```text
Keep the global project version changed to 2.1.1 and rebuild all modules as the reconstructed private version.
```

Remaining work before treating this as fully aligned:

```text
Optionally run a runtime smoke test against the intended MySQL/Redis environment.
Investigate whether extra upstream classes should be excluded only if strict byte-for-byte jar parity becomes required.
```
