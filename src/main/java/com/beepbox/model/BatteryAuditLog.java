package com.beepbox.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "battery_audit_logs")
public class BatteryAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "box_txref", nullable = false)
    private String boxTxref;

    @Column(name = "battery_capacity", nullable = false)
    private Integer batteryCapacity;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private BoxState state;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public BatteryAuditLog() {
    }

    public BatteryAuditLog(String boxTxref, Integer batteryCapacity, BoxState state, LocalDateTime timestamp) {
        this.boxTxref = boxTxref;
        this.batteryCapacity = batteryCapacity;
        this.state = state;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBoxTxref() {
        return boxTxref;
    }

    public void setBoxTxref(String boxTxref) {
        this.boxTxref = boxTxref;
    }

    public Integer getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Integer batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public BoxState getState() {
        return state;
    }

    public void setState(BoxState state) {
        this.state = state;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatteryAuditLog log = (BatteryAuditLog) o;
        return Objects.equals(id, log.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
