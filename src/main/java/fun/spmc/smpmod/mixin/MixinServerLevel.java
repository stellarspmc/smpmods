package fun.spmc.smpmod.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public class MixinServerLevel {
    @ModifyVariable(method = "tick", at = @At(value = "STORE"), name = "runs")
    private boolean smpmod$forceDimensionTick(boolean runs) {
        ServerLevel level = (ServerLevel) (Object) this;
        return runs || !level.getForceLoadedChunks().isEmpty();
    }

    // tODo: redo sleep warping, read ServerLevel.java#319 -> tick functions
}