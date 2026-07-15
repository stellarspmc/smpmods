package fun.spmc.smpmod.minecraft.treasure.particle;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

public class CommonParticleHandler {
    private static void tickAnimation(ServerLevel world, ArmorStand armorStand) {
        int currentFrame = armorStand.tickCount % 16;

        if (currentFrame <= 14)
            renderFrameParticles(world, armorStand, currentFrame);

        if (currentFrame == 2)
            world.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    armorStand.getX(), armorStand.getY(), armorStand.getZ(),
                    20,  // Count
                    0.0, 0.0, 0.0, // Spread (X, Y, Z)
                    0.01 // Speed
            );


        if (currentFrame == 15)
            world.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FIREWORK,
                    armorStand.getX(), armorStand.getY(), armorStand.getZ(),
                    3,   // Count
                    0.0, 0.0, 0.0, // Spread
                    0.01 // Speed
            );
    }

    private static void renderFrameParticles(ServerLevel world, ArmorStand armorStand, int frame) {
        int startPointIndex = frame * 20;

        for (int i = 0; i < 20; i++) {
            int pointIndex = (startPointIndex + i) % 100;

            double localX;
            double localZ;
            double localY = -0.500; // Fixed vertical offset matching your ^-0.500

            if (pointIndex < 25) {
                localX = -0.5 + (pointIndex * 0.04);
                localZ = -0.5;
            } else if (pointIndex < 50) {
                localX = 0.5;
                localZ = -0.5 + ((pointIndex - 25) * 0.04);
            } else if (pointIndex < 75) {
                localX = 0.5 - ((pointIndex - 50) * 0.04);
                localZ = 0.5;
            } else {
                localX = -0.5;
                localZ = 0.5 - ((pointIndex - 75) * 0.04);
            }

            Vec3 worldPos = convertCaretToWorld(armorStand, localX, localY, localZ);

            DustParticleOptions dust = new DustParticleOptions(1, 1.0f);

            world.sendParticles(dust, worldPos.x, worldPos.y, worldPos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 convertCaretToWorld(ArmorStand entity, double localX, double localY, double localZ) {
        float pitch = entity.getXRot();
        float yaw = entity.getYRot();

        float radPitch = (float) Math.toRadians(pitch);
        float radYaw = (float) Math.toRadians(yaw);

        float cosYaw = (float) Math.cos(radYaw + Math.PI / 2);
        float sinYaw = (float) Math.sin(radYaw + Math.PI / 2);
        float cosPitch = (float) Math.cos(-radPitch);
        float sinPitch = (float) Math.sin(-radPitch);
        float cosPitch90 = (float) Math.cos(-radPitch + Math.PI / 2);
        float sinPitch90 = (float) Math.sin(-radPitch + Math.PI / 2);

        Vec3 forward = new Vec3(cosYaw * cosPitch, sinPitch, sinYaw * cosPitch);
        Vec3 up = new Vec3(cosYaw * cosPitch90, sinPitch90, sinYaw * cosPitch90);
        Vec3 left = forward.cross(up).scale(-1.0);

        return entity.position()
                .add(left.scale(localX))
                .add(up.scale(localY))
                .add(forward.scale(localZ));
    }
}
