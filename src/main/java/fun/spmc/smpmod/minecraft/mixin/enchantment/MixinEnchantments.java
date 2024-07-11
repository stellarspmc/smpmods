package fun.spmc.smpmod.minecraft.mixin.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Enchantments.class)
public class MixinEnchantments {

    @Unique
    private static Enchantment.Definition recreateDefinition(Enchantment.Definition oldDef, int lvl) {
        return new Enchantment.Definition(
                oldDef.comp_2506(),
                oldDef.comp_2507(),
                oldDef.comp_2508(),
                lvl,
                oldDef.comp_2510(),
                oldDef.comp_2511(),
                oldDef.comp_2512(),
                oldDef.comp_2513()
        );
    }

    /*@Inject(method = "register", at = @At("HEAD"))
    private static void register(Registerable<Enchantment> registerable, RegistryKey<Enchantment> registryKey, Enchantment.Builder builder, CallbackInfo ci) {
        String identifier = registryKey.getValue().getPath();
        switch (identifier) {
            case "protection":
            case "fire_protection":
            case "blast_protection":
            case "projectile_protection":
            case "feather_falling":
            case "punch":
                builder.definition.comp_2509 = 5;
                break;
            case "looting":
            case "sharpness":
            case "smite":
            case "bane_of_arthropods":
            case "fire_aspect":
            case "sweeping_edge":
            case "knockback":
                builder.definition.comp_2509 = 7;
                break;
            case "efficiency":
            case "fortune":
            case "power":
                builder.definition.comp_2509 = 9;
                break;
            case "multishot":
                builder.definition.comp_2509 = 15;
        }
        builder.definition = recreateDefinition(builder.definition, 15);
    }*/

    /**
     * @author tcfplayz
     * @reason test
     */
    @Overwrite
    private static void register(Registerable<Enchantment> registerable, RegistryKey<Enchantment> registryKey, Enchantment.Builder builder) {
        registerable.register(registryKey, new Enchantment.Builder(recreateDefinition(builder.definition, 15)).build(registryKey.getValue()));
    }
}
