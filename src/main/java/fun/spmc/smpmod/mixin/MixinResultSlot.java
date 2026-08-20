package fun.spmc.smpmod.mixin;

import fun.spmc.smpmod.quest.QuestManager;
import fun.spmc.smpmod.quest.data.Quest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class MixinResultSlot {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onCraftItem(Player player, ItemStack carried, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            QuestManager.getQuests(serverPlayer).getActiveQuests().forEach(activeQuest -> {
                Quest quest = activeQuest.getQuest();
                if (quest.type() == Quest.QuestType.CRAFTING && quest.target().equals(BuiltInRegistries.ITEM.getKey(carried.getItem()))) activeQuest.increment(1);
            });
        }
    }
}