package ewewukek.flightmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ewewukek.flightmod.Config;
import ewewukek.flightmod.FlightMod;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(
        method = "tickMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/Vec3d;)V",
            ordinal = 0
        )
    )
    private void travel(LivingEntity entity, Vec3d input) {
        if (entity instanceof ClientPlayerEntity) {
            ClientPlayerEntity player = (ClientPlayerEntity)entity;
            if (player.abilities.flying) {
                input = transformInput((ClientPlayerEntity)entity, input);
            }
        }
        entity.travel(input);
    }

    private static Vec3d transformInput(ClientPlayerEntity player, Vec3d input) {
        final float deg2rad = (float)(Math.PI / 180);

        float flySpeed = player.abilities.getFlySpeed();
        // "real" speed
        double speed = 0.98 * flySpeed * (player.isSprinting() ? 2 : 1);

        Vec3d v = player.getVelocity();
        // current forward speed
        double f = v.dotProduct(Vec3d.fromPolar(0, player.yaw));
        // current side speed
        double s = v.dotProduct(Vec3d.fromPolar(0, player.yaw - 90));

        int iy = 0;
        if (player.input.jumping) iy++;
        if (player.input.sneaking) iy--;

        if (input.lengthSquared() > 1) input = input.normalize();
        double x = input.x;
        double z = input.z;

        float cp = MathHelper.cos(-player.pitch * deg2rad);
        float sp = MathHelper.sin(-player.pitch * deg2rad);

        if (Config.movementMode.enabled() && z > 0.1 && (iy > 0 && sp > 1e-3 || iy < 0 && sp < -1e-3)) {
            if (Config.movementMode.fullSpeed()) {
                // vanilla vertical acceleration
                double a = iy * 3 * flySpeed;
                // vanilla max vertical velocity
                double vyMax = a / (1 - 0.6);
                // vanilla vertical velocity on next tick
                double vyNext = v.y * 0.6 + a;

                // alternative vertical acceleration
                double a2 = sp * speed;
                // alternative vertical velocity on next tick
                double vy2Next = v.y * 0.91 + a2;

                if (Math.abs(vy2Next) > Math.abs(vyNext)) {
                    if (Math.abs(vy2Next) > Math.abs(vyMax)) {
                        v = new Vec3d(v.x, v.y - a + a2, v.z);
                        player.setVelocity(v);
                    }
                    FlightMod.overrideVanillaFriction = true;
                }
            }

            // length of target velocity
            double l = Math.abs(v.y / sp);
            // target forward speed
            double t = l * cp;

            z = 0.98 * MathHelper.clamp((t - f) / speed, -1, 1);
        }

        if (Config.inertiaCompensation.enabled()) {
            boolean always = Config.inertiaCompensation.always();
            if (always && Math.abs(z) < 0.1) {
                if (Math.abs(x) < 0.1) {
                    Vec3d i = new Vec3d(-s / speed, 0, -f / speed);
                    if (i.length() > 0.98) i = i.normalize().multiply(0.98);
                    x = i.x;
                    z = i.z;
                } else {
                    double maxZ = Math.min(0.98, Math.sqrt(1 - x * x));
                    z = MathHelper.clamp(-f / speed, -maxZ, maxZ);
                }
            } else if ((always || z > 0.5) && Math.abs(x) < 0.1) {
                double maxX = Math.min(0.98, Math.sqrt(1 - z * z));
                x = MathHelper.clamp(-s / speed, -maxX, maxX);
            }
        }

        return new Vec3d(x, input.y, z);
    }
}
