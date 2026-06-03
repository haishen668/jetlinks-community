# LSX Device 源码恢复状态

日期：2026-06-02
最近补充：2026-06-03

## 目标

为已经在线上运行的私有改造 jar 恢复一套可维护、可二次开发的源码工程：

```text
C:\Users\Administrator\Downloads\lsx-device.jar
```

目标不是继续直接使用旧 jar，而是让本地源码工程能够编译、运行、修改，并尽量对齐旧线上 jar 的行为。

## 当前仓库状态

后端仓库：

```text
F:\project\other\jetlinks\jetlinks-community
```

私有远程仓库：

```text
origin  https://github.com/haishen668/jetlinks-community
```

本地已添加官方上游：

```text
upstream  https://gitee.com/jetlinks/jetlinks-community.git
```

官方 `2.1` 分支已拉取，并推送到私有仓库：

```text
origin/2.1
```

当前恢复分支：

```text
lsx-device-2.1.1
```

该分支基于：

```text
2.1 / 7f69c8f4878799c562cc954835695f18360af180
```

## 已保留的旧工作

从原来的 `2.11` 分支切走前，已将当时本地改动保存为 stash：

```text
stash@{0}: On 2.11: codex-save-2.11-before-switch-to-2.1
```

这些改动没有被丢弃。

## 旧 Jar 指纹

旧 jar 中的 Maven 元数据：

```text
groupId=org.jetlinks.community
artifactId=lsx-device
version=2.1.1
parent=org.jetlinks.community:jetlinks-community:2.1.1
```

`pom.properties` 中记录的 jar 构建时间：

```text
Wed May 13 21:24:03 CST 2026
```

旧 jar 中观察到的重要依赖版本：

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

主类字节码版本是 Java 8：

```text
major version: 52
```

## 公开源码搜索结论

公开 GitHub / Gitee 标签中能找到 `2.1.0` 和 `2.2.0`，但没有找到公开的 `2.1.1` 标签。

公开 `2.1` 分支仍然是 `2.1.0-SNAPSHOT`。因此当前旧 jar 更像是基于公开 `2.1` 线私有改出来的内部 `2.1.1` 构建。

## 已完成的恢复工作

恢复工作开始于本文档创建之前。当前已经完成：

```text
创建分支：lsx-device-2.1.1
复制 jetlinks-standalone 为 lsx-device
删除复制产生的 lsx-device/target 构建产物
将根 pom.xml 版本从 2.1.0-SNAPSHOT 改为 2.1.1
在根 pom.xml 中增加 lsx-device 模块
按旧 jar 指纹对齐根依赖版本：
  spring.boot.version       2.7.11
  hsweb.framework.version   4.0.17
  easyorm.version           4.1.2
  jetlinks.version          1.2.2
将子模块 parent 版本从 2.1.0-SNAPSHOT 改为 2.1.1
用旧 jar 中提取的 pom.xml 替换 lsx-device/pom.xml
将旧 jar 资源复制到 lsx-device/src/main/resources
删除 lsx-device 中旧 jar 不存在的资源文件
恢复旧 jar 中的 application.yml 和 application-wj.yml
对齐依赖版本，直到 BOOT-INF/lib 差异为 0
恢复旧 jar 中的私有定制源码类，包括认证、客户设备、通知通道、上传配置、告警、设备任务、场景动作等
对齐部分旧 jar 成员签名，包括客户设备部署、设备地址同步、REWEB 字段、客户设备查询、场景触发元数据解析等
```

这些恢复和源码级对齐提交应保留在私有 `lsx-device-2.1.1` 分支上。

## 旧 Jar 相比公开 2.1 的主要新增和改动

根据旧 jar、反编译证据、类列表对比和源码恢复过程，旧线上 jar 相比公开 JetLinks `2.1` 主要增加或改动了这些能力：

- 客户管理相关接口和实体
- 客户设备管理相关接口、导入、导出、批量更新、位置查询
- 设备详情扩展字段：品牌、型号、运营商、切卡状态、同步状态、Ping 配置、REWEB 字段、SIM 卡列表等
- REWEB 平台侧字段、`subDomain` 计算、`webpwd` 上报同步、功能调用入口
- 设备 SIM 卡实体与设备上报同步逻辑
- 设备任务接口、任务执行记录、启用、禁用、删除等能力
- 告警处理导出相关字段
- 通知通道实体扩展
- 上传配置扩展
- 场景动作和场景触发解析增强
- `lsx-device` 独立启动模块和旧线上配置资源
- 旧环境以 MySQL + Redis 为主，而不是新版本 2.11 中遇到的 PostgreSQL 配置

## 当前验证结果

基础打包命令：

```text
.\mvnw.cmd -pl lsx-device -am -DskipTests clean package
```

构建环境：

```text
JAVA_HOME=C:\Users\Administrator\.jdks\corretto-1.8.0_412
```

2026-06-02 的验证结果：

```text
BUILD SUCCESS
```

当时验证时间：

```text
2026-06-02 19:29 CST
```

打包产物：

```text
F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar
```

当时构建产物 SHA-256：

```text
A9CD5C1AAB2BAA80355DE5DFDF7C269800C00A715F196B043B2C22D48E5FE3EA
```

外层包对齐证据：

```text
BOOT-INF/lib old=357 new=357 missing=0 extra=0
BOOT-INF/classes files old=14 new=14
JetLinksApplication bytecode major version=52
```

配置对齐证据：

```text
lsx-device/src/main/resources/application.yml 与旧 jar 中的 application.yml SHA-256 一致。
lsx-device/src/main/resources/application-wj.yml 与旧 jar 中的 application-wj.yml SHA-256 一致。
旧线上配置中包含生产敏感值，因为生产部署需要直接使用该分支，所以这些配置已恢复到源码。
不要在聊天、公开文档或非私有仓库中打印或传播原始敏感值。
```

嵌套模块 class-list 审计：

```text
authentication-manager-2.1.1.jar old=70  new=72  missing-in-new=0  extra-in-new=2
device-manager-2.1.1.jar        old=210 new=212 missing-in-new=0  extra-in-new=2
rule-engine-manager-2.1.1.jar   old=128 new=151 missing-in-new=0  extra-in-new=23
notify-manager-2.1.1.jar        old=43  new=69  missing-in-new=0  extra-in-new=26
io-component-2.1.1.jar          old=32  new=32  missing-in-new=0  extra-in-new=0
```

含义：

```text
在已审计的定制模块中，旧 jar 里存在的类，在重建后的新 jar 中都能找到。
重建后的新 jar 仍保留了一些上游源码中的额外类，这些类不在旧运行时 jar 中。
```

选定成员签名审计：

```text
审计方法：对 27 个选定定制类执行 javap -private。
对比范围：全部字段，以及非 private 的源码可见方法和构造方法。
忽略范围：编译器生成的 lambda$ 和 access$ bridge 方法。
结果：TOTAL missing-members=0 extra-members=8
```

覆盖的重要类：

```text
CustomerDetail, UserDetail, UserDetailEntity, UserDetailService, TermParseUtil, CustomerController
CustomerDevice, DeviceCardEntity, DeviceInstanceEntity, DevicePosition, DeviceStateInfo, LocalDeviceInstanceService
CustomerDeviceController, CustomerDeviceExcelImporter, CustomerDeviceExcelInfo, CustomerDeviceWrapper, BatchUpdateDeviceRequest
AlarmHandleHistoryInfo, DeviceJob, DeviceJobLog, DeviceTrigger, SceneAction, DeviceJobService, DeviceJobController, AlarmHandleExcelInfo
NotifyChannelEntity, UploadProperties
```

含义：

```text
对于从旧 jar 中选出的定制类，重建后的源码没有缺失旧 jar 中的字段或非 private 方法签名。
剩余 extra 成员主要来自重建源码保留的上游兼容成员。
```

## 2026-06-03 本地 MQTT 联调补充

本地 `wj` 环境已经跑通 MQTT 模拟设备连接，详见：

```text
docs/reconstruction/local-mqtt-connection-status-20260603.md
```

关键结果：

```text
后端健康检查：{"status":"UP"}
MQTT 端口：1883
HTTP 端口：8848
协议 ID：1816752822044901376
设备 ID：869624060052013
模拟 MQTT CONNECT 返回：returnCode=0
连接保持期间，设备详情显示 state.value=online
```

本地联调额外补了两个本地恢复项：

- 修复 Windows 本地 MQTT 连接时 `connection.getClientAddress()` 为空导致的 NPE。
- 增加仅 `wj` profile 生效的本地 MQTT 认证和协议加载调试接口。

这些改动用于新源码环境，不修改旧线上 jar。

## Jar 解压和分析目录

临时分析文件位于仓库源码树之外：

```text
F:\project\other\jetlinks\.codex_tmp\lsx-fingerprint
F:\project\other\jetlinks\.codex_tmp\lsx-classes
F:\project\other\jetlinks\.codex_tmp\lsx-device-jars
```

这些目录只是分析和解压 scratch 目录，不是源码。

## 运行配置证据

旧 jar 中包含 `application.yml` 和 `application-wj.yml`。

重要结论：旧 jar 主要使用 MySQL + Redis，不是当前新版本 2.11 启动失败时遇到的 PostgreSQL `jetlinks` 数据库配置。

旧 jar 配置示例，不记录密码：

```text
application.yml:
  spring.profiles.active: dev
  spring.redis.host: 旧线上 Redis 地址
  spring.r2dbc.url: r2dbc:mysql://<线上 MySQL 地址>/<线上库名>?ssl=false&serverZoneId=Asia/Shanghai
  easyorm.default-schema: 线上库名
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

敏感凭据存在于旧 jar 配置中，并且为了生产一致性已经恢复到私有源码资源。必须将这些配置视为私有仓库材料，不要在聊天、公开文档或公开仓库中传播原始密钥。

## 下一步建议

当前分支方向：

```text
保留全局版本 2.1.1，将所有模块作为私有恢复版本继续构建。
```

继续工作前建议：

```text
1. 保留当前已跑通 MQTT 的基线，不要随意回退。
2. 决定 wj profile 下的本地调试接口是否只用于开发，线上部署前应审查。
3. 用真实线上设备验证 REWEB 功能调用和远程页面打开。
4. 如果要严格追求字节级一致，再评估是否需要移除重建 jar 中多出来的上游类。
```
