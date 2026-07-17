package fun.spmc.smpmod.minecraft.economy.shop;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopManager extends SavedData {

    private static final Map<UUID, ShopData> shopsByInteractionUuid = new HashMap<>();
    private static final Map<BlockPos, ShopData> shopsByBarrelPos = new HashMap<>();
    private static final Map<UUID, ShopData> shopsById = new HashMap<>();

    public static final Codec<ShopManager> CODEC = ShopData.CODEC.listOf().xmap(
            shops -> {
                ShopManager manager = new ShopManager();
                shopsByInteractionUuid.clear();
                shopsByBarrelPos.clear();
                shopsById.clear();
                for (ShopData shop : shops) manager.registerShop(shop);
                return manager;
            },
            _ -> List.copyOf(shopsById.values())
    );

    public static final SavedDataType<ShopManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmods", "shops"),
            ShopManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public ShopManager() {}

    private void registerShop(ShopData data) {
        shopsById.put(data.getShopId(), data);
        shopsByInteractionUuid.put(data.getInteractionEntityUuid(), data);
        shopsByBarrelPos.put(data.getBarrelPos(), data);
    }

    public static ShopData getByInteraction(UUID entityUuid) {
        return shopsByInteractionUuid.get(entityUuid);
    }

    public static ShopData getByPos(BlockPos pos) {
        return shopsByBarrelPos.get(pos);
    }

    public static void createShop(UUID owner, BlockPos pos, double price, ItemStack sellItem, ServerLevel level) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        Display.ItemDisplay itemDisplay = EntityTypes.ITEM_DISPLAY.create(level, EntitySpawnReason.TRIGGERED);
        if (itemDisplay != null) {
            itemDisplay.setPos(x, y + 0.35, z);
            itemDisplay.setItemStack(sellItem.copy());
            itemDisplay.setTransformation(new Transformation(new Vector3f(0f), null, new Vector3f(0.5f),null));
            level.addFreshEntity(itemDisplay);
        }

        Display.TextDisplay textDisplay = EntityTypes.TEXT_DISPLAY.create(level, EntitySpawnReason.TRIGGERED);
        if (textDisplay != null) {
            textDisplay.setPos(x, y + 0.85, z);
            String label = String.format("%dx %s — $%.2f", sellItem.getCount(), sellItem.getHoverName().getString(), price);
            textDisplay.setText(Component.literal(label));
            textDisplay.setBillboardConstraints(Display.BillboardConstraints.CENTER);
            level.addFreshEntity(textDisplay);
        }

        Interaction interaction = EntityTypes.INTERACTION.create(level, EntitySpawnReason.TRIGGERED);
        if (interaction != null) {
            interaction.setPos(x, y, z); // Centered directly on top of the container
            interaction.setHeight(1.0f);
            interaction.setWidth(1.0f);
            level.addFreshEntity(interaction);
        }

        if (itemDisplay == null || textDisplay == null || interaction == null) return;

        ShopData data = new ShopData(
                UUID.randomUUID(),
                owner,
                pos,
                interaction.getUUID(),
                itemDisplay.getUUID(),
                textDisplay.getUUID(),
                sellItem.copyWithCount(1),
                sellItem.getCount(),
                price
        );

        ShopManager manager = get(level);
        manager.registerShop(data);
        manager.setDirty();
    }

    public static void removeShop(ShopData shop, ServerLevel level) {
        if (shop == null) return;

        shop.destroyShop(level);

        shopsByInteractionUuid.remove(shop.getInteractionEntityUuid());
        shopsByBarrelPos.remove(shop.getBarrelPos());
        shopsById.remove(shop.getShopId());

        get(level).setDirty();
    }

    public static ShopManager get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }
}