package fun.spmc.smpmod.industrial.mineral;

import fun.spmc.smpmod.misc.NPCData;
import fun.spmc.smpmod.utils.BasePolymerItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;

public class BaseMineralItem extends BasePolymerItem {
    private final String headTexture;
    private final Component id;

    public BaseMineralItem(Properties properties, Item vanillaItem, Component id) {
        super(properties, vanillaItem);
        this.headTexture = null;
        this.id = id;
    }

    // heads
    public BaseMineralItem(Properties properties, String headTexture, Component id) {
        super(properties, Items.PLAYER_HEAD);
        this.headTexture = headTexture;
        this.id = id;
    }

    @Override
    public Component buildName(ItemStack stack) {
        return Component.empty().append(id).withStyle(style -> style.withItalic(false));
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        return List.of();
    }

    @Override
    public void modifyItem(ItemStack stack, ItemStack stackData) {
        if (headTexture != null) {
            stack.set(DataComponents.PROFILE, NPCData.createCustomProfile("PolymerItem", UUID.randomUUID(), headTexture));
        }
    }
}
