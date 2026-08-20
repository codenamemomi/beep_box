package com.beepbox.controller;

import com.beepbox.dto.BoxDto;
import com.beepbox.dto.ItemDto;
import com.beepbox.dto.LoadBoxRequest;
import com.beepbox.model.BoxState;
import com.beepbox.repository.BoxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BoxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoxRepository boxRepository;

    @BeforeEach
    void setUp() {
        boxRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/boxes - Register new box")
    void createBox_Success() throws Exception {
        BoxDto boxDto = new BoxDto("BOX-101", 300.0, 90, BoxState.IDLE);

        mockMvc.perform(post("/api/v1/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boxDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.txref").value("BOX-101"))
                .andExpect(jsonPath("$.data.weightLimit").value(300.0))
                .andExpect(jsonPath("$.data.batteryCapacity").value(90))
                .andExpect(jsonPath("$.data.state").value("IDLE"));
    }

    @Test
    @DisplayName("POST /api/v1/boxes - Validation failure when txref > 20 chars or weight > 500g")
    void createBox_ValidationFailure() throws Exception {
        BoxDto invalidDto = new BoxDto("BOX-VERY-LONG-TXREF-NAME-OVER-20-CHARS", 600.0, 105, BoxState.IDLE);

        mockMvc.perform(post("/api/v1/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/boxes/available - Query available boxes for loading")
    void getAvailableBoxes_Success() throws Exception {
        BoxDto box1 = new BoxDto("BOX-AVAIL", 400.0, 80, BoxState.IDLE);
        BoxDto box2 = new BoxDto("BOX-LOWBAT", 400.0, 15, BoxState.IDLE);

        mockMvc.perform(post("/api/v1/boxes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(box1)));

        mockMvc.perform(post("/api/v1/boxes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(box2)));

        mockMvc.perform(get("/api/v1/boxes/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].txref").value("BOX-AVAIL"));
    }

    @Test
    @DisplayName("POST /api/v1/boxes/{txref}/items - Load box with items successfully")
    void loadBoxWithItems_Success() throws Exception {
        BoxDto box = new BoxDto("BOX-LOAD-1", 450.0, 75, BoxState.IDLE);
        mockMvc.perform(post("/api/v1/boxes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(box)));

        LoadBoxRequest loadRequest = new LoadBoxRequest(List.of(
                new ItemDto("camera-kit", 150.0, "CAM_001"),
                new ItemDto("lens-filter", 50.0, "LENS_002")
        ));

        mockMvc.perform(post("/api/v1/boxes/BOX-LOAD-1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.state").value("LOADED"))
                .andExpect(jsonPath("$.data.currentWeight").value(200.0))
                .andExpect(jsonPath("$.data.items", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/v1/boxes/{txref}/items - Fail loading when battery < 25%")
    void loadBoxWithItems_LowBatteryError() throws Exception {
        BoxDto box = new BoxDto("BOX-LOW-LOAD", 450.0, 20, BoxState.IDLE);
        mockMvc.perform(post("/api/v1/boxes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(box)));

        LoadBoxRequest loadRequest = new LoadBoxRequest(List.of(
                new ItemDto("parcel", 100.0, "PARCEL_01")
        ));

        mockMvc.perform(post("/api/v1/boxes/BOX-LOW-LOAD/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Battery level is below 25%")));
    }

    @Test
    @DisplayName("POST /api/v1/boxes/{txref}/items - Fail loading when weight limit is exceeded")
    void loadBoxWithItems_WeightLimitError() throws Exception {
        BoxDto box = new BoxDto("BOX-SMALL", 200.0, 80, BoxState.IDLE);
        mockMvc.perform(post("/api/v1/boxes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(box)));

        LoadBoxRequest loadRequest = new LoadBoxRequest(List.of(
                new ItemDto("heavy-cargo", 250.0, "CARGO_01")
        ));

        mockMvc.perform(post("/api/v1/boxes/BOX-SMALL/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Weight limit exceeded")));
    }

    @Test
    @DisplayName("GET /api/v1/boxes/{txref}/battery - Check battery level")
    void getBatteryLevel_Success() throws Exception {
        BoxDto box = new BoxDto("BOX-BAT", 350.0, 95, BoxState.IDLE);
        mockMvc.perform(post("/api/v1/boxes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(box)));

        mockMvc.perform(get("/api/v1/boxes/BOX-BAT/battery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.txref").value("BOX-BAT"))
                .andExpect(jsonPath("$.data.batteryCapacity").value(95))
                .andExpect(jsonPath("$.data.status").value("OK"));
    }

    @Test
    @DisplayName("GET /api/v1/boxes/{txref}/items - Check loaded items")
    void getLoadedItems_Success() throws Exception {
        BoxDto box = new BoxDto("BOX-CHECK-ITEMS", 500.0, 85, BoxState.IDLE);
        mockMvc.perform(post("/api/v1/boxes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(box)));

        LoadBoxRequest loadRequest = new LoadBoxRequest(List.of(
                new ItemDto("gadget", 80.0, "GADGET_1")
        ));
        mockMvc.perform(post("/api/v1/boxes/BOX-CHECK-ITEMS/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loadRequest)));

        mockMvc.perform(get("/api/v1/boxes/BOX-CHECK-ITEMS/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("gadget"))
                .andExpect(jsonPath("$.data[0].code").value("GADGET_1"));
    }
}
