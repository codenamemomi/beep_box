package com.beepbox.dto;

import com.beepbox.model.BoxState;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class BoxDto {

    private Long id;

    @NotBlank(message = "Box txref is required")
    @Size(max = 20, message = "Box txref must not exceed 20 characters")
    private String txref;

    @NotNull(message = "Weight limit is required")
    @Max(value = 500, message = "Weight limit cannot exceed 500 grams")
    @Min(value = 1, message = "Weight limit must be positive")
    private Double weightLimit;

    @NotNull(message = "Battery capacity is required")
    @Min(value = 0, message = "Battery capacity cannot be less than 0%")
    @Max(value = 100, message = "Battery capacity cannot exceed 100%")
    private Integer batteryCapacity;

    private BoxState state;

    private Double currentWeight;

    private List<ItemDto> items = new ArrayList<>();

    public BoxDto() {
    }

    public BoxDto(String txref, Double weightLimit, Integer batteryCapacity, BoxState state) {
        this.txref = txref;
        this.weightLimit = weightLimit;
        this.batteryCapacity = batteryCapacity;
        this.state = state;
    }

    public BoxDto(Long id, String txref, Double weightLimit, Integer batteryCapacity, BoxState state, Double currentWeight, List<ItemDto> items) {
        this.id = id;
        this.txref = txref;
        this.weightLimit = weightLimit;
        this.batteryCapacity = batteryCapacity;
        this.state = state;
        this.currentWeight = currentWeight;
        this.items = items;
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

    public Double getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(Double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }
}
