package fun.spmc.smpmod.minecraft.mixin.bedrock;

import eu.pb4.graves.ui.GraveGui;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import eu.pb4.sgui.api.gui.BaseSlotGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.minecraft.grave.BedrockGraveUI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.geysermc.floodgate.api.FloodgateApi;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SimpleGui.class, remap = false)
public abstract class MixinSimpleGui extends BaseSlotGui {

    public MixinSimpleGui(ServerPlayer player, int size) {
        super(player, size);
    }

    @ModifyVariable(method = "setSlot(ILeu/pb4/sgui/api/elements/GuiElement;)V", at = @At("HEAD"), argsOnly = true, name = "element")
    private GuiElement smpmod$translateBedrockGraveSlot(GuiElement element) {
        if (element == null || this.player == null) return element;
        if (!(((Object) this) instanceof GraveGui)) return element; // mixin shenanigans

        if (FloodgateApi.getInstance().isFloodgatePlayer(this.player.getUUID())) {
            ItemStack originalStack = element.getItemStack();
            ItemStack translatedStack = BedrockGraveUI.translateForBedrock(originalStack);

            if (translatedStack != originalStack) {
                return new SimpleGuiElement(translatedStack, element.getGuiCallback());
            }
        }

        return element;
    }
}