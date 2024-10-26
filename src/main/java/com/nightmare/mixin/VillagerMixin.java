package com.nightmare.mixin;

import com.nightmare.NightmareMod;
import com.nightmare.NightmareSounds;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import java.util.Random;

@Mixin(VillagerEntity.class)
public class VillagerMixin {
    private int messageTimer = 0;
    private int flickerTimer = 0;
    private int particleTimer = 0;  // New timer for regular particle effects
    private float headRotation = 0f;
    private static final double CHASE_RANGE = 20.0;
    private static final double MOVEMENT_SPEED = 0.6;
    private static final float SPIN_SPEED = 30f;
    private final Random random = new Random();

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo info) {
        VillagerEntity villager = (VillagerEntity)(Object)this;
        
        if (!villager.getWorld().isClient) {
            if (villager.getWorld().isNight()) {
                // Clear their normal behaviors
                villager.getBrain().forget(MemoryModuleType.HOME);
                villager.getBrain().forget(MemoryModuleType.JOB_SITE);
                villager.getBrain().forget(MemoryModuleType.MEETING_POINT);
                villager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
                
                PlayerEntity nearestPlayer = villager.getWorld().getClosestPlayer(
                    villager.getX(), villager.getY(), villager.getZ(), CHASE_RANGE, true);
    
                if (nearestPlayer != null) {
                    // Stop existing navigation
                    villager.getNavigation().stop();
                    villager.getBrain().forget(MemoryModuleType.PATH);
                    villager.getBrain().forget(MemoryModuleType.WALK_TARGET);
                    
                    // Set sprinting
                    villager.setSprinting(true);
                    
                    // Calculate direction to player
                    double dx = nearestPlayer.getX() - villager.getX();
                    double dz = nearestPlayer.getZ() - villager.getZ();
                    double length = Math.sqrt(dx * dx + dz * dz);
                    dx = dx / length;
                    dz = dz / length;
    
                    // Check for blocks in front of villager
                    BlockPos blockInFront = new BlockPos(
                        (int)(villager.getX() + dx),
                        (int)villager.getY(),
                        (int)(villager.getZ() + dz)
                    );
                    BlockPos blockAbove = blockInFront.up();
    
                    // If there's a solid block in front and above is air, jump
                    if (villager.getWorld().getBlockState(blockInFront).isFullCube(villager.getWorld(), blockInFront) &&
                        villager.getWorld().getBlockState(blockAbove).isAir()) {
                        if (villager.isOnGround()) {
                            villager.jump();
                            villager.setVelocity(dx * 0.4, villager.getVelocity().y, dz * 0.4);
                        }
                    }
    
                    // Direct movement towards player
                    villager.getMoveControl().moveTo(
                        nearestPlayer.getX(),
                        nearestPlayer.getY(),
                        nearestPlayer.getZ(),
                        1.0 * MOVEMENT_SPEED
                    );
                    
                    // Message and sound timer
                    messageTimer++;
                    if (messageTimer >= 80) {
                        messageTimer = 0;
                        villager.getWorld().getServer().getPlayerManager()
                            .broadcast(Text.literal("NIGHTMARE NIGHTMARE NIGHTMARE"), false);
                        
                        NightmareMod.LOGGER.info("Playing nightmare sound at: " + villager.getX() + ", " + villager.getY() + ", " + villager.getZ());
                        
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

                    // Handle flickering
                    flickerTimer++;
                    if (flickerTimer >= 20) {
                        flickerTimer = 0;
                        if (villager.getWorld().getRandom().nextFloat() < 0.2f) {
                            villager.setInvisible(!villager.isInvisible());
                            
                            // Spawn portal particles when flickering
                            ServerWorld serverWorld = (ServerWorld) villager.getWorld();
                            for (int i = 0; i < 20; i++) {
                                serverWorld.spawnParticles(
                                    ParticleTypes.PORTAL,
                                    villager.getX() + (random.nextDouble() - 0.5),
                                    villager.getY() + random.nextDouble() * 2,
                                    villager.getZ() + (random.nextDouble() - 0.5),
                                    1, 0, 0, 0, 0.1
                                );
                            }
                        }
                    }

                    // Spin head
                    headRotation += SPIN_SPEED;
                    if (headRotation >= 360f) {
                        headRotation = 0f;
                    }
                    villager.setHeadYaw(headRotation);
                    villager.setBodyYaw(headRotation);

                    // Regular particle effects
                    particleTimer++;
                    if (particleTimer >= 2) { // Every 2 ticks
                        particleTimer = 0;
                        ServerWorld serverWorld = (ServerWorld) villager.getWorld();
                        
                        // Smoke trail
                        serverWorld.spawnParticles(
                            ParticleTypes.SMOKE,
                            villager.getX(),
                            villager.getY() + 0.2,
                            villager.getZ(),
                            1, 0.2, 0, 0.2, 0.01
                        );

                        // Soul particles around head
                        double radius = 0.3;
                        double angle = Math.toRadians(headRotation);
                        serverWorld.spawnParticles(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            villager.getX() + radius * Math.cos(angle),
                            villager.getY() + 2,
                            villager.getZ() + radius * Math.sin(angle),
                            1, 0, 0, 0, 0.02
                        );
                    }
                }
            } else {
                // Reset villager state during day
                villager.setInvisible(false);
            }
        }
    }
}