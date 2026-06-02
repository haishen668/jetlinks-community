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
```

These changes have not been committed yet.

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
  spring.redis.host: 124.71.164.4
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

Sensitive credentials are present in the jar configs. Treat extracted config files as local development evidence and avoid publishing secrets.

## Immediate Next Decision

Before continuing implementation, decide whether to:

```text
Option A: Keep the global project version changed to 2.1.1 and rebuild all modules as the reconstructed private version.
Option B: Keep public modules at 2.1.0-SNAPSHOT and make only lsx-device resolve against local/extracted 2.1.1 jars.
```

Recommended option: Option A.

Reason: the runtime jar contains many internal modules at `2.1.1`, so a coherent private branch with all module versions set to `2.1.1` is easier to build, reason about, and modify.
