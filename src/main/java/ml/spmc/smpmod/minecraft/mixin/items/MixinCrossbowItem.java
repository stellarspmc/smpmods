package ml.spmc.smpmod.minecraft.mixin.items;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class MixinCrossbowItem {

    @Shadow
    protected static boolean loadProjectile(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, boolean bl2) {return false;}
    @Shadow
    protected static List<ItemStack> getProjectiles(ItemStack itemStack) {return null;}
    @Shadow
    protected static void shoot(World world, LivingEntity livingEntity, Hand hand, ItemStack itemStack, ItemStack itemStack2, float f, boolean bl, float g, float h, float i) {}
    @Shadow
    protected static float[] getSoundPitches(Random random) {return new float[]{};}
    @Shadow
    protected static void postShoot(World world, LivingEntity livingEntity, ItemStack itemStack) {}

        /**
         * @author tcfplayz
         * @reason to implement multishot to have more ammo
         */
    @Overwrite
    private static boolean loadProjectiles(LivingEntity livingEntity, ItemStack itemStack) {
        int i = EnchantmentHelper.getLevel(Enchantments.MULTISHOT, itemStack);
        int j = i > 0 ? i + 1 : 1;
        boolean bl = livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).getAbilities().creativeMode;
        ItemStack itemStack2 = livingEntity.getProjectileType(itemStack);
        ItemStack itemStack3 = itemStack2.copy();

        for(int k = 0; k < j; ++k) {
            if (k > 0) {
                itemStack2 = itemStack3.copy();
            }

            if (itemStack2.isEmpty() && bl) {
                itemStack2 = new ItemStack(Items.ARROW);
                itemStack3 = itemStack2.copy();
            }

            if (!loadProjectile(livingEntity, itemStack, itemStack2, k > 0, bl)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @author tcfplayz
     * @reason to implement multishot to have more ammo
     */
    @Overwrite
    public static void shootAll(World world, LivingEntity livingEntity, Hand hand, ItemStack itemStack, float f, float g) {
        List<ItemStack> list = getProjectiles(itemStack);
        float[] fs = getSoundPitches(livingEntity.getRandom());

        for(int i = 0; i < list.size(); ++i) {
            ItemStack itemStack2 = list.get(i);
            boolean bl = livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).getAbilities().creativeMode;
            if (!itemStack2.isEmpty()) {
                switch(i) {
                    case 1:
                        shoot(world, livingEntity, hand, itemStack, itemStack2, fs[i], bl, f, g, -10.0F);
                        break;
                    case 2:
                        shoot(world, livingEntity, hand, itemStack, itemStack2, fs[i], bl, f, g, 10.0F);
                        break;
                    default:
                        shoot(world, livingEntity, hand, itemStack, itemStack2, fs[i], bl, f, g, 0.0F);

                }
            }
        }

        postShoot(world, livingEntity, itemStack);
    }
}
