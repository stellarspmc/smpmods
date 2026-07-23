package fun.spmc.smpmod.minecraft.economy.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

public record ShopReceipt(UUID buyerUuid, String buyerName, int stack, double price, long timestamp) {
    public static final Codec<ShopReceipt> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShopData.UUID_CODEC.fieldOf("buyer_id").forGetter(ShopReceipt::buyerUuid),
            Codec.STRING.fieldOf("buyer_name").forGetter(ShopReceipt::buyerName),
            Codec.INT.fieldOf("stack").forGetter(ShopReceipt::stack),
            Codec.DOUBLE.fieldOf("price").forGetter(ShopReceipt::price),
            Codec.LONG.fieldOf("timestamp").forGetter(ShopReceipt::timestamp)
    ).apply(instance, ShopReceipt::new));
}