package com.beepbox.exception;

public class LowBatteryException extends RuntimeException {
    public LowBatteryException(int currentBattery) {
        super(String.format("Cannot load box or set state to LOADING: Battery level is below 25%% (Current: %d%%)", currentBattery));
    }
}
