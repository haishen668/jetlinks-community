-- JetLinks 2.1.1 local MQTT protocol/gateway seed.
-- Run after the application has created tables at least once.
-- Working directory must be jetlinks-community when the backend starts, because
-- file.manager.storage-base-path is ./data/files in application-wj.yml.

SET @now_millis := ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000);

-- Online protocol jar evidence:
-- file id: 452f67679610c2197e97f6d678a93038
-- local file path: ./data/files/20250307/452f67679610c2197e97f6d678a93038.jar
-- sha256: fb0c6144ad056326e26eb829c13759b5080da095c7bb02386c7f064ac059f24e
--
-- server_node_id must match jetlinks.server-id.
-- With application-wj.yml default port 8848, that value is lsx-device:8848.
-- If the backend port is changed with --server.port=18848, update this value
-- to lsx-device:18848 before loading the seed.
INSERT INTO s_file (
    id,
    name,
    extension,
    length,
    md5,
    sha256,
    create_time,
    creator_id,
    server_node_id,
    storage_path,
    options,
    others
) VALUES (
    '452f67679610c2197e97f6d678a93038',
    'lsx-official-protocol.jar',
    'jar',
    102512,
    '24504ceb0d6570b84b86e6180d9fca9f',
    'fb0c6144ad056326e26eb829c13759b5080da095c7bb02386c7f064ac059f24e',
    1741332825208,
    'system',
    'lsx-device:8848',
    '20250307/452f67679610c2197e97f6d678a93038.jar',
    NULL,
    NULL
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    extension = VALUES(extension),
    length = VALUES(length),
    md5 = VALUES(md5),
    sha256 = VALUES(sha256),
    server_node_id = VALUES(server_node_id),
    storage_path = VALUES(storage_path);

-- Online protocol support:
-- id: 1816752822044901376
-- name: 标准协议
-- type: jar
-- The local seed intentionally uses fileId only. This avoids depending on the
-- online access URL or access key while still exercising the same jar loader.
INSERT INTO dev_protocol (
    id,
    name,
    description,
    type,
    state,
    creator_id,
    create_time,
    configuration
) VALUES (
    '1816752822044901376',
    '标准协议',
    '线上 MQTT 标准协议包，本地通过 fileId 加载',
    'jar',
    1,
    'system',
    @now_millis,
    '{"fileId":"452f67679610c2197e97f6d678a93038","provider":"org.jetlinks.protocol.official.JetLinksProtocolSupportProvider"}'
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    type = VALUES(type),
    state = VALUES(state),
    configuration = VALUES(configuration);

-- Online MQTT server network:
-- id: 1816751690245521408
-- online publicHost is agent.wugee.net.cn. Local seed uses 127.0.0.1 so test
-- clients can connect to the development machine directly.
INSERT INTO network_config (
    id,
    name,
    description,
    type,
    state,
    creator_id,
    create_time,
    configuration,
    share_cluster,
    cluster
) VALUES (
    '1816751690245521408',
    'MQTT直连',
    '本地复现线上 MQTT_SERVER 网络组件',
    'MQTT_SERVER',
    'enabled',
    'system',
    @now_millis,
    '{"host":"0.0.0.0","port":1883,"publicHost":"127.0.0.1","publicPort":1883,"secure":false,"maxMessageSize":8192}',
    1,
    NULL
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    type = VALUES(type),
    state = VALUES(state),
    configuration = VALUES(configuration),
    share_cluster = VALUES(share_cluster),
    cluster = VALUES(cluster);

-- Online MQTT device gateway:
-- id: 1816753013292580864
-- provider: mqtt-server-gateway
-- channel_id points to network_config.id above.
-- protocol points to dev_protocol.id above.
INSERT INTO device_gateway (
    id,
    name,
    provider,
    state,
    channel,
    channel_id,
    protocol,
    transport,
    configuration,
    description,
    creator_id,
    create_time,
    modifier_id,
    modify_time,
    state_time
) VALUES (
    '1816753013292580864',
    'MQTT组件',
    'mqtt-server-gateway',
    'enabled',
    'network',
    '1816751690245521408',
    '1816752822044901376',
    'MQTT',
    NULL,
    '本地复现线上 MQTT 网关',
    'system',
    @now_millis,
    'system',
    @now_millis,
    @now_millis
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    provider = VALUES(provider),
    state = VALUES(state),
    channel = VALUES(channel),
    channel_id = VALUES(channel_id),
    protocol = VALUES(protocol),
    transport = VALUES(transport),
    configuration = VALUES(configuration),
    description = VALUES(description),
    modifier_id = VALUES(modifier_id),
    modify_time = VALUES(modify_time),
    state_time = VALUES(state_time);
