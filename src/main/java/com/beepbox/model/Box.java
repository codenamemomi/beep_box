package com.beepbox.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "boxes")
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Box txref is required")
    @Size(max = 20, message = "Box txref must not exceed 20 characters")
    @Column(name = "txref", nullable = false, unique = true, length = 20)
    private String txref;

    @NotNull(message = "Weight limit is required")
    @Max(value = 500, message = "Weight limit cannot exceed 500 grams")
    @Min(value = 1, message = "Weight limit must be positive")
    @Column(name = "weight_limit", nullable = false)
    private Double weightLimit;

    @NotNull(message = "Battery capacity is required")
    @Min(value = 0, message = "Battery capacity cannot be less than 0%")
    @Max(value = 100, message = "Battery capacity cannot exceed 100%")
    @Column(name = "battery_capacity", nullable = false)
    private Integer batteryCapacity;

    @NotNull(message = "State is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private BoxState state;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    public Box() {
    }

    public Box(String txref, Double weightLimit, Integer batteryCapacity, BoxState state) {
        this.txref = txref;
        this.weightLimit = weightLimit;
        this.batteryCapacity = batteryCapacity;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTxref() {
        return txref;
    }

    public void setTxref(String txref) {
        this.txref = txref;
    }

    public Double getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(Double weightLimit) {
        this.weightLimit = weightLimit;
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

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        items.add(item);
        item.setBox(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setBox(null);
    }

    public double getCurrentWeight() {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        return items.stream()
                .mapToDouble(item -> item.getWeight() != null ? item.getWeight() : 0.0)
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box box = (Box) o;
        return Objects.equals(id, box.id) && Objects.equals(txref, box.txref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, txref);
    }
}
