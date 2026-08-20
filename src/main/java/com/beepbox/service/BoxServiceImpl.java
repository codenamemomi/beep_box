package com.beepbox.service;

import com.beepbox.dto.BatteryLevelResponse;
import com.beepbox.dto.BoxDto;
import com.beepbox.dto.ItemDto;
import com.beepbox.exception.*;
import com.beepbox.model.Box;
import com.beepbox.model.BoxState;
import com.beepbox.model.Item;
import com.beepbox.repository.BoxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoxServiceImpl implements BoxService {

    private final BoxRepository boxRepository;

    public BoxServiceImpl(BoxRepository boxRepository) {
        this.boxRepository = boxRepository;
    }

    @Override
    public BoxDto createBox(BoxDto boxDto) {
        if (boxRepository.existsByTxref(boxDto.getTxref())) {
            throw new DuplicateTxrefException(boxDto.getTxref());
        }

        if (boxDto.getWeightLimit() > 500.0) {
            throw new IllegalArgumentException("Weight limit cannot exceed 500 grams");
        }

        BoxState initialState = boxDto.getState() != null ? boxDto.getState() : BoxState.IDLE;

        // Prevent setting initial state to LOADING if battery is below 25%
        if (initialState == BoxState.LOADING && boxDto.getBatteryCapacity() < 25) {
            throw new LowBatteryException(boxDto.getBatteryCapacity());
        }

        Box box = new Box(
                boxDto.getTxref(),
                boxDto.getWeightLimit(),
                boxDto.getBatteryCapacity(),
                initialState
        );

        Box savedBox = boxRepository.save(box);
        return mapToDto(savedBox);
    }

    @Override
    public BoxDto loadBoxWithItems(String txref, List<ItemDto> itemDtos) {
        Box box = boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException(txref));

        // Requirement: Prevent the box from being in LOADING state if battery level is below 25%
        if (box.getBatteryCapacity() < 25) {
            throw new LowBatteryException(box.getBatteryCapacity());
        }

        // Calculate total weight of new items + existing items
        double newItemsWeight = itemDtos.stream()
                .mapToDouble(ItemDto::getWeight)
                .sum();
        double currentWeight = box.getCurrentWeight();
        double totalWeight = currentWeight + newItemsWeight;

        // Requirement: Prevent the box from being loaded with more weight than it can carry
        if (totalWeight > box.getWeightLimit()) {
            throw new BoxWeightLimitExceededException(totalWeight, box.getWeightLimit());
        }

        // Set state to LOADING during load operation
        box.setState(BoxState.LOADING);

        // Convert item DTOs to entities and attach to box
        for (ItemDto itemDto : itemDtos) {
            Item item = new Item(itemDto.getName(), itemDto.getWeight(), itemDto.getCode());
            box.addItem(item);
        }

        // Once items are added, set state to LOADED
        box.setState(BoxState.LOADED);

        Box savedBox = boxRepository.save(box);
        return mapToDto(savedBox);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getLoadedItems(String txref) {
        Box box = boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException(txref));

        return box.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoxDto> getAvailableBoxesForLoading() {
        // Available for loading: State is IDLE and Battery Level >= 25%
        List<Box> availableBoxes = boxRepository.findByStateAndBatteryCapacityGreaterThanEqual(BoxState.IDLE, 25);
        return availableBoxes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BatteryLevelResponse getBatteryLevel(String txref) {
        Box box = boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException(txref));

        String status = box.getBatteryCapacity() < 25 ? "LOW_BATTERY" : "OK";
        return new BatteryLevelResponse(box.getTxref(), box.getBatteryCapacity(), status);
    }

    @Override
    @Transactional(readOnly = true)
    public BoxDto getBoxByTxref(String txref) {
        Box box = boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException(txref));
        return mapToDto(box);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoxDto> getAllBoxes() {
        return boxRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BoxDto updateBoxState(String txref, BoxState newState) {
        Box box = boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException(txref));

        if (newState == BoxState.LOADING && box.getBatteryCapacity() < 25) {
            throw new LowBatteryException(box.getBatteryCapacity());
        }

        box.setState(newState);
        Box updatedBox = boxRepository.save(box);
        return mapToDto(updatedBox);
    }

    private BoxDto mapToDto(Box box) {
        List<ItemDto> itemDtos = box.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());

        return new BoxDto(
                box.getId(),
                box.getTxref(),
                box.getWeightLimit(),
                box.getBatteryCapacity(),
                box.getState(),
                box.getCurrentWeight(),
                itemDtos
        );
    }

    private ItemDto mapItemToDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getWeight(),
                item.getCode()
        );
    }
}
