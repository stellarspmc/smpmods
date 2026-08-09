package fun.spmc.smpmod.fishing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.registry.PolymerFishes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class FishTracker extends SavedData {
    private static final Codec<Map<UUID, List<String>>> UNLOCKED_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.list(Codec.STRING));

    public static final Codec<FishTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UNLOCKED_CODEC.fieldOf("unlocked").forGetter(FishTracker::getUnlocked)
    ).apply(instance, FishTracker::new));

    public static final SavedDataType<FishTracker> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmod", "fish_tracker"),
            FishTracker::new,
            CODEC,
            DataFixTypes.PLAYER
    );

    private final Map<UUID, List<String>> fishUnlocked;

    public FishTracker(Map<UUID, List<String>> fishUnlocked) { this.fishUnlocked = fishUnlocked; }
    public FishTracker() { this(new HashMap<>()); }
    public Map<UUID, List<String>> getUnlocked() { return fishUnlocked; }
    public static FishTracker get() { return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE); }
    private boolean checkAlreadyAdded(UUID id, String fish) { return getUnlockedFish(id).contains(fish); }
    public List<String> getUnlockedFish(UUID id) { return fishUnlocked.getOrDefault(id, new ArrayList<>()); }

    public void registerPlayer(UUID uuid) {
        if (!fishUnlocked.containsKey(uuid)) fishUnlocked.put(uuid, new ArrayList<>());
        this.setDirty();
    }

    public void addFish(UUID id, String fish) {
        List<String> set = getUnlockedFish(id);
        if (BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("smpmod", fish)).isEmpty()) return;
        if (checkAlreadyAdded(id, fish)) {
            set.add(fish);
            fishUnlocked.replace(id, set);
            setDirty();
        }
    }

    public static int openFishIndexMenu(ServerPlayer player) {
        openFishIndexMenu(player, 0);
        return 1;
    }

    public static void openFishIndexMenu(ServerPlayer player, int page) {
        List<Item> allFish = PolymerFishes.getAllFish();
        int maxPages = Math.max(1, (int) Math.ceil((double) allFish.size() / 45));
        int currentPage = Math.clamp(page, 0, maxPages - 1);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Fish Codex (" + (currentPage + 1) + "/" + maxPages + ")"));

        refreshGui(gui, player, currentPage, maxPages);
        gui.open();
    }

    private static void refreshGui(SimpleGui gui, ServerPlayer player, int page, int maxPages) {
        List<Item> allFish = PolymerFishes.FISH;
        List<String> unlockedList = FishTracker.get().getUnlockedFish(player.getUUID());

        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, allFish.size());

        for (int i = 0; i < 45; i++) {
            int fishIndex = startIndex + i;

            if (fishIndex < endIndex) {
                Item fishItem = allFish.get(fishIndex);
                String fishId = BuiltInRegistries.ITEM.getKey(fishItem).getPath();

                boolean isUnlocked = unlockedList.contains(fishId);

                if (isUnlocked) gui.setSlot(i, new GuiElementBuilder(fishItem).addLoreLine(Component.literal("✔ Unlocked").withColor(TextColor.fromRgb(0x55FF55))));
                else gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("???").withColor(TextColor.fromRgb(0xAAAAAA))).addLoreLine(Component.literal("Catch this fish to unlock!").withColor(TextColor.fromRgb(0xFF5555))));
            } else gui.setSlot(i, new GuiElementBuilder(Items.AIR));
        }

        for (int slot = 45; slot < 54; slot++) gui.setSlot(slot, new GuiElementBuilder(Items.STAINED_GLASS_PANE.lightGray()).setName(Component.literal("")));
        if (page > 0) gui.setSlot(45, new GuiElementBuilder(Items.ARROW).setName(Component.literal("← Previous Page").withColor(TextColor.fromRgb(0xFFFF55))).setCallback((_) -> openFishIndexMenu(player, page - 1)));
        gui.setSlot(49, new GuiElementBuilder(Items.PAPER).setName(Component.literal("Page " + (page + 1) + " of " + maxPages).withColor(TextColor.fromRgb(0xFFFFFF))).addLoreLine(Component.literal("Unlocked: " + unlockedList.size() + " / " + allFish.size()).withColor(TextColor.fromRgb(0xAAFFAA))));
        if (page < maxPages - 1) gui.setSlot(53, new GuiElementBuilder(Items.ARROW).setName(Component.literal("Next Page →").withColor(TextColor.fromRgb(0xFFFF55))).setCallback((_) -> openFishIndexMenu(player, page + 1)));
    } // TODO: fix
}