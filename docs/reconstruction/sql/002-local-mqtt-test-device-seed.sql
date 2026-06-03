-- Optional local MQTT test product/device seed.
-- This file creates a development-only identity so the MQTT auth path can be
-- tested without importing the full 31GB online database.
--
-- MQTT official protocol auth:
-- clientId = device id
-- username = secureId + "|" + timestamp
-- password = md5(secureId + "|" + timestamp + "|" + secureKey)
--
-- Replace these values if you want to test a real device identity from the
-- online database.

SET @now_millis := ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000);
SET @product_id := 'local_mqtt_test_product';
SET @device_id := 'local_mqtt_test_001';
SET @secure_id := 'localTest01';
SET @secure_key := 'localTestSecureKey';

INSERT INTO dev_product (
    id,
    name,
    classified_id,
    classified_name,
    message_protocol,
    transport_protocol,
    network_way,
    device_type,
    metadata,
    state,
    creator_id,
    create_time,
    modify_time,
    `describe`,
    configuration,
    access_id,
    access_provider,
    access_name
) VALUES (
    @product_id,
    '本地MQTT测试产品',
    '',
    '',
    '1816752822044901376',
    'MQTT',
    'device',
    'device',
    '{}',
    1,
    'system',
    @now_millis,
    @now_millis,
    '本地 MQTT 连通性测试产品',
    JSON_OBJECT('secureId', @secure_id, 'secureKey', @secure_key),
    '1816753013292580864',
    'mqtt-server-gateway',
    'MQTT组件'
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    message_protocol = VALUES(message_protocol),
    transport_protocol = VALUES(transport_protocol),
    network_way = VALUES(network_way),
    device_type = VALUES(device_type),
    metadata = VALUES(metadata),
    state = VALUES(state),
    modify_time = VALUES(modify_time),
    configuration = VALUES(configuration),
    access_id = VALUES(access_id),
    access_provider = VALUES(access_provider),
    access_name = VALUES(access_name);

INSERT INTO dev_device_instance (
    id,
    name,
    device_type,
    product_id,
    product_name,
    configuration,
    derive_metadata,
    state,
    creator_id,
    create_time,
    registry_time,
    modify_time
) VALUES (
    @device_id,
    '本地MQTT测试设备',
    'device',
    @product_id,
    '本地MQTT测试产品',
    JSON_OBJECT('secureId', @secure_id, 'secureKey', @secure_key),
    0,
    'offline',
    'system',
    @now_millis,
    @now_millis,
    @now_millis
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    device_type = VALUES(device_type),
    product_id = VALUES(product_id),
    product_name = VALUES(product_name),
    configuration = VALUES(configuration),
    derive_metadata = VALUES(derive_metadata),
    state = VALUES(state),
    modify_time = VALUES(modify_time);
