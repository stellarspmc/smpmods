package fun.spmc.smpmod.minecraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

@Mixin(Enchantment.class)
public class MixinEnchantment {

    @ModifyReturnValue(method = "canEnchant", at = @At("RETURN"))
    public boolean smpmods$unbalancedEnchants(boolean original, ItemStack itemStack) {
        if (original) return true;

        if (minecraftServer == null) return false;
        var registry = minecraftServer.registryAccess().lookup(Registries.ENCHANTMENT);
        if (registry.isEmpty()) return false;

        Holder<Enchantment> enchant = registry.get().wrapAsHolder((Enchantment) (Object) this);

        if (itemStack.is(ItemTags.SWORDS)) return enchant.is(Enchantments.IMPALING);
        if (itemStack.is(ItemTags.AXES)) return
                enchant.is(Enchantments.LOOTING) ||
                enchant.is(Enchantments.FIRE_ASPECT) ||
                enchant.is(Enchantments.IMPALING);
        if (itemStack.is(Items.MACE)) return
                enchant.is(Enchantments.SHARPNESS) ||
                enchant.is(Enchantments.IMPALING);
        if (itemStack.is(ItemTags.SPEARS)) return enchant.is(Enchantments.IMPALING);
        if (itemStack.is(Items.BOW)) return
                enchant.is(Enchantments.LOOTING) ||
                enchant.is(Enchantments.PIERCING);
        if (itemStack.is(Items.TRIDENT)) return
                enchant.is(Enchantments.SHARPNESS) ||
                enchant.is(Enchantments.BANE_OF_ARTHROPODS) ||
                enchant.is(Enchantments.SMITE) ||
                enchant.is(Enchantments.LOOTING);
        if (itemStack.is(Items.CROSSBOW)) return
                enchant.is(Enchantments.FLAME) ||
                enchant.is(Enchantments.PUNCH) ||
                enchant.is(Enchantments.INFINITY) ||
                enchant.is(Enchantments.MENDING) ||
                enchant.is(Enchantments.POWER) ||
                enchant.is(Enchantments.LOOTING);

        return false;
    }

    @ModifyReturnValue(method = "areCompatible", at = @At("RETURN"))
    private static boolean smpmods$unbalancedCompatibleEnchants(boolean original, Holder<Enchantment> enchantment, Holder<Enchantment> other) {
        if (original) return true;
        if (smpmods$isBothInGroup(enchantment, other,
                Enchantments.PROTECTION,
                Enchantments.FIRE_PROTECTION,
                Enchantments.BLAST_PROTECTION,
                Enchantments.PROJECTILE_PROTECTION)) return true;

        if (smpmods$isBothInGroup(enchantment, other,
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.DENSITY,
                Enchantments.BREACH)) return true;

        if (smpmods$compareBothWays(enchantment, other, Enchantments.INFINITY, Enchantments.MENDING)) return true;

        return smpmods$compareBothWays(enchantment, other, Enchantments.PIERCING, Enchantments.MULTISHOT);
    }

    @Unique
    private static boolean smpmods$compareBothWays(Holder<Enchantment> a, Holder<Enchantment> b, ResourceKey<Enchantment> comparedA, ResourceKey<Enchantment> comparedB) {
        return (a.is(comparedA) && b.is(comparedB)) || (a.is(comparedB) && b.is(comparedA));
    }

    @SafeVarargs
    @Unique
    private static boolean smpmods$isBothInGroup(Holder<Enchantment> a, Holder<Enchantment> b, ResourceKey<Enchantment>... keys) {
        boolean aInGroup = false;
        boolean bInGroup = false;

        for (ResourceKey<Enchantment> key : keys) {
            if (a.is(key)) aInGroup = true;
            if (b.is(key)) bInGroup = true;
        }

        return aInGroup && bInGroup;
    }
}
