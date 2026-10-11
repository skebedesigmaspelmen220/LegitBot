package com.legitbot.mixin;

import com.legitbot.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(ClientPlayerEntity.class)
public class LegitAimbotMixin {

    public static LivingEntity currentTarget = null;
    @Unique private final Map<UUID, Integer> reactionTimer = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (client.world == null || player == null) return;
        
        if (player.age % 200 == 0) reactionTimer.clear();

        if (!ModConfig.isEnabled) {
            currentTarget = null;
            return;
        }

        LivingEntity target = findBestTarget(client, player);
        currentTarget = target;

        if (target != null) {
            int seenTicks = reactionTimer.getOrDefault(target.getUuid(), 0);
            
            if (canSeeStrict(client, player, target)) {
                reactionTimer.put(target.getUuid(), seenTicks + 1);
            } else {
                reactionTimer.put(target.getUuid(), 0);
                return; 
            }

            if (seenTicks < ModConfig.reactionDelay) return;

            aimAtTarget(client, player, target);
        }
    }

    @Unique
    private void aimAtTarget(MinecraftClient client, ClientPlayerEntity player, LivingEntity target) {
        float[] angles = getAngles(player, target);
        
        // Получение чувствительности для версии 1.21.11
        float sensitivity = client.options.getMouseSensitivity().getValue().floatValue();
        float m = sensitivity * 0.6f + 0.2f;
        float gcd = m * m * m * 8.0f * 0.15f;

        float yawDiff = MathHelper.wrapDegrees(angles[0] - player.getYaw());
        float pitchDiff = MathHelper.wrapDegrees(angles[1] - player.getPitch());

        float safeSmooth = Math.max(1.0f, ModConfig.smooth);

        float yawChange = yawDiff / safeSmooth;
        float pitchChange = pitchDiff / safeSmooth;

        yawChange -= yawChange % gcd;
        pitchChange -= pitchChange % gcd;

        player.setYaw(player.getYaw() + yawChange);
        player.setPitch(MathHelper.clamp(player.getPitch() + pitchChange, -90F, 90F));
    }

    @Unique
    private LivingEntity findBestTarget(MinecraftClient client, ClientPlayerEntity player) {
        LivingEntity best = null;
        double closestAngle = ModConfig.fov;

        for (Entity e : client.world.getEntities()) {
            if (e instanceof LivingEntity target && e != player && target.isAlive() && !(e instanceof ArmorStandEntity)) {
                if (player.distanceTo(target) > ModConfig.distance) continue;

                float[] angles = getAngles(player, target);
                double diff = Math.abs(MathHelper.wrapDegrees(player.getYaw() - angles[0]));

                if (diff < closestAngle) {
                    closestAngle = diff;
                    best = target;
                }
            }
        }
        return best;
    }

    @Unique
    private float[] getAngles(ClientPlayerEntity player, LivingEntity target) {
        Vec3d eye = player.getEyePos();
        Vec3d t = target.getPos().add(0, target.getEyeHeight() * 0.85, 0);
        double dX = t.x - eye.x;
        double dY = t.y - eye.y;
        double dZ = t.z - eye.z;
        double dXZ = Math.sqrt(dX * dX + dZ * dZ);
        float yaw = (float)(Math.toDegrees(Math.atan2(dZ, dX)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dY, dXZ)));
        return new float[]{yaw, pitch};
    }

    @Unique
    private boolean canSeeStrict(MinecraftClient client, ClientPlayerEntity player, LivingEntity target) {
        Vec3d eyePos = player.getEyePos();
        Vec3d targetPos = target.getPos().add(0, target.getEyeHeight(), 0);
        HitResult result = client.world.raycast(new RaycastContext(eyePos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        return result.getType() == HitResult.Type.MISS;
    }
}