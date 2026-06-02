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

### Jar Alignment Checks

Commands compared old jar and rebuilt jar:

```text
old: C:\Users\Administrator\Downloads\lsx-device.jar
new: F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar
```

Results:

```text
BOOT-INF/lib old=357 new=357 diff=0
BOOT-INF/classes old=22 new=22 diff=0
org.jetlinks.community.standalone.JetLinksApplication major version=52
```

Intentional difference:

```text
Raw jar credentials were replaced with LSX_* environment placeholders before committing source.
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
Replaced raw credentials with environment placeholders before committing source.
Raw extracted jar resources remain only in .codex_tmp for local evidence.
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
