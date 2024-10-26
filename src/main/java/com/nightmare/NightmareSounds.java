package com.nightmare;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class NightmareSounds {
    public static final Identifier NIGHTMARE_SOUND_ID = Identifier.of("nightmare", "nightmare_scream");
    public static final SoundEvent NIGHTMARE_SOUND = SoundEvent.of(NIGHTMARE_SOUND_ID);

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, NIGHTMARE_SOUND_ID, NIGHTMARE_SOUND);
    }
}