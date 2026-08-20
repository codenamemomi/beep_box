package com.beepbox.controller;

import com.beepbox.dto.*;
import com.beepbox.service.BoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boxes")
@Tag(name = "BeepBox Management", description = "Endpoints for registering, loading, and monitoring BeepBox units.")
public class BoxController {

    private final BoxService boxService;

    public BoxController(BoxService boxService) {
        this.boxService = boxService;
    }

    @PostMapping
    @Operation(summary = "Create a box", description = "Registers a new delivery box in the system.")
    public ResponseEntity<ApiResponse<BoxDto>> createBox(@Valid @RequestBody BoxDto boxDto) {
        BoxDto createdBox = boxService.createBox(boxDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Box created successfully", createdBox));
    }

    @GetMapping
    @Operation(summary = "List all boxes", description = "Retrieves a list of all registered boxes.")
    public ResponseEntity<ApiResponse<List<BoxDto>>> getAllBoxes() {
        List<BoxDto> boxes = boxService.getAllBoxes();
        return ResponseEntity.ok(ApiResponse.success(boxes));
    }

    @GetMapping("/available")
    @Operation(summary = "Check available boxes for loading", description = "Returns boxes that are in IDLE state and have a battery capacity of at least 25%.")
    public ResponseEntity<ApiResponse<List<BoxDto>>> getAvailableBoxesForLoading() {
        List<BoxDto> availableBoxes = boxService.getAvailableBoxesForLoading();
        return ResponseEntity.ok(ApiResponse.success("Available boxes for loading retrieved successfully", availableBoxes));
    }

    @GetMapping("/{txref}")
    @Operation(summary = "Get box details", description = "Retrieves detailed information for a box by its txref.")
    public ResponseEntity<ApiResponse<BoxDto>> getBoxByTxref(@PathVariable String txref) {
        BoxDto box = boxService.getBoxByTxref(txref);
        return ResponseEntity.ok(ApiResponse.success(box));
    }

    @PostMapping("/{txref}/items")
    @Operation(summary = "Load a box with items", description = "Loads specified items into a box. Rejects if battery is below 25% or total weight exceeds weight limit.")
    public ResponseEntity<ApiResponse<BoxDto>> loadBoxWithItems(
            @PathVariable String txref,
            @Valid @RequestBody LoadBoxRequest request) {
        BoxDto updatedBox = boxService.loadBoxWithItems(txref, request.getItems());
        return ResponseEntity.ok(ApiResponse.success("Items loaded into box successfully", updatedBox));
    }

    @GetMapping("/{txref}/items")
    @Operation(summary = "Check loaded items for a given box", description = "Retrieves all items currently loaded into the box.")
    public ResponseEntity<ApiResponse<List<ItemDto>>> getLoadedItems(@PathVariable String txref) {
        List<ItemDto> items = boxService.getLoadedItems(txref);
        return ResponseEntity.ok(ApiResponse.success("Loaded items retrieved successfully", items));
    }

    @GetMapping("/{txref}/battery")
    @Operation(summary = "Check battery level for a given box", description = "Retrieves the current battery percentage and status for a box.")
    public ResponseEntity<ApiResponse<BatteryLevelResponse>> getBatteryLevel(@PathVariable String txref) {
        BatteryLevelResponse batteryLevel = boxService.getBatteryLevel(txref);
        return ResponseEntity.ok(ApiResponse.success(batteryLevel));
    }

    @PatchMapping("/{txref}/state")
    @Operation(summary = "Update box state", description = "Manually updates the state of a box (e.g. IDLE, DELIVERING, DELIVERED, RETURNING).")
    public ResponseEntity<ApiResponse<BoxDto>> updateBoxState(
            @PathVariable String txref,
            @Valid @RequestBody BoxStateUpdateRequest request) {
        BoxDto updatedBox = boxService.updateBoxState(txref, request.getState());
        return ResponseEntity.ok(ApiResponse.success("Box state updated successfully", updatedBox));
    }
}
