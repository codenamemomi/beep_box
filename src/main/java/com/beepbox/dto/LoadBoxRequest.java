package com.beepbox.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class LoadBoxRequest {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<ItemDto> items = new ArrayList<>();

    public LoadBoxRequest() {
    }

    public LoadBoxRequest(List<ItemDto> items) {
        this.items = items;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }
}
