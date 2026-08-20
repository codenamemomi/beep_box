package com.beepbox.repository;

import com.beepbox.model.BatteryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatteryAuditLogRepository extends JpaRepository<BatteryAuditLog, Long> {

    List<BatteryAuditLog> findByBoxTxrefOrderByTimestampDesc(String boxTxref);
}
