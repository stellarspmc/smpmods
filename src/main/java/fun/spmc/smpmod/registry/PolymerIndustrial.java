package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.industrial.mineral.BaseMineralItem;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class PolymerIndustrial {
    private static void registerMineral(String id, Item vanillaModel, TextColor textColor) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, vanillaModel, Component.literal(MessageUtils.formatName(id)).withColor(textColor))); }
    private static void registerMineral(String id, Item vanillaModel, Component name) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, vanillaModel, name)); }
    private static void registerKarat(String id, int karat, TextColor goldColor) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, Items.GOLD_INGOT, Component.literal("Gold Ingot ").withColor(goldColor).append(Component.literal("(" + karat + "-Carat)").withColor(TextColor.fromRgb(0xAAAAAA))))); }
    private static void registerHead(String id, String texture, TextColor textColor) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, texture, Component.literal(MessageUtils.formatName(id)).withColor(textColor))); }
    private static TextColor hex(int hex) { return TextColor.fromRgb(hex); }

    protected static void registerMinerals() {
        registerMineral("sifted_dust", Items.GUNPOWDER, hex(0x706E6B));
        registerMineral("iron_dust", Items.GUNPOWDER, hex(0xD8AF93));
        registerMineral("gold_dust", Items.GLOWSTONE_DUST, hex(0xFDF55F));
        registerMineral("tin_dust", Items.SUGAR, hex(0xD0D7DC));
        registerMineral("copper_dust", Items.GLOWSTONE_DUST, hex(0xE07A5F));
        registerMineral("silver_dust", Items.SUGAR, hex(0xE5ECEF));
        registerMineral("aluminum_dust", Items.SUGAR, hex(0xB0C4DE));
        registerMineral("lead_dust", Items.GUNPOWDER, hex(0x4A4E5A));
        registerMineral("zinc_dust", Items.SUGAR, hex(0xBAC8CE));
        registerMineral("magnesium_dust", Items.SUGAR, hex(0xEAECEE));

        registerMineral("tin_ingot", Items.IRON_INGOT, hex(0xD0D7DC));
        registerMineral("silver_ingot", Items.IRON_INGOT, hex(0xF0F4F8));
        registerMineral("aluminum_ingot", Items.IRON_INGOT, hex(0xD3DFEE));
        registerMineral("lead_ingot", Items.IRON_INGOT, hex(0x5A5E6B));
        registerMineral("zinc_ingot", Items.IRON_INGOT, hex(0xC5D3DC));
        registerMineral("magnesium_ingot", Items.IRON_INGOT, hex(0xF2F4F5));

        registerMineral("steel_ingot", Items.IRON_INGOT, hex(0x7B8C9E));
        registerMineral("bronze_ingot", Items.BRICK, hex(0xCD7F32));
        registerMineral("duralumin_ingot", Items.IRON_INGOT, hex(0xB8C3D0));
        registerMineral("billon_ingot", Items.IRON_INGOT, hex(0x9A9188));
        registerMineral("brass_ingot", Items.GOLD_INGOT, hex(0xE1C158));
        registerMineral("aluminum_brass_ingot", Items.GOLD_INGOT, hex(0xE8D573));
        registerMineral("aluminum_bronze_ingot", Items.GOLD_INGOT, hex(0xDDA15E));
        registerMineral("corinthian_bronze_ingot", Items.GOLD_INGOT, hex(0xB56576));
        registerMineral("solder_ingot", Items.IRON_INGOT, hex(0x9AA5B1));
        registerMineral("damascus_steel_ingot", Items.IRON_INGOT, hex(0x4C566A));
        registerMineral("ferrosilicon", Items.IRON_INGOT, hex(0x8892B0));
        registerMineral("redstone_alloy", Items.BRICK, hex(0xE63946));
        registerMineral("nickel_ingot", Items.IRON_INGOT, hex(0xA8B2A2));
        registerMineral("cobalt_ingot", Items.IRON_INGOT, hex(0x2B6CB0));
        registerMineral("gilded_iron", Items.GOLD_INGOT, Component.literal("Gilded Iron").withColor(hex(0xD4AF37)).withStyle(ChatFormatting.BOLD));
        registerMineral("hardened_metal_ingot", Items.IRON_INGOT, Component.literal("Hardened Metal").withColor(hex(0x2E3440)).withStyle(ChatFormatting.BOLD));
        registerMineral("reinforced_alloy_ingot", Items.IRON_INGOT, Component.literal("Reinforced Alloy Ingot").withColor(hex(0x434C5E)).withStyle(ChatFormatting.BOLD));

        registerKarat("gold_4k",  4,  hex(0xD8CC9B));
        registerKarat("gold_6k",  6,  hex(0xDFD28D));
        registerKarat("gold_8k",  8,  hex(0xE7D87E));
        registerKarat("gold_10k", 10, hex(0xEFDF6F));
        registerKarat("gold_12k", 12, hex(0xF7E560));
        registerKarat("gold_14k", 14, hex(0xFFEB50));
        registerKarat("gold_16k", 16, hex(0xFFE23F));
        registerKarat("gold_18k", 18, hex(0xFFD82E));
        registerKarat("gold_20k", 20, hex(0xFFCE1B));
        registerKarat("gold_22k", 22, hex(0xFFC308));
        registerKarat("gold_24k", 24, hex(0xFFB700));

        registerHead("carbon", "8b3a095b6b81e6b9853a19324eedf0bb9349417258dd173b8eff87a087aa", hex(0x3E424B));
        registerHead("compressed_carbon", "321d495165748d3116f99d6b5bd5d42eb8ba592bcdfad37fd95f9b6c04a3b", hex(0x22252A));
        registerHead("carbon_chunk", "321d495165748d3116f99d6b5bd5d42eb8ba592bcdfad37fd95f9b6c04a3b", hex(0x111317));
    }
}
