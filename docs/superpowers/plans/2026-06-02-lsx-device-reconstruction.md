# LSX Device Reconstruction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reconstruct a maintainable `lsx-device` source module from the existing modified `lsx-device.jar`, based on the public JetLinks `2.1` source line.

**Architecture:** Use public `jetlinks-community` `2.1` as the source skeleton, create a private `2.1.1` reconstruction branch, add `lsx-device` as a standalone boot module, and progressively align Maven metadata, resources, startup classes, and runtime behavior with the old jar. Decompiled jar content is source-of-truth for the custom runtime where public source and jar disagree.

**Tech Stack:** Java 8, Maven multi-module build, Spring Boot 2.7.11, JetLinks community 2.1 line, MySQL R2DBC, Redis, optional Elasticsearch/TDengine disabled for local reconstruction.

---

## Current State

Working repository:

```text
F:\project\other\jetlinks\jetlinks-community
```

Current branch:

```text
lsx-device-2.1.1
```

Status document:

```text
F:\project\other\jetlinks\jetlinks-community\docs\reconstruction\lsx-device-status.md
```

Implementation already started before this plan was requested:

```text
Root pom.xml changed to version 2.1.1
Root pom.xml added module lsx-device
All child POM parent versions changed to 2.1.1
lsx-device directory copied from jetlinks-standalone
lsx-device/pom.xml replaced with jar-extracted pom.xml
```

Before continuing, review the current diff:

```powershell
git -C F:\project\other\jetlinks\jetlinks-community status --short --branch
git -C F:\project\other\jetlinks\jetlinks-community diff --stat
```

Expected result:

```text
Branch is lsx-device-2.1.1
Changes include pom.xml files and new lsx-device module
No unrelated user files are modified
```

## File Structure

Files to create or modify:

```text
pom.xml
  Root Maven project version, dependency fingerprint, and module list.

lsx-device/pom.xml
  Boot module POM extracted from the old jar.

lsx-device/src/main/java/org/jetlinks/community/standalone/**
  Startup and standalone web/config classes. Compare public source against jar bytecode.

lsx-device/src/main/resources/application.yml
lsx-device/src/main/resources/application-wj.yml
lsx-device/src/main/resources/logback-spring.xml
lsx-device/src/main/resources/banner.txt
lsx-device/src/main/resources/hsweb-starter.js
lsx-device/src/main/resources/index.html
  Runtime resources extracted from the jar, with local-development-safe handling for secrets.

docs/reconstruction/lsx-device-status.md
  Current status and evidence record.

docs/reconstruction/lsx-device-diff-notes.md
  Running notes for jar-vs-source differences discovered during reconstruction.
```

## Task 1: Stabilize Current Branch and Documentation

**Files:**

```text
docs/reconstruction/lsx-device-status.md
docs/superpowers/plans/2026-06-02-lsx-device-reconstruction.md
```

- [x] **Step 1: Confirm branch and current diff**

Run:

```powershell
git -C F:\project\other\jetlinks\jetlinks-community status --short --branch
git -C F:\project\other\jetlinks\jetlinks-community diff --stat
```

Expected:

```text
## lsx-device-2.1.1
Only POM/module reconstruction changes and docs are present
```

- [x] **Step 2: Commit only the branch setup and documentation if the user approves**

Run:

```powershell
git -C F:\project\other\jetlinks\jetlinks-community add docs/reconstruction/lsx-device-status.md docs/superpowers/plans/2026-06-02-lsx-device-reconstruction.md
git -C F:\project\other\jetlinks\jetlinks-community commit -m "docs: record lsx device reconstruction plan"
```

Expected:

```text
[lsx-device-2.1.1 <commit>] docs: record lsx device reconstruction plan
```

Actual:

```text
[lsx-device-2.1.1 bbff1db1] docs: record lsx device reconstruction plan
```

## Task 2: Finish Maven Module Skeleton

**Files:**

```text
pom.xml
lsx-device/pom.xml
all child pom.xml files with parent version 2.1.1
```

- [x] **Step 1: Verify Maven coordinates match the jar**

Run:

```powershell
Select-String -Path F:\project\other\jetlinks\jetlinks-community\pom.xml -Pattern "<version>2.1.1</version>|<module>lsx-device</module>|spring.boot.version|hsweb.framework.version|easyorm.version|jetlinks.version"
Select-String -Path F:\project\other\jetlinks\jetlinks-community\lsx-device\pom.xml -Pattern "<artifactId>lsx-device</artifactId>|<version>2.1.1</version>|<version>0.9.1</version>"
```

Expected:

```text
Root version is 2.1.1
Root module list includes lsx-device
lsx-device artifactId is lsx-device
r2dbc-mysql is 0.9.1
```

- [x] **Step 2: Verify every child module points to parent 2.1.1**

Run:

```powershell
rg -n "2\.1\.0-SNAPSHOT" F:\project\other\jetlinks\jetlinks-community -g pom.xml
```

Expected:

```text
No matches
```

- [x] **Step 3: Run Maven model validation**

Run:

```powershell
mvn -f F:\project\other\jetlinks\jetlinks-community\pom.xml -pl lsx-device -am -DskipTests validate
```

Expected:

```text
BUILD SUCCESS
```

If Maven cannot resolve public/private dependencies, record the missing artifact and repository in:

```text
docs/reconstruction/lsx-device-diff-notes.md
```

Actual:

```text
Command: .\mvnw.cmd -pl lsx-device -am -DskipTests validate
Environment: JAVA_HOME=C:\Program Files\Java\jdk-21.0.6
Result: BUILD SUCCESS
Date: 2026-06-02 18:16:43 +08:00
```

## Task 3: Restore Jar Resources Into lsx-device

**Files:**

```text
lsx-device/src/main/resources/application.yml
lsx-device/src/main/resources/application-wj.yml
lsx-device/src/main/resources/logback-spring.xml
lsx-device/src/main/resources/banner.txt
lsx-device/src/main/resources/hsweb-starter.js
lsx-device/src/main/resources/index.html
docs/reconstruction/lsx-device-diff-notes.md
```

- [x] **Step 1: Extract resources from the jar**

Run:

```powershell
$jar = "C:\Users\Administrator\Downloads\lsx-device.jar"
$javaHome = "C:\Users\Administrator\Desktop\开发\Crack\recaf\recaf-4.0.0\corretto-22.0.2\bin"
$out = "F:\project\other\jetlinks\.codex_tmp\lsx-resource-extract"
New-Item -ItemType Directory -Force -Path $out | Out-Null
Push-Location $out
& "$javaHome\jar.exe" xf $jar BOOT-INF/classes/application.yml BOOT-INF/classes/application-wj.yml BOOT-INF/classes/logback-spring.xml BOOT-INF/classes/banner.txt BOOT-INF/classes/hsweb-starter.js BOOT-INF/classes/index.html
Pop-Location
```

Expected:

```text
F:\project\other\jetlinks\.codex_tmp\lsx-resource-extract\BOOT-INF\classes\application.yml
F:\project\other\jetlinks\.codex_tmp\lsx-resource-extract\BOOT-INF\classes\application-wj.yml
```

- [x] **Step 2: Copy resources into lsx-device**

Run:

```powershell
Copy-Item -Force F:\project\other\jetlinks\.codex_tmp\lsx-resource-extract\BOOT-INF\classes\* F:\project\other\jetlinks\jetlinks-community\lsx-device\src\main\resources\
```

Expected:

```text
lsx-device/src/main/resources/application-wj.yml exists
```

- [x] **Step 3: Protect secrets before publishing**

Inspect:

```powershell
Select-String -Path F:\project\other\jetlinks\jetlinks-community\lsx-device\src\main\resources\application*.yml -Pattern "password:|username:|host:|r2dbc:"
```

Expected:

```text
Credentials are identified and documented before pushing to a public-visible branch.
```

If the private GitHub repository is not guaranteed private, replace production secrets with local placeholders before committing.

Actual:

```text
Extracted and copied:
  application.yml
  application-wj.yml
  logback-spring.xml
  banner.txt
  hsweb-starter.js
  index.html

Removed from lsx-device because they are not present in the old jar:
  application-embedded.yml
  application-local.yml

Replaced raw Redis, R2DBC, and TDengine passwords with environment placeholders:
  LSX_REDIS_PASSWORD
  LSX_DB_PASSWORD
  LSX_TDENGINE_PASSWORD

Secret scan:
  Searched lsx-device and reconstruction docs for raw jar credential fragments.
  Result: no raw jar credential fragments remain in reconstruction source/docs.
```

## Task 4: Compare Startup Classes Against Jar

**Files:**

```text
lsx-device/src/main/java/org/jetlinks/community/standalone/JetLinksApplication.java
lsx-device/src/main/java/org/jetlinks/community/standalone/configuration/JetLinksConfiguration.java
lsx-device/src/main/java/org/jetlinks/community/standalone/configuration/JetLinksProperties.java
lsx-device/src/main/java/org/jetlinks/community/standalone/authorize/LoginEvent.java
lsx-device/src/main/java/org/jetlinks/community/standalone/web/ApiInfoProperties.java
lsx-device/src/main/java/org/jetlinks/community/standalone/web/ClusterInfoController.java
lsx-device/src/main/java/org/jetlinks/community/standalone/web/SystemInfoController.java
docs/reconstruction/lsx-device-diff-notes.md
```

- [x] **Step 1: Decompile or inspect jar startup classes**

Use Recaf UI or `javap` from the bundled JDK:

```powershell
$javaHome = "C:\Users\Administrator\Desktop\开发\Crack\recaf\recaf-4.0.0\corretto-22.0.2\bin"
& "$javaHome\javap.exe" -classpath F:\project\other\jetlinks\.codex_tmp\lsx-main-classes\BOOT-INF\classes -p -c org.jetlinks.community.standalone.JetLinksApplication
```

Expected:

```text
Method signatures and important annotations are visible
```

- [x] **Step 2: Compare class list**

Run:

```powershell
rg -n "class JetLinksApplication|class JetLinksConfiguration|class JetLinksProperties|class LoginEvent|class ApiInfoProperties|class ClusterInfoController|class SystemInfoController" F:\project\other\jetlinks\jetlinks-community\lsx-device\src\main\java
```

Expected:

```text
All seven startup classes exist under lsx-device
```

- [x] **Step 3: Record differences before changing code**

Create or update:

```text
docs/reconstruction/lsx-device-diff-notes.md
```

Record each difference in this format:

```text
Class: org.jetlinks.community.standalone.JetLinksApplication
Jar behavior:
Source behavior:
Action:
```

Expected:

```text
Every jar-vs-source difference has an explicit action before code edits.
```

Actual:

```text
Extracted jar boot classes to .codex_tmp\lsx-boot-classes.
Generated javap output in .codex_tmp\lsx-javap.
The jar startup class list matches lsx-device source.
No startup Java code change was required in this pass.
```

## Task 5: Build lsx-device

**Files:**

```text
lsx-device/**
pom.xml
```

- [x] **Step 1: Compile with tests skipped**

Run:

```powershell
mvn -f F:\project\other\jetlinks\jetlinks-community\pom.xml -pl lsx-device -am -DskipTests package
```

Expected:

```text
BUILD SUCCESS
F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar
```

Actual:

```text
Command: .\mvnw.cmd -pl lsx-device -am -DskipTests clean package
Environment: JAVA_HOME=C:\Users\Administrator\.jdks\corretto-1.8.0_412
Result: BUILD SUCCESS
Finished at: 2026-06-02 18:26:15 +08:00
Artifact: F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar
```

- [x] **Step 2: Compare built jar identity**

Run:

```powershell
$javaHome = "C:\Users\Administrator\Desktop\开发\Crack\recaf\recaf-4.0.0\corretto-22.0.2\bin"
& "$javaHome\jar.exe" tf F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar | Select-String "META-INF/maven/org.jetlinks.community/lsx-device/pom.properties|BOOT-INF/classes/application-wj.yml"
```

Expected:

```text
Built jar contains lsx-device Maven metadata
Built jar contains application-wj.yml
```

Actual:

```text
Built jar contains:
  META-INF/maven/org.jetlinks.community/lsx-device/pom.properties
  BOOT-INF/classes/application.yml
  BOOT-INF/classes/application-wj.yml
  BOOT-INF/lib/spring-boot-2.7.11.jar
  BOOT-INF/lib/jetlinks-core-1.2.2.jar
  BOOT-INF/lib/hsweb-core-4.0.17.jar
  BOOT-INF/lib/r2dbc-mysql-0.9.1.jar

Java bytecode:
  org.jetlinks.community.standalone.JetLinksApplication major version 52

Alignment checks:
  BOOT-INF/lib old=357 new=357 diff=0
  BOOT-INF/classes old=22 new=22 diff=0

Intentional difference:
  Raw jar credentials were replaced with environment placeholders in source and built jar.
```

## Task 6: Local Runtime Preparation

**Files:**

```text
docs/reconstruction/lsx-device-local-env.md
```

- [x] **Step 1: Document required middleware**

Create:

```text
docs/reconstruction/lsx-device-local-env.md
```

Include:

```text
Java 8 or Java compatible with target 1.8
Maven
MySQL database: device
MySQL user: device
Redis database: 1
Backend port: 8848
Frontend port: 9000 or 9100 depending on selected UI branch
```

- [ ] **Step 2: Run without starting production connections**

Use a local profile and local credentials only:

```powershell
java -jar F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar --spring.profiles.active=wj
```

Expected:

```text
Application starts far enough to connect to local MySQL and Redis
If database is missing, error message names the missing local database
```

Current status:

```text
Created docs/reconstruction/lsx-device-local-env.md with JDK 8, MySQL, Redis, profile, and LSX_* environment variable instructions.
Did not start the app yet because local MySQL/Redis credentials have not been provided and raw jar credentials were intentionally not committed.
```

## Task 7: Commit and Push Reconstruction Milestones

**Files:**

```text
All reconstruction changes
```

- [ ] **Step 1: Commit Maven skeleton after validation**

Run:

```powershell
git -C F:\project\other\jetlinks\jetlinks-community add pom.xml */pom.xml jetlinks-components/**/*.xml jetlinks-manager/**/*.xml lsx-device docs/reconstruction docs/superpowers/plans
git -C F:\project\other\jetlinks\jetlinks-community commit -m "feat: reconstruct lsx device module skeleton"
```

Expected:

```text
Commit created on lsx-device-2.1.1
```

- [ ] **Step 2: Push branch to private origin**

Run:

```powershell
git -C F:\project\other\jetlinks\jetlinks-community push -u origin lsx-device-2.1.1
```

Expected:

```text
origin/lsx-device-2.1.1 exists
```

## Stop Points

Stop and ask the user before:

```text
Publishing any extracted production credentials
Changing frontend branch or frontend dependencies
Replacing public 2.1 source code with decompiled component code
Deleting or reverting the saved 2.11 stash
Switching database/middleware strategy away from MySQL + Redis
```
