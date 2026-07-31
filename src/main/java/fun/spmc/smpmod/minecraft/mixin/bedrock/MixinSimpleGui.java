package fun.spmc.smpmod.minecraft.mixin.bedrock;

import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.minecraft.grave.BedrockGraveUI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SimpleGui.class, remap = false)
public abstract class MixinSimpleGui {

    /**@Shadow @Final protected ServerPlayer player;


    @ModifyVariable(
            method = "setSlot(ILnet/minecraft/world/inventory/Slot;)V",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false,
            name = "index")
    private int bedrock$translateGraveSlot(int index) {
        if (element == null || this.player == null) {
            return element;
        }

        if (!isBedrockPlayer(this.player)) {
            return element;
        }

        ItemStack originalStack = element.getItemStack();
        ItemStack translatedStack = BedrockGraveUI.translateForBedrock(originalStack);

        if (translatedStack != originalStack) {
            return new GuiElementInterface.Item(translatedStack, element.getCallback());
        }

        return element;
    }

    private boolean isBedrockPlayer(ServerPlayer player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID());
        } catch (Throwable ignored) {
            return false;
        }
    }**/
}