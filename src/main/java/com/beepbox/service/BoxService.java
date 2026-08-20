package com.beepbox.service;

import com.beepbox.dto.BatteryLevelResponse;
import com.beepbox.dto.BoxDto;
import com.beepbox.dto.ItemDto;
import com.beepbox.model.BoxState;

import java.util.List;

public interface BoxService {

    BoxDto createBox(BoxDto boxDto);

    BoxDto loadBoxWithItems(String txref, List<ItemDto> itemDtos);

    List<ItemDto> getLoadedItems(String txref);

    List<BoxDto> getAvailableBoxesForLoading();

    BatteryLevelResponse getBatteryLevel(String txref);

    BoxDto getBoxByTxref(String txref);

    List<BoxDto> getAllBoxes();

    BoxDto updateBoxState(String txref, BoxState state);
}
