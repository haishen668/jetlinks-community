package org.jetlinks.community.device.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.community.device.enums.DeviceState;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DeviceStateInfo {
    private String deviceId;

    private DeviceState state;

    private String address;

    public static DeviceStateInfo of(String deviceId, DeviceState state) {
        return of(deviceId, state, null);
    }
}
