package fun.spmc.smpmod.mobs.boss;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

public class CrystalBoss extends Warden implements PolymerEntity {
    private final EndCrystal[] orbitingCrystals = new EndCrystal[3];
    private int roarAbilityCooldown = 0;

    public CrystalBoss(EntityType<? extends Warden> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Warden.createAttributes()
                .add(Attributes.MAX_HEALTH, 1250)
                .add(Attributes.MOVEMENT_SPEED, .3)
                .add(Attributes.ATTACK_DAMAGE, 30);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            updateOrbitingCrystals();

            if (this.roarAbilityCooldown > 0) this.roarAbilityCooldown--;
            if (this.roarAnimationState.isStarted() && this.roarAbilityCooldown <= 0) {
                triggerCrystalExplosionAbility();
                this.roarAbilityCooldown = 200;
            }
        }
    }

    private void updateOrbitingCrystals() {
        double radius = 2.5;
        double speed = .05;

        for (int i = 0; i < 3; i++) {
            if (orbitingCrystals[i] == null || !orbitingCrystals[i].isAlive()) {
                EndCrystal crystal = new EndCrystal(this.level(), this.getX(), this.getY() + 2, this.getZ());
                crystal.setShowBottom(false);
                crystal.setInvulnerable(true);
                this.level().addFreshEntity(crystal);
                orbitingCrystals[i] = crystal;
            }

            double angle = (this.tickCount * speed) + (i * (2 * Math.PI / 3));
            orbitingCrystals[i].teleportTo(this.getX() + radius * Math.cos(angle), this.getY() + 2.5 + Math.sin(this.tickCount * 0.1 + i), this.getZ() + radius * Math.sin(angle));
        }
    }

    private void triggerCrystalExplosionAbility() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        for (int i = 0; i < 3; i++) {
            BlockPos targetPos = target.blockPosition().offset((int) (this.random.nextDouble() - .5) * 6, 0, (int) (this.random.nextDouble() - .5) * 6);
            this.level().explode(this, targetPos.getX(), targetPos.getY(), targetPos.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource damageSource, boolean flag) {
        super.dropCustomDeathLoot(level, damageSource, flag);

        for (EndCrystal crystal : orbitingCrystals) if (crystal != null) crystal.discard();


        this.spawnAtLocation(level, new ItemStack(Items.HEART_OF_THE_SEA, 15));
        this.spawnAtLocation(level, new ItemStack(Items.ECHO_SHARD, 35));
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityTypes.WARDEN;
    }

    public static boolean trySpawnBoss(ServerLevel level, BlockPos crystalPos) {
        BlockPos centerPos = crystalPos.below();

        if (!level.getBlockState(centerPos).is(Blocks.OBSIDIAN)) return false;
        if (!level.getBlockState(centerPos.below()).is(Blocks.OBSIDIAN)) return false;
        if (!level.getBlockState(centerPos.east()).is(Blocks.END_STONE) ||
                !level.getBlockState(centerPos.west()).is(Blocks.END_STONE)) {
            if (!level.getBlockState(centerPos.north()).is(Blocks.END_STONE) ||
                    !level.getBlockState(centerPos.south()).is(Blocks.END_STONE)) return false;
        }

        level.destroyBlock(centerPos, false);
        level.destroyBlock(centerPos.below(), false);
        level.destroyBlock(centerPos.east(), false);
        level.destroyBlock(centerPos.west(), false);

        CrystalBoss boss = new CrystalBoss(EntityTypes.WARDEN, level);
        boss.teleportTo(centerPos.getX() + 0.5, centerPos.getY(), centerPos.getZ() + 0.5);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(centerPos), EntitySpawnReason.TRIGGERED, null);

        level.addFreshEntity(boss);
        return true;
    }

    public static InteractionResult eventSpawnBoss(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) return InteractionResult.PASS;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(Items.END_CRYSTAL)) {
            BlockPos clickedPos = hitResult.getBlockPos();
            if (world.getBlockState(clickedPos).is(Blocks.OBSIDIAN)) {
                if (trySpawnBoss((ServerLevel) world, clickedPos)) {
                    if (!player.isCreative()) heldItem.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}