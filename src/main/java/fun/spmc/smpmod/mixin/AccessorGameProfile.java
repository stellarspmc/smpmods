package fun.spmc.smpmod.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GameProfile.class, remap = false)
public interface AccessorGameProfile {
    @Accessor("properties")
    @Mutable
    void setProperties(PropertyMap properties);
}
