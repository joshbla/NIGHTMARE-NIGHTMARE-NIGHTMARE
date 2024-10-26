package com.nightmare;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NightmareMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("nightmare");

    @Override
    public void onInitialize() {
        NightmareSounds.registerSounds();
        LOGGER.info("NIGHTMARE NIGHTMARE NIGHTMARE initialized");
    }
}