package com.nightmare.mixin;

import com.nightmare.NightmareMod;
import com.nightmare.NightmareSounds;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.sound.SoundCategory;

@Mixin(VillagerEntity.class)
public class VillagerMixin {
    private int messageTimer = 0;
    private static final double CHASE_RANGE = 20.0;
    private static final double MOVEMENT_SPEED = 0.6;

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo info) {
        VillagerEntity villager = (VillagerEntity)(Object)this;
        
        if (!villager.getWorld().isClient) {
            if (villager.getWorld().isNight()) {
                villager.getBrain().forget(MemoryModuleType.HOME);
                villager.getBrain().forget(MemoryModuleType.JOB_SITE);
                villager.getBrain().forget(MemoryModuleType.MEETING_POINT);
                villager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
                
                PlayerEntity nearestPlayer = villager.getWorld().getClosestPlayer(
                    villager.getX(), villager.getY(), villager.getZ(), CHASE_RANGE, true);

                if (nearestPlayer != null) {
                    villager.getNavigation().stop();
                    villager.getBrain().forget(MemoryModuleType.PATH);
                    villager.getBrain().forget(MemoryModuleType.WALK_TARGET);
                    
                    villager.getMoveControl().moveTo(
                        nearestPlayer.getX(),
                        nearestPlayer.getY(),
                        nearestPlayer.getZ(),
                        1.0 * MOVEMENT_SPEED
                    );
                    
                    messageTimer++;
                    if (messageTimer >= 80) {
                        messageTimer = 0;
                        villager.getWorld().getServer().getPlayerManager()
                            .broadcast(Text.literal("NIGHTMARE NIGHTMARE NIGHTMARE"), false);
                        
                        // Add debug message
                        NightmareMod.LOGGER.info("Playing nightmare sound at: " + villager.getX() + ", " + villager.getY() + ", " + villager.getZ());
                        
                        // Play the sound with increased volume
                        villager.getWorld().playSound(
                            null,
                            villager.getX(),
                            villager.getY(),
                            villager.getZ(),
                            NightmareSounds.NIGHTMARE_SOUND,
                            SoundCategory.HOSTILE,
                            2.0F,
                            1.0F
                        );
                    }
                }
            }
        }
    }
}