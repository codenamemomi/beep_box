package com.beepbox.service;

import com.beepbox.model.BatteryAuditLog;
import com.beepbox.model.Box;
import com.beepbox.repository.BatteryAuditLogRepository;
import com.beepbox.repository.BoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BatteryAuditScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatteryAuditScheduler.class);

    private final BoxRepository boxRepository;
    private final BatteryAuditLogRepository batteryAuditLogRepository;

    public BatteryAuditScheduler(BoxRepository boxRepository, BatteryAuditLogRepository batteryAuditLogRepository) {
        this.boxRepository = boxRepository;
        this.batteryAuditLogRepository = batteryAuditLogRepository;
    }

    @Scheduled(fixedRateString = "${beepbox.battery-audit.fixed-rate:60000}")
    public void auditBoxBatteries() {
        log.info("Starting battery audit task for all registered boxes...");
        List<Box> boxes = boxRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Box box : boxes) {
            log.info("Audit Box [txref: {}, state: {}, battery: {}%]",
                    box.getTxref(), box.getState(), box.getBatteryCapacity());

            BatteryAuditLog auditLog = new BatteryAuditLog(
                    box.getTxref(),
                    box.getBatteryCapacity(),
                    box.getState(),
                    now
            );
            batteryAuditLogRepository.save(auditLog);
        }

        log.info("Completed battery audit. Processed {} boxes.", boxes.size());
    }
}
