package com.beepbox.dto;

import com.beepbox.model.BoxState;
import jakarta.validation.constraints.NotNull;

public class BoxStateUpdateRequest {

    @NotNull(message = "State is required")
    private BoxState state;

    public BoxStateUpdateRequest() {
    }

    public BoxStateUpdateRequest(BoxState state) {
        this.state = state;
    }

    public BoxState getState() {
        return state;
    }

    public void setState(BoxState state) {
        this.state = state;
    }
}
