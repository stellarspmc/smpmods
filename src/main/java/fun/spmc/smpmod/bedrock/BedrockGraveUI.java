package fun.spmc.smpmod.bedrock;

import eu.pb4.graves.registry.GravesRegistry;
import eu.pb4.graves.registry.IconItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class BedrockGraveUI {

    public static ItemStack translateForBedrock(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;
        if (stack.is(GravesRegistry.ICON_ITEM)) return translateIconItem(stack);
        if (!stack.is(Items.PLAYER_HEAD)) return stack;

        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return stack;
        String name = customName.getString().toLowerCase();

        ItemLore lore = stack.get(DataComponents.LORE);
        List<Component> loreLines = lore != null ? lore.lines() : List.of();

        // Translate based on Universal Graves button names
        ItemStack translated = translateUniversalGravesButton(name, customName, loreLines);
        if (translated != null) {
            return translated;
        }

        // Fallback: try generic button name matching
        translated = translateGenericButton(name, customName, loreLines);
        if (translated != null) {
            return translated;
        }

        // Default fallback for unidentified heads (preserves custom name & lore)
        return createItem(Items.PAPER.getDefaultInstance(), customName, loreLines);
    }

    /**
     * Translates Universal Graves IconItem based on its TEXTURE component.
     */
    private static ItemStack translateIconItem(ItemStack stack) {
        IconItem.Texture texture = stack.get(IconItem.TEXTURE);
        if (texture == null) texture = IconItem.Texture.INVALID;

        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        ItemLore lore = stack.get(DataComponents.LORE);
        List<Component> loreLines = lore != null ? lore.lines() : List.of();

        return switch (texture) {
            case NEXT_PAGE -> createItem(Items.STAINED_GLASS_PANE.green().getDefaultInstance(),
                    customName != null ? customName : Component.literal("Next Page →").withStyle(ChatFormatting.GREEN),
                    loreLines);
            case NEXT_PAGE_BLOCKED -> createItem(Items.STAINED_GLASS_PANE.gray().getDefaultInstance(),
                    customName != null ? customName : Component.literal("Next Page →").withStyle(ChatFormatting.DARK_GRAY),
                    loreLines);
            case PREVIOUS_PAGE -> createItem(Items.STAINED_GLASS_PANE.red().getDefaultInstance(),
                    customName != null ? customName : Component.literal("← Previous Page").withStyle(ChatFormatting.RED),
                    loreLines);
            case PREVIOUS_PAGE_BLOCKED -> createItem(Items.STAINED_GLASS_PANE.gray().getDefaultInstance(),
                    customName != null ? customName : Component.literal("← Previous Page").withStyle(ChatFormatting.DARK_GRAY),
                    loreLines);
            case QUICK_PICKUP -> createItem(Items.CHEST.getDefaultInstance(),
                    customName != null ? customName : Component.literal("Take All Items").withStyle(ChatFormatting.GOLD),
                    loreLines);
            case BREAK_GRAVE -> createItem(Items.TNT.getDefaultInstance(),
                    customName != null ? customName : Component.literal("Break Grave").withStyle(ChatFormatting.RED),
                    loreLines);
            case REMOVE_PROTECTION -> createItem(Items.SHIELD.getDefaultInstance(),
                    customName != null ? customName : Component.literal("Remove Protection").withStyle(ChatFormatting.RED),
                    loreLines);
            case TATER -> createItem(Items.POTATO.getDefaultInstance(),
                    customName != null ? customName : Component.literal("Tater"),
                    loreLines);
            default -> createItem(Items.PAPER.getDefaultInstance(),
                    customName != null ? customName : Component.literal("Button"),
                    loreLines);
        };
    }

    /**
     * Translates Universal Graves specific buttons.
     */
    private static ItemStack translateUniversalGravesButton(String nameLower, Component originalName, List<Component> lore) {
        // === NAVIGATION BUTTONS ===
        if (nameLower.contains("next page")) {
            boolean disabled = nameLower.startsWith("§8") || (originalName.getStyle().getColor() != null
                    && originalName.getStyle().getColor().toString().equals("dark_gray"));
            if (disabled) {
                return createItem(Items.STAINED_GLASS_PANE.gray().getDefaultInstance(),
                        Component.literal("Next Page →").withStyle(ChatFormatting.DARK_GRAY), lore);
            }
            return createItem(Items.STAINED_GLASS_PANE.green().getDefaultInstance(),
                    Component.literal("Next Page →").withStyle(ChatFormatting.GREEN), lore);
        }

        if (nameLower.contains("previous page")) {
            boolean disabled = nameLower.startsWith("§8") || (originalName.getStyle().getColor() != null
                    && originalName.getStyle().getColor().toString().equals("dark_gray"));
            if (disabled) {
                return createItem(Items.STAINED_GLASS_PANE.gray().getDefaultInstance(),
                        Component.literal("← Previous Page").withStyle(ChatFormatting.DARK_GRAY), lore);
            }
            return createItem(Items.STAINED_GLASS_PANE.red().getDefaultInstance(),
                    Component.literal("← Previous Page").withStyle(ChatFormatting.RED), lore);
        }

        // === ACTION BUTTONS ===
        // "Take All Items" / Quick Pickup
        if (nameLower.contains("take all") || nameLower.contains("quick pickup") || nameLower.contains("equip")) {
            return createItem(Items.CHEST.getDefaultInstance(),
                    Component.literal("Take All Items").withStyle(ChatFormatting.GOLD), lore);
        }

        // "Teleport to Grave"
        if (nameLower.contains("teleport") || nameLower.contains("tp")) {
            return createItem(Items.ENDER_PEARL.getDefaultInstance(),
                    Component.literal("Teleport to Grave").withStyle(ChatFormatting.LIGHT_PURPLE), lore);
        }

        // "Fetch Grave" (Summon grave to player)
        if (nameLower.contains("fetch") || nameLower.contains("summon")) {
            boolean isConfirm = nameLower.contains("confirm") || nameLower.contains("sure");
            return createItem(isConfirm ? Items.RECOVERY_COMPASS.getDefaultInstance() : Items.COMPASS.getDefaultInstance(),
                    isConfirm ? Component.literal("Confirm Fetch Grave").withStyle(ChatFormatting.GREEN)
                            : Component.literal("Fetch Grave").withStyle(ChatFormatting.AQUA), lore);
        }

        // "Unlock Grave"
        if (nameLower.contains("unlock")) {
            return createItem(Items.TRIPWIRE_HOOK.getDefaultInstance(),
                    Component.literal("Unlock Grave").withStyle(ChatFormatting.YELLOW), lore);
        }

        // "Break Grave" / Destroy
        if (nameLower.contains("break") || nameLower.contains("destroy")) {
            boolean isConfirm = nameLower.contains("confirm") || nameLower.contains("sure");
            return createItem(isConfirm ? Items.REDSTONE_BLOCK.getDefaultInstance() : Items.TNT.getDefaultInstance(),
                    isConfirm ? Component.literal("Confirm Break Grave").withStyle(ChatFormatting.DARK_RED)
                            : Component.literal("Break Grave").withStyle(ChatFormatting.RED), lore);
        }

        // "Remove Protection"
        if (nameLower.contains("remove protection") || nameLower.contains("unprotect")) {
            boolean isConfirm = nameLower.contains("confirm") || nameLower.contains("sure");
            return createItem(isConfirm ? Items.SHIELD.getDefaultInstance() : Items.IRON_BARS.getDefaultInstance(),
                    isConfirm ? Component.literal("Confirm Remove Protection").withStyle(ChatFormatting.RED)
                            : Component.literal("Remove Protection").withStyle(ChatFormatting.GOLD), lore);
        }

        // "Grave Info" / Compass Creation Icon
        if (nameLower.contains("info") || nameLower.contains("location") || nameLower.contains("coordinates")) {
            return createItem(Items.BOOK.getDefaultInstance(),
                    Component.literal("Grave Info").withStyle(ChatFormatting.AQUA), lore);
        }

        return null;
    }

    private static ItemStack translateGenericButton(String nameLower, Component originalName, List<Component> lore) {
        // Navigation - generic
        if (nameLower.contains("next") || nameLower.contains("forward") || nameLower.contains(">>") || nameLower.contains("→")) {
            return createItem(Items.STAINED_GLASS_PANE.lime().getDefaultInstance(),
                    Component.literal("Next →").withStyle(ChatFormatting.GREEN), lore);
        }
        if (nameLower.contains("previous") || nameLower.contains("prev") || nameLower.contains("<<") || nameLower.contains("←")) {
            return createItem(Items.STAINED_GLASS_PANE.red().getDefaultInstance(),
                    Component.literal("← Previous").withStyle(ChatFormatting.RED), lore);
        }
        if (nameLower.contains("back")) {
            return createItem(Items.ARROW.getDefaultInstance(),
                    Component.literal("← Back").withStyle(ChatFormatting.YELLOW), lore);
        }

        // Close/exit
        if (nameLower.contains("close") || nameLower.contains("exit") || nameLower.contains("cancel")) {
            return createItem(Items.BARRIER.getDefaultInstance(),
                    Component.literal("Close").withStyle(ChatFormatting.RED), lore);
        }

        // Confirm/accept
        if (nameLower.contains("confirm") || nameLower.contains("click again")) {
            return createItem(Items.DYE.lime().getDefaultInstance(),
                    Component.literal("Confirm").withStyle(ChatFormatting.GREEN), lore);
        }

        // Collect/take
        if (nameLower.contains("collect") || nameLower.contains("take") || nameLower.contains("retrieve") || nameLower.contains("claim")) {
            return createItem(Items.CHEST.getDefaultInstance(),
                    Component.literal("Collect Items").withStyle(ChatFormatting.GOLD), lore);
        }

        // Delete/remove/destroy
        if (nameLower.contains("delete") || nameLower.contains("destroy")) {
            return createItem(Items.LAVA_BUCKET.getDefaultInstance(),
                    Component.literal("Delete").withStyle(ChatFormatting.DARK_RED), lore);
        }

        // Info/help
        if (nameLower.contains("info") || nameLower.contains("help") || nameLower.contains("?")) {
            return createItem(Items.BOOK.getDefaultInstance(),
                    Component.literal("Info").withStyle(ChatFormatting.AQUA), lore);
        }

        // Player grave representation (show as skeleton skull)
        if (nameLower.contains("grave") || nameLower.contains("death") || nameLower.contains("died")) {
            return createItem(Items.SKELETON_SKULL.getDefaultInstance(), originalName, lore);
        }

        // Protection related
        if (nameLower.contains("protect") || nameLower.contains("lock") || nameLower.contains("unlock")) {
            return createItem(Items.SHIELD.getDefaultInstance(), originalName, lore);
        }

        return null;
    }

    private static ItemStack createItem(ItemStack base, Component name, List<Component> lore) {
        ItemStack stack = base.copy();
        stack.set(DataComponents.CUSTOM_NAME, name);
        if (lore != null && !lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
        return stack;
    }

    private static ItemStack createItem(ItemStack base, Component name) {
        return createItem(base, name, List.of());
    }
}