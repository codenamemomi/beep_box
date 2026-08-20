package com.beepbox.repository;

import com.beepbox.model.Box;
import com.beepbox.model.BoxState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {

    Optional<Box> findByTxref(String txref);

    boolean existsByTxref(String txref);

    List<Box> findByStateAndBatteryCapacityGreaterThanEqual(BoxState state, Integer batteryCapacity);
}
