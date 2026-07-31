package fun.spmc.smpmod.minecraft.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @ModifyVariable(method = "tick", at = @At(value = "STORE"), name = "runs")
    private boolean smpmods$forceDimensionTick(boolean runs) {
        ServerLevel level = (ServerLevel) (Object) this;
        return runs || !level.getForceLoadedChunks().isEmpty();
    }
}