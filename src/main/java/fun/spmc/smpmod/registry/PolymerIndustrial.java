package fun.spmc.smpmod.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.industrial.machine.SculkCompressorBlock;
import fun.spmc.smpmod.industrial.machine.entity.SculkCompressorEntity;
import fun.spmc.smpmod.industrial.mineral.BaseMineralItem;
import fun.spmc.smpmod.industrial.recipe.CompressorRecipe;
import fun.spmc.smpmod.industrial.recipe.SmelterRecipe;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolymerIndustrial {
    private static void registerMineral(String id, Item vanillaModel, TextColor textColor) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, vanillaModel, Component.literal(MessageUtils.formatName(id)).withColor(textColor))); }
    private static void registerMineral(String id, Item vanillaModel, Component name) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, vanillaModel, name)); }
    private static void registerKarat(String id, int karat, TextColor goldColor) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, Items.GOLD_INGOT, Component.literal("Gold Ingot ").withColor(goldColor).append(Component.literal("(" + karat + " - Carat)").withColor(TextColor.fromRgb(0xAAAAAA))))); }
    private static void registerHead(String id, String texture, TextColor textColor) { PolymerRegistry.createItem(id, properties -> new BaseMineralItem(properties, texture, Component.literal(MessageUtils.formatName(id)).withColor(textColor))); }

    public static RecipeType<CompressorRecipe> COMPRESSOR_TYPE;
    public static RecipeSerializer<CompressorRecipe> COMPRESSOR_SERIALIZER;

    public static RecipeType<SmelterRecipe> SMELTERY_TYPE;
    public static RecipeSerializer<SmelterRecipe> SMELTERY_SERIALIZER;

    public static BlockEntityType<SculkCompressorEntity> SCULK_ENTITY;

    protected static void registerMinerals() {
        registerMineral("sifted_dust", Items.GUNPOWDER, TextColor.fromRgb(0x706E6B));
        registerMineral("iron_dust", Items.GUNPOWDER, TextColor.fromRgb(0xD8AF93));
        registerMineral("gold_dust", Items.GLOWSTONE_DUST, TextColor.fromRgb(0xFDF55F));
        registerMineral("tin_dust", Items.SUGAR, TextColor.fromRgb(0xD0D7DC));
        registerMineral("copper_dust", Items.GLOWSTONE_DUST, TextColor.fromRgb(0xE07A5F));
        registerMineral("silver_dust", Items.SUGAR, TextColor.fromRgb(0xE5ECEF));
        registerMineral("aluminum_dust", Items.SUGAR, TextColor.fromRgb(0xB0C4DE));
        registerMineral("lead_dust", Items.GUNPOWDER, TextColor.fromRgb(0x4A4E5A));
        registerMineral("zinc_dust", Items.SUGAR, TextColor.fromRgb(0xBAC8CE));
        registerMineral("magnesium_dust", Items.SUGAR, TextColor.fromRgb(0xEAECEE));

        registerMineral("tin_ingot", Items.IRON_INGOT, TextColor.fromRgb(0xD0D7DC));
        registerMineral("silver_ingot", Items.IRON_INGOT, TextColor.fromRgb(0xF0F4F8));
        registerMineral("aluminum_ingot", Items.IRON_INGOT, TextColor.fromRgb(0xD3DFEE));
        registerMineral("lead_ingot", Items.IRON_INGOT, TextColor.fromRgb(0x5A5E6B));
        registerMineral("zinc_ingot", Items.IRON_INGOT, TextColor.fromRgb(0xC5D3DC));
        registerMineral("magnesium_ingot", Items.IRON_INGOT, TextColor.fromRgb(0xF2F4F5));

        registerMineral("silicon", Items.CLAY_BALL, TextColor.fromRgb(0x8B969B));

        registerMineral("steel_ingot", Items.IRON_INGOT, TextColor.fromRgb(0x7B8C9E));
        registerMineral("bronze_ingot", Items.BRICK, TextColor.fromRgb(0xCD7F32));
        registerMineral("duralumin_ingot", Items.IRON_INGOT, TextColor.fromRgb(0xB8C3D0));
        registerMineral("billon_ingot", Items.IRON_INGOT, TextColor.fromRgb(0x9A9188));
        registerMineral("brass_ingot", Items.GOLD_INGOT, TextColor.fromRgb(0xE1C158));
        registerMineral("aluminum_brass_ingot", Items.GOLD_INGOT, TextColor.fromRgb(0xE8D573));
        registerMineral("aluminum_bronze_ingot", Items.GOLD_INGOT, TextColor.fromRgb(0xDDA15E));
        registerMineral("corinthian_bronze_ingot", Items.GOLD_INGOT, TextColor.fromRgb(0xB56576));
        registerMineral("solder_ingot", Items.IRON_INGOT, TextColor.fromRgb(0x9AA5B1));
        registerMineral("damascus_steel_ingot", Items.IRON_INGOT, TextColor.fromRgb(0x4C566A));
        registerMineral("ferrosilicon", Items.IRON_INGOT, TextColor.fromRgb(0x8892B0));
        registerMineral("redstone_alloy", Items.BRICK, TextColor.fromRgb(0xE63946));
        registerMineral("nickel_ingot", Items.IRON_INGOT, TextColor.fromRgb(0xA8B2A2));
        registerMineral("cobalt_ingot", Items.IRON_INGOT, TextColor.fromRgb(0x2B6CB0));
        registerMineral("gilded_iron", Items.GOLD_INGOT, Component.literal("Gilded Iron").withColor(TextColor.fromRgb(0xD4AF37)).withStyle(ChatFormatting.BOLD));
        registerMineral("hardened_metal_ingot", Items.IRON_INGOT, Component.literal("Hardened Metal").withColor(TextColor.fromRgb(0x2E3440)).withStyle(ChatFormatting.BOLD));
        registerMineral("reinforced_alloy_ingot", Items.IRON_INGOT, Component.literal("Reinforced Alloy Ingot").withColor(TextColor.fromRgb(0x434C5E)).withStyle(ChatFormatting.BOLD));

        registerKarat("gold_4k",  4,  TextColor.fromRgb(0xD8CC9B));
        registerKarat("gold_6k",  6,  TextColor.fromRgb(0xDFD28D));
        registerKarat("gold_8k",  8,  TextColor.fromRgb(0xE7D87E));
        registerKarat("gold_10k", 10, TextColor.fromRgb(0xEFDF6F));
        registerKarat("gold_12k", 12, TextColor.fromRgb(0xF7E560));
        registerKarat("gold_14k", 14, TextColor.fromRgb(0xFFEB50));
        registerKarat("gold_16k", 16, TextColor.fromRgb(0xFFE23F));
        registerKarat("gold_18k", 18, TextColor.fromRgb(0xFFD82E));
        registerKarat("gold_20k", 20, TextColor.fromRgb(0xFFCE1B));
        registerKarat("gold_22k", 22, TextColor.fromRgb(0xFFC308));
        registerKarat("gold_24k", 24, TextColor.fromRgb(0xFFB700));

        registerMineral("compressed_nether_brick", Items.BRICK, TextColor.DARK_RED);
        registerHead("carbon", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNlZTQ2NTQwZjhhOTczMmJmY2Q2MjY1ZGFjNzNiNTA5YmZlNGYwMDk1Zjk1NWQ0ODNmZGEwOTNhZmY3MWQzNSJ9fX0=", TextColor.fromRgb(0x3E424B));
        registerHead("compressed_carbon", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIxZDQ5NTE2NTc0OGQzMTE2Zjk5ZDZiNWJkNWQ0MmViOGJhNTkyYmNkZmFkMzdmZDk1ZjliNmMwNGEzYiJ9fX0=", TextColor.fromRgb(0x22252A));
        registerHead("carbon_chunk", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIxZDQ5NTE2NTc0OGQzMTE2Zjk5ZDZiNWJkNWQ0MmViOGJhNTkyYmNkZmFkMzdmZDk1ZjliNmMwNGEzYiJ9fX0=", TextColor.fromRgb(0x111317));
        registerHead("nether_core", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQyNDEyMjhhZWY4NGUzNTY3MWNjMzEwMTE2ZDExYjhkMGYwODc2MjIwNTM1NTNjMGRjNGU0YTVkYWMzYzQwNSJ9fX0=", TextColor.DARK_RED);
    }
    public static void registerBlocks() {
        SCULK_ENTITY = PolymerRegistry.createBlockWithItemEntity("sculk_compressor", SculkCompressorBlock::new, BlockBehaviour.Properties.of(), SculkCompressorEntity::new, Items.SCULK_CATALYST);
        //PolymerRegistry.createBlockWithItem("smeltry", SmelteryBlock::new, BlockBehaviour.Properties.of(), Items.SMOKER);
    }
    public static void registerRecipes() {
        COMPRESSOR_TYPE = PolymerRegistry.registerRecipeType("compressing");
        COMPRESSOR_SERIALIZER = PolymerRegistry.registerRecipeSerializer("compressing", RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(CompressorRecipe::ingredient),
                Codec.INT.optionalFieldOf("count", 1).forGetter(CompressorRecipe::count),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(CompressorRecipe::result),
                Codec.INT.optionalFieldOf("process_time", 200).forGetter(CompressorRecipe::processTime) // Default 10 sec (200 ticks)
        ).apply(instance, CompressorRecipe::new)), StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, CompressorRecipe::ingredient,
                ByteBufCodecs.VAR_INT, CompressorRecipe::count,
                ItemStackTemplate.STREAM_CODEC, CompressorRecipe::result,
                ByteBufCodecs.VAR_INT, CompressorRecipe::processTime, CompressorRecipe::new
        ));

        SMELTERY_TYPE = PolymerRegistry.registerRecipeType("smelting");
        SMELTERY_SERIALIZER = PolymerRegistry.registerRecipeSerializer("smelting", RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(SmelterRecipe::ingredients),
                Codec.INT.optionalFieldOf("count", 1).forGetter(SmelterRecipe::count),
                ItemStack.CODEC.fieldOf("result").forGetter(SmelterRecipe::result),
                Codec.INT.optionalFieldOf("process_time", 200).forGetter(SmelterRecipe::processTime)
        ).apply(instance, SmelterRecipe::new)), StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), SmelterRecipe::ingredients,
                ByteBufCodecs.VAR_INT, SmelterRecipe::count,
                ItemStack.STREAM_CODEC, SmelterRecipe::result,
                ByteBufCodecs.VAR_INT, SmelterRecipe::processTime, SmelterRecipe::new
        ));
    }
}
