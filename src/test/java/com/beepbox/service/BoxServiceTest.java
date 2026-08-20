package com.beepbox.service;

import com.beepbox.dto.BatteryLevelResponse;
import com.beepbox.dto.BoxDto;
import com.beepbox.dto.ItemDto;
import com.beepbox.exception.BoxNotFoundException;
import com.beepbox.exception.BoxWeightLimitExceededException;
import com.beepbox.exception.DuplicateTxrefException;
import com.beepbox.exception.LowBatteryException;
import com.beepbox.model.Box;
import com.beepbox.model.BoxState;
import com.beepbox.repository.BoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private BoxRepository boxRepository;

    @InjectMocks
    private BoxServiceImpl boxService;

    private Box sampleBox;
    private BoxDto sampleBoxDto;

    @BeforeEach
    void setUp() {
        sampleBox = new Box("BOX-001", 400.0, 80, BoxState.IDLE);
        sampleBox.setId(1L);

        sampleBoxDto = new BoxDto("BOX-001", 400.0, 80, BoxState.IDLE);
    }

    @Test
    @DisplayName("Should successfully create a new box with valid details")
    void createBox_Success() {
        when(boxRepository.existsByTxref("BOX-001")).thenReturn(false);
        when(boxRepository.save(any(Box.class))).thenReturn(sampleBox);

        BoxDto created = boxService.createBox(sampleBoxDto);

        assertNotNull(created);
        assertEquals("BOX-001", created.getTxref());
        assertEquals(400.0, created.getWeightLimit());
        assertEquals(80, created.getBatteryCapacity());
        assertEquals(BoxState.IDLE, created.getState());
        verify(boxRepository, times(1)).save(any(Box.class));
    }

    @Test
    @DisplayName("Should auto-generate txref when txref is omitted/null")
    void createBox_AutoGenerateTxref() {
        BoxDto dtoWithoutTxref = new BoxDto(350.0, 90, BoxState.IDLE);
        when(boxRepository.existsByTxref(anyString())).thenReturn(false);
        when(boxRepository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoxDto created = boxService.createBox(dtoWithoutTxref);

        assertNotNull(created);
        assertNotNull(created.getTxref());
        assertTrue(created.getTxref().startsWith("BOX-"));
        verify(boxRepository, times(1)).save(any(Box.class));
    }

    @Test
    @DisplayName("Should throw DuplicateTxrefException if txref already exists")
    void createBox_DuplicateTxref() {
        when(boxRepository.existsByTxref("BOX-001")).thenReturn(true);

        assertThrows(DuplicateTxrefException.class, () -> boxService.createBox(sampleBoxDto));
        verify(boxRepository, never()).save(any(Box.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if weight limit > 500g")
    void createBox_WeightLimitExceeded() {
        BoxDto invalidDto = new BoxDto("BOX-002", 600.0, 80, BoxState.IDLE);
        when(boxRepository.existsByTxref("BOX-002")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> boxService.createBox(invalidDto));
    }

    @Test
    @DisplayName("Should reject loading items if box battery is below 25%")
    void loadBoxWithItems_LowBattery() {
        Box lowBatteryBox = new Box("BOX-LOW", 500.0, 20, BoxState.IDLE);
        when(boxRepository.findByTxref("BOX-LOW")).thenReturn(Optional.of(lowBatteryBox));

        List<ItemDto> items = List.of(new ItemDto("meds", 50.0, "MED_01"));

        assertThrows(LowBatteryException.class, () -> boxService.loadBoxWithItems("BOX-LOW", items));
        verify(boxRepository, never()).save(any(Box.class));
    }

    @Test
    @DisplayName("Should reject loading items if total weight exceeds weight limit")
    void loadBoxWithItems_Overweight() {
        when(boxRepository.findByTxref("BOX-001")).thenReturn(Optional.of(sampleBox));

        List<ItemDto> items = List.of(
                new ItemDto("heavy-item", 450.0, "HEAVY_01")
        );

        assertThrows(BoxWeightLimitExceededException.class, () -> boxService.loadBoxWithItems("BOX-001", items));
    }

    @Test
    @DisplayName("Should successfully load box with items when battery >= 25% and weight <= limit")
    void loadBoxWithItems_Success() {
        when(boxRepository.findByTxref("BOX-001")).thenReturn(Optional.of(sampleBox));
        when(boxRepository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ItemDto> items = List.of(
                new ItemDto("item-1", 100.0, "CODE_1"),
                new ItemDto("item-2", 150.0, "CODE_2")
        );

        BoxDto loadedBox = boxService.loadBoxWithItems("BOX-001", items);

        assertNotNull(loadedBox);
        assertEquals(BoxState.LOADED, loadedBox.getState());
        assertEquals(250.0, loadedBox.getCurrentWeight());
        assertEquals(2, loadedBox.getItems().size());
        verify(boxRepository, times(1)).save(sampleBox);
    }

    @Test
    @DisplayName("Should return available boxes in IDLE state with battery >= 25%")
    void getAvailableBoxesForLoading_Success() {
        when(boxRepository.findByStateAndBatteryCapacityGreaterThanEqual(BoxState.IDLE, 25))
                .thenReturn(List.of(sampleBox));

        List<BoxDto> available = boxService.getAvailableBoxesForLoading();

        assertEquals(1, available.size());
        assertEquals("BOX-001", available.get(0).getTxref());
    }

    @Test
    @DisplayName("Should return battery level and status for a box")
    void getBatteryLevel_Success() {
        when(boxRepository.findByTxref("BOX-001")).thenReturn(Optional.of(sampleBox));

        BatteryLevelResponse batteryLevel = boxService.getBatteryLevel("BOX-001");

        assertNotNull(batteryLevel);
        assertEquals("BOX-001", batteryLevel.getTxref());
        assertEquals(80, batteryLevel.getBatteryCapacity());
        assertEquals("OK", batteryLevel.getStatus());
    }

    @Test
    @DisplayName("Should throw BoxNotFoundException when querying non-existent txref")
    void getBox_NotFound() {
        when(boxRepository.findByTxref("INVALID")).thenReturn(Optional.empty());

        assertThrows(BoxNotFoundException.class, () -> boxService.getBoxByTxref("INVALID"));
    }
}
