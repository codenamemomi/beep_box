package com.beepbox.dto;

public class BatteryLevelResponse {

    private String txref;
    private Integer batteryCapacity;
    private String status;

    public BatteryLevelResponse() {
    }

    public BatteryLevelResponse(String txref, Integer batteryCapacity, String status) {
        this.txref = txref;
        this.batteryCapacity = batteryCapacity;
        this.status = status;
    }

    public String getTxref() {
        return txref;
    }

    public void setTxref(String txref) {
        this.txref = txref;
    }

    public Integer getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Integer batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
