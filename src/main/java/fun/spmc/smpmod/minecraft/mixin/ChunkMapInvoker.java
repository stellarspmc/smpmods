package fun.spmc.smpmod.minecraft.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface ChunkMapInvoker {
    @Invoker("removeEntity")
    void invokeRemoveEntity(Entity entity);

    @Invoker("addEntity")
    void invokeAddEntity(Entity entity);
}