package fun.spmc.smpmod.mobs;

import fun.spmc.smpmod.utils.ServerMob;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class CrystalMaster implements ServerMob {
    @Override
    public EntityType<? extends LivingEntity> getEntityType() {
        return null;
    }

    @Override
    public double entitySpawnRate() {
        return 0;
    }

    @Override
    public Component getEntityName() {
        return null;
    }

    @Override
    public void setHead(LivingEntity entity) {

    }

    @Override
    public void setChest(LivingEntity entity) {

    }

    @Override
    public void setLegs(LivingEntity entity) {

    }

    @Override
    public void setBoots(LivingEntity entity) {

    }

    @Override
    public void setItems(LivingEntity entity) {

    }

    @Override
    public void setEffects(LivingEntity entity) {

    }

    @Override
    public void setAttributes(LivingEntity entity) {

    }

    public void spawnMob(ServerLevel level) {
        /**public void setPlacedBy(final Level level, final BlockPos pos, final BlockState state, final @Nullable LivingEntity by, final ItemStack itemStack) {
            checkSpawn(level, pos);
        }

        public static void checkSpawn(final Level level, final BlockPos pos) {
            BlockEntity var3 = level.getBlockEntity(pos);
            if (var3 instanceof SkullBlockEntity placedSkull) {
                checkSpawn(level, pos, placedSkull);
            }

        }

        public static void checkSpawn(final Level level, final BlockPos pos, final SkullBlockEntity placedSkull) {
            if (!level.isClientSide()) {
                BlockState blockState = placedSkull.getBlockState();
                boolean correctBlock = blockState.is(Blocks.WITHER_SKELETON_SKULL) || blockState.is(Blocks.WITHER_SKELETON_WALL_SKULL);
                if (correctBlock && pos.getY() >= level.getMinY() && level.getDifficulty() != Difficulty.PEACEFUL) {
                    BlockPattern.BlockPatternMatch match = getOrCreateWitherFull().find(level, pos);
                    if (match != null) {
                        WitherBoss witherBoss = (WitherBoss) EntityTypes.WITHER.create(level, EntitySpawnReason.TRIGGERED);
                        if (witherBoss != null) {
                            CarvedPumpkinBlock.clearPatternBlocks(level, match);
                            BlockPos spawnPos = match.getBlock(1, 2, 0).getPos();
                            witherBoss.snapTo((double)spawnPos.getX() + (double)0.5F, (double)spawnPos.getY() + 0.55, (double)spawnPos.getZ() + (double)0.5F, match.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                            witherBoss.yBodyRot = match.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                            witherBoss.makeInvulnerable();

                            for(ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, witherBoss.getBoundingBox().inflate((double)50.0F))) {
                                CriteriaTriggers.SUMMONED_ENTITY.trigger(player, witherBoss);
                            }

                            level.addFreshEntity(witherBoss);
                            CarvedPumpkinBlock.updatePatternBlocks(level, match);
                        }

                    }
                }
            }
        }*/ // placebo for later
    }
}
