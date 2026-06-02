# LSX Device Diff Notes

Date: 2026-06-02

## Validation Log

### Maven Model Validation

Command:

```text
.\mvnw.cmd -pl lsx-device -am -DskipTests validate
```

Environment:

```text
JAVA_HOME=C:\Program Files\Java\jdk-21.0.6
```

Result:

```text
BUILD SUCCESS
```

Notes:

```text
The Maven reactor recognized 37 modules, including org.jetlinks.community:lsx-device:2.1.1.
The command output contained a trailing Windows wrapper noise line, but the Maven result and process exit were successful.
```

### Maven Package

Command:

```text
.\mvnw.cmd -pl lsx-device -am -DskipTests clean package
```

Environment:

```text
JAVA_HOME=C:\Users\Administrator\.jdks\corretto-1.8.0_412
```

Result:

```text
BUILD SUCCESS
```

Artifact:

```text
F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar
```

Notes:

```text
JDK 21 failed against the old Lombok/Javac API with JCTree$JCImport.qualid.
JDK 8 is the correct build environment for this reconstructed jar line.
```

### Source-Level Maven Package

Command:

```text
.\mvnw.cmd -pl lsx-device -am -DskipTests clean package
```

Environment:

```text
JAVA_HOME=C:\Users\Administrator\.jdks\corretto-1.8.0_412
```

Result:

```text
BUILD SUCCESS at 2026-06-02 19:09 CST
```

Scope:

```text
37-module reactor build completed through lsx-device.
authentication-manager, device-manager, notify-manager, rule-engine-manager, and io-component all compiled from source.
```

### Jar Alignment Checks

Commands compared old jar and rebuilt jar:

```text
old: C:\Users\Administrator\Downloads\lsx-device.jar
new: F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar
```

Results:

```text
BOOT-INF/lib old=357 new=357 diff=0
BOOT-INF/classes old=14 new=14 diff=0
org.jetlinks.community.standalone.JetLinksApplication major version=52
```

Production configuration:

```text
application.yml and application-wj.yml were restored from the old jar into lsx-device/src/main/resources.
Their rebuilt BOOT-INF/classes hashes match the old jar resources.
Raw values are intentionally not printed in this document.
```

Nested custom module class-list audit:

```text
authentication-manager-2.1.1.jar old=70  new=72  missing-in-new=0  extra-in-new=2
device-manager-2.1.1.jar        old=210 new=212 missing-in-new=0  extra-in-new=2
rule-engine-manager-2.1.1.jar   old=128 new=151 missing-in-new=0  extra-in-new=23
notify-manager-2.1.1.jar        old=43  new=69  missing-in-new=0  extra-in-new=26
io-component-2.1.1.jar          old=32  new=32  missing-in-new=0  extra-in-new=0
```

## Jar vs Source Differences

### Resource Files

Jar behavior:

```text
BOOT-INF/classes/application.yml
BOOT-INF/classes/application-wj.yml
BOOT-INF/classes/logback-spring.xml
BOOT-INF/classes/banner.txt
BOOT-INF/classes/hsweb-starter.js
BOOT-INF/classes/index.html
```

Source behavior:

```text
lsx-device was copied from jetlinks-standalone and initially contained application-embedded.yml and application-local.yml.
```

Action:

```text
Copied the six jar resources into lsx-device/src/main/resources.
Removed application-embedded.yml and application-local.yml from lsx-device because they are not present in the jar.
Initially replaced raw credentials with environment placeholders.
After production-parity requirement was clarified, restored application.yml and application-wj.yml from the old jar.
Raw values are private repository material and should not be copied into public docs or chat.
```

### Source-Level Custom Classes

Jar behavior:

```text
The old jar contains private custom classes and fields that are not present in public JetLinks 2.1 source.
Examples include customer management, customer device/card/position data, REWEB password fields, device job APIs, alarm handle export, notify channel entity, and upload properties.
```

Source behavior:

```text
The current branch now includes these old-jar classes in source and builds them into the new jar.
```

Action:

```text
Added/restored custom classes under authentication-manager, device-manager, rule-engine-manager, notify-manager, and io-component.
Repaired CFR-decompiled generics/default-method issues so the source builds cleanly on JDK 8.
Verified no audited old custom module class is missing from the rebuilt nested jars.
```

### Startup Classes

Class:

```text
org.jetlinks.community.standalone.JetLinksApplication
org.jetlinks.community.standalone.JetLinksApplication$AdminAllAccess
org.jetlinks.community.standalone.authorize.LoginEvent
org.jetlinks.community.standalone.configuration.JetLinksConfiguration
org.jetlinks.community.standalone.configuration.JetLinksProperties
org.jetlinks.community.standalone.web.ApiInfoProperties
org.jetlinks.community.standalone.web.ClusterInfoController
org.jetlinks.community.standalone.web.SystemInfoController
```

Jar behavior:

```text
The jar contains only these standalone boot/config/web classes under BOOT-INF/classes/org/jetlinks/community/standalone.
javap output was written to F:\project\other\jetlinks\.codex_tmp\lsx-javap.
The jar class bytecode exposes the same fields, public methods, and framework annotations as the public 2.1 standalone source.
```

Source behavior:

```text
lsx-device/src/main/java was copied from jetlinks-standalone.
The source has the matching class list and matching method/annotation shape.
```

Action:

```text
No startup Java code changes were made in this pass.
Compile/package verification is still required to prove the copied source builds after the POM and resource changes.
```

Differences will be recorded in this format before code changes:

```text
Class:
Jar behavior:
Source behavior:
Action:
```
