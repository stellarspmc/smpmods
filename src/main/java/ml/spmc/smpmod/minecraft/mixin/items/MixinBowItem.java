package ml.spmc.smpmod.minecraft.mixin.items;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BowItem.class)
public class MixinBowItem {

    @Shadow
    public static float getPullProgress(int i) {
        return 1;
    }
    @Shadow
    public int getMaxUseTime(ItemStack itemStack) {
        return 72000;
    }

    /**
     * @author tcfplayz
     * @reason add multishot into bows
     */
    @Overwrite
    public void onStoppedUsing(ItemStack itemStack, World world, LivingEntity livingEntity, int i) {
        if (livingEntity instanceof PlayerEntity playerEntity) {
            boolean creative = playerEntity.getAbilities().creativeMode;
            boolean infArrow = creative || EnchantmentHelper.getLevel(Enchantments.INFINITY, itemStack) > 0;
            ItemStack projectileType = playerEntity.getProjectileType(itemStack);
            if (!projectileType.isEmpty() || infArrow) {
                if (projectileType.isEmpty()) projectileType = new ItemStack(Items.ARROW);

                int j = this.getMaxUseTime(itemStack) - i;
                float progress = getPullProgress(j);
                if (!(progress < 0.1)) {
                    boolean bl2 = infArrow && projectileType.isOf(Items.ARROW);
                    ArrowItem arrowItem = (ArrowItem)(projectileType.getItem() instanceof ArrowItem ? projectileType.getItem() : Items.ARROW);
                    PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(world, projectileType, playerEntity);
                    persistentProjectileEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0, progress * 3, 1);

                    if (progress == 1) persistentProjectileEntity.setCritical(true);

                    int power = EnchantmentHelper.getLevel(Enchantments.POWER, itemStack);
                    if (power > 0) persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() + power * 0.5 + 0.5);

                    int punch = EnchantmentHelper.getLevel(Enchantments.PUNCH, itemStack);
                    if (punch > 0) persistentProjectileEntity.setPunch(punch);

                    int flame = EnchantmentHelper.getLevel(Enchantments.FLAME, itemStack);
                    if (flame > 0) persistentProjectileEntity.setOnFireFor(flame * 100);

                    itemStack.damage(1, playerEntity, (playerEntity2) -> playerEntity2.sendToolBreakStatus(playerEntity.getActiveHand()));

                    if (bl2 || creative && (projectileType.isOf(Items.SPECTRAL_ARROW) || projectileType.isOf(Items.TIPPED_ARROW))) persistentProjectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;

                    int multishot = EnchantmentHelper.getLevel(Enchantments.MULTISHOT, itemStack);
                    if (multishot > 0) {
                        for (int a = 0; a == (multishot * 5); a++) {
                            world.spawnEntity(persistentProjectileEntity);
                        }
                    } else world.spawnEntity(persistentProjectileEntity);

                    world.playSound(playerEntity, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + progress * 0.5F);
                    if (!bl2 && !creative) {
                        projectileType.decrement(1);
                        if (projectileType.isEmpty()) playerEntity.getInventory().removeOne(projectileType);
                    }
                }
            }
        }
    }
}
