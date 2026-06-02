# LSX Device Local Development Environment

Date: 2026-06-02

## Build JDK

Use JDK 8 for this reconstructed branch.

Verified local JDK:

```text
C:\Users\Administrator\.jdks\corretto-1.8.0_412
```

PowerShell setup:

```powershell
$env:JAVA_HOME = "C:\Users\Administrator\.jdks\corretto-1.8.0_412"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Verified build command:

```powershell
.\mvnw.cmd -pl lsx-device -am -DskipTests clean package
```

Expected result:

```text
BUILD SUCCESS
lsx-device\target\lsx-device.jar
```

## Runtime Profile

Use the old jar's local-style profile:

```text
wj
```

Run command:

```powershell
java -jar F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar --spring.profiles.active=wj
```

## Middleware Shape

The old jar is aligned to MySQL + Redis.

It is not aligned to the current PostgreSQL `jetlinks` database configuration that produced:

```text
database "jetlinks" does not exist
```

Local middleware:

```text
MySQL database: device
Redis host: 127.0.0.1
Redis database: 1
Backend port: 8848
```

## Environment Variables

The source does not commit raw jar credentials. Configure local secrets with environment variables.

For `application-wj.yml`:

```powershell
$env:LSX_REDIS_HOST = "127.0.0.1"
$env:LSX_REDIS_PORT = "6379"
$env:LSX_REDIS_PASSWORD = "<local redis password>"
$env:LSX_R2DBC_URL = "r2dbc:mysql://127.0.0.1:3306/device?ssl=false&serverZoneId=Asia/Shanghai"
$env:LSX_DB_USERNAME = "device"
$env:LSX_DB_PASSWORD = "<local mysql password>"
$env:LSX_TDENGINE_PASSWORD = ""
```

For the default `application.yml` profile, the same variables are used. Change `LSX_R2DBC_URL` to the target database URL.

## Notes

TDengine is disabled in the reconstructed jar configs:

```text
tdengine.enabled=false
```

Elasticsearch embedded mode is disabled:

```text
elasticsearch.embedded.enabled=false
```

The built jar contains the same `BOOT-INF/lib` and `BOOT-INF/classes` entry lists as the old jar, except raw credentials are replaced by environment placeholders.
