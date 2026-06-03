# REWEB 后端恢复说明

日期：2026-06-03

## 范围

本文记录 LSX / JetLinks 2.1.1 私有改造版本中 REWEB 相关后端能力的恢复情况。

旧线上 jar 只作为只读证据使用：

- `C:\Users\Administrator\Downloads\lsx-device.jar`
- 解压分析目录：`F:\project\other\jetlinks\.codex_tmp\lsx-device-jar`

## 旧 Jar 证据

旧 `device-manager-2.1.1.jar` 中包含 REWEB 相关行为，主要集中在以下类：

- `DeviceDetail`
  - 对外暴露 `port`、`subDomain`、`passwd`、`operator`、`switchState`、`syncFlag`、`pingAddr`、`pingRetry`、`cards`
  - 使用 `md5(mac)` 计算 `subDomain`
  - 提供 `withCards(...)` 和 `withPort(...)`
- `LocalDeviceInstanceService`
  - 按设备分组查询 `DeviceCardEntity`
  - 将 SIM 卡列表组装到 `DeviceDetail`
  - 调用 `getPort(deviceId)`，并把端口写入设备详情响应
- `DeviceMessageBusinessHandler`
  - 订阅设备属性上报
  - 将设备上报的 `webpwd` 保存为 `passwd`
  - 解析 `gpsloc` 并解析高德位置
  - 同步 `switch_state`、`ping_addr`、`ping_retry`、`syncFlag`
  - 写入当前生效 SIM 卡，并将其他 SIM 卡标记为非生效

在已恢复的后端源码和旧平台 jar 中，没有发现独立的 FRP Controller 或 FRP Service。这不表示 REWEB 与 FRP 无关，而是说明 Java 平台本身不直接运行或配置 FRP。

目前恢复出的 REWEB 设计是旧版本的链路：

1. 前端读取设备详情中的 `subDomain`
2. 前端把 `subDomain` 作为 `reweb` 功能的第一个入参
3. 前端调用标准设备功能接口
4. 后端发送标准 JetLinks `FunctionInvokeMessage`
5. 前端在调用成功后打开 `http://{subDomain}.reweb.wugee.net.cn`

平台侧调用链证据：

- 旧前端把 `reweb` 的第一个输入参数设置为 `subDomain`
- 旧前端和已恢复前端都会调用 `POST /device/instance/{deviceId}/function/{functionId}`
- 后端进入 `LocalDeviceInstanceService.invokeFunction(...)`
- `invokeFunction(...)` 发送标准 JetLinks `FunctionInvokeMessage`
- `DeviceMessageConnector` 将 `INVOKE_FUNCTION` 映射到 `/message/send/function`

因此，实际远程 Web 隧道预计由 Java 平台之外的组件建立，最可能是设备固件和 `*.reweb.wugee.net.cn` 后面的外部 REWEB / FRP 网关共同完成。

## 额外检查

2026-06-03 已做过这些检查：

- `data/protocols` 和 `data/files/20260510` 中存在协议 jar 缓存。
- 非空缓存 jar 是 `jetlinks-official-protocol`，只包含标准 MQTT、HTTP、TCP、binary 编解码能力。
- 在该协议 jar 中未发现 `frp`、`frpc`、`frps`、`reweb`、`wugee` 常量。
- `reweb.wugee.net.cn`、随机子域名 `*.reweb.wugee.net.cn`、`agent.wugee.net.cn` 都解析到同一个公网 IP。
- 本机到 `agent.wugee.net.cn:1883` 的 TCP 连接可达。

## 已恢复的后端改动

已恢复或对齐的文件：

- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/response/DeviceDetail.java`
  - 恢复旧前端需要的 LSX 客户设备详情字段
  - 恢复 SIM 卡列表和 REWEB 端口响应字段
  - 恢复 `subDomain = md5(mac)` 逻辑
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/service/LocalDeviceInstanceService.java`
  - 恢复设备 SIM 卡分组查询
  - 恢复 `withCards(...)` 和 `withPort(...)` 详情组装
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/service/DeviceMessageBusinessHandler.java`
  - 恢复 REWEB 密码、GPS 位置、Ping 配置、切卡同步状态、SIM 卡状态的属性上报同步逻辑
- `jetlinks-manager/device-manager/src/main/java/org/jetlinks/community/device/entity/DeviceProductEntity.java`
  - 恢复客户设备详情中使用的 `brand` 字段

## 验证

使用 IDEA JDK 8 执行过后端编译和打包：

```powershell
$env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-1.8.0_412'
& 'C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.3-bin\6actqn1ngkbj8g7k704ro02jj7\apache-maven-3.9.3\bin\mvn.cmd' -q -pl jetlinks-manager\device-manager -am -DskipTests compile
& 'C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.3-bin\6actqn1ngkbj8g7k704ro02jj7\apache-maven-3.9.3\bin\mvn.cmd' -q -pl lsx-device -am -DskipTests clean package
```

结果：

- 后端编译通过
- 后端打包通过
- 打包产物：`F:\project\other\jetlinks\jetlinks-community\lsx-device\target\lsx-device.jar`

## 剩余运行时依赖

后端已经恢复旧版本的平台侧响应字段和消息同步链路。实际远程 Web 是否可用，仍依赖设备侧实现和外部 REWEB / FRP 基础设施：

```text
*.reweb.wugee.net.cn
agent.wugee.net.cn:1883
```

这部分外部基础设施不在当前源码树中。平台侧负责：

- 暴露 REWEB 所需字段
- 同步设备上报的 `webpwd`
- 向设备发送 `reweb` 功能调用

是否能真正打开远程 Web 页面，需要用真实在线设备和线上 REWEB / FRP 服务继续验证。
