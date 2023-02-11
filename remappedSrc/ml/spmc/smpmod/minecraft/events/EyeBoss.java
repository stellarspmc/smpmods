package ml.spmc.smpmod.minecraft.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;

import static ml.spmc.smpmod.SMPMod.SERVER;

public class EyeBoss extends Events {
    private void spawnEye(Player p) {
        p.sendSystemMessage(Component.literal("<eye boss> i am coming for you :)"));
        eye(p);
    }

    private static ItemStack getHead() {
        final ItemStack eyeHead = Items.PLAYER_HEAD.getDefaultInstance();
        CompoundTag owner = eyeHead.getOrCreateTagElement("SkullOwner");
        owner.putString("Id", "f3e8d413-e643-4ce2-a8a7-b29a1e23fb14");
        owner.putString("Name", "eyelol");
        return eyeHead;
    }


    private static Zombie eye(Player p) {
        Zombie zombie = new Zombie(p.getLevel());
        p.getLevel().addFreshEntity(zombie);
        zombie.setCustomName(Component.literal("Eye Boss"));
        zombie.setGlowingTag(true);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, Items.NETHERITE_SWORD.getDefaultInstance());
        zombie.setHealth(100f);
        zombie.setItemSlot(EquipmentSlot.HEAD, getHead());
        zombie.setItemSlot(EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE.getDefaultInstance());
        return zombie;
    }

    @Override
    public void onEvent() {
        List<ServerPlayer> plr = SERVER.getPlayerList().getPlayers();
        Collections.shuffle(plr);
        spawnEye(plr.get(0));
    }

    @Override
    public void onEventEnd() {
        // nothing
    }

    @Override
    public int durationInSeconds() {
        // inf
        return 0;
    }
}
