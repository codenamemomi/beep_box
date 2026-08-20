package com.beepbox.config;

import com.beepbox.model.Box;
import com.beepbox.model.BoxState;
import com.beepbox.model.Item;
import com.beepbox.repository.BoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BoxRepository boxRepository;

    public DataInitializer(BoxRepository boxRepository) {
        this.boxRepository = boxRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (boxRepository.count() > 0) {
            log.info("Database already contains data. Skipping initial data seeding.");
            return;
        }

        log.info("Preloading initial sample boxes and items into database...");

        // 1. Box available for loading (100% battery, IDLE)
        Box box1 = new Box("BOX-101", 500.0, 100, BoxState.IDLE);
        boxRepository.save(box1);

        // 2. Another box available for loading (85% battery, IDLE)
        Box box2 = new Box("BOX-102", 450.0, 85, BoxState.IDLE);
        boxRepository.save(box2);

        // 3. Low battery box (15% battery < 25%, IDLE) -> Cannot be loaded
        Box box3 = new Box("BOX-103", 300.0, 15, BoxState.IDLE);
        boxRepository.save(box3);

        // 4. Box already LOADED with pre-populated items (90% battery)
        Box box4 = new Box("BOX-104", 400.0, 90, BoxState.LOADED);
        Item item1 = new Item("meds-kit", 150.0, "MEDS_01");
        Item item2 = new Item("camera-lens", 100.0, "LENS_02");
        box4.addItem(item1);
        box4.addItem(item2);
        boxRepository.save(box4);

        // 5. Box currently DELIVERING (65% battery)
        Box box5 = new Box("BOX-105", 350.0, 65, BoxState.DELIVERING);
        Item item3 = new Item("drone-battery", 200.0, "BAT_05");
        box5.addItem(item3);
        boxRepository.save(box5);

        log.info("Preloaded {} sample boxes into the database.", boxRepository.count());
    }
}
