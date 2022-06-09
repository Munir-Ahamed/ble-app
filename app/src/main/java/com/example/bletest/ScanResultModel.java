package com.example.bletest;

public class ScanResultModel {
    private final String deviceName;
    private final String macAddress;
    private final int signalStrength;

    ScanResultModel (String deviceName, String macAddress, int signalStrength) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.signalStrength = signalStrength;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMacAddress() {
        return macAddress;

    }public int getSignalStrength() {
        return signalStrength;
    }

}
