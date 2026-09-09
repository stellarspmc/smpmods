package spmc.smpmod.mobs

import spmc.smpmod.utils.ServerMob
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.cow.MushroomCow
import net.minecraft.world.entity.animal.happyghast.HappyGhast
import net.minecraft.world.entity.animal.sheep.Sheep
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Shulker
import net.minecraft.world.item.DyeColor
import java.util.*

object ServerMobEvents {
    private val mobList: ArrayList<ServerMob> = ArrayList<ServerMob>()

    @JvmStatic
    fun onEntityJoin(entity: Entity, level: ServerLevel) {
        if (entity.type === EntityTypes.COW && level.getRandom().nextFloat() > .99f) replaceMob(entity, EntityTypes.MOOSHROOM, level)
        if (entity.type === EntityTypes.BLAZE && level.getRandom().nextFloat() > .8f) replaceMob(entity, EntityTypes.BREEZE, level)
        if (entity.type === EntityTypes.SPIDER && level.getRandom().nextFloat() > .5f) replaceMob(entity, EntityTypes.CAVE_SPIDER, level)
        if (entity.type === EntityTypes.ZOMBIE && level.getRandom().nextFloat() > .5f) replaceMob(entity, EntityTypes.HUSK, level)
        if (entity.type === EntityTypes.SKELETON && level.getRandom().nextFloat() > .67f) replaceMob(entity, EntityTypes.BOGGED, level)
        else if (entity.type === EntityTypes.SKELETON && level.getRandom().nextFloat() > .67f) replaceMob(entity, EntityTypes.STRAY, level)
        if (entity.type === EntityTypes.HORSE && level.getRandom().nextFloat() > .75f) replaceMob(entity, EntityTypes.SKELETON_HORSE, level)
        else if (entity.type === EntityTypes.HORSE && level.getRandom().nextFloat() > .75f) replaceMob(entity, EntityTypes.ZOMBIE_HORSE, level)
        if (entity.type === EntityTypes.CAMEL && level.getRandom().nextFloat() > .75f) replaceMob(entity, EntityTypes.CAMEL_HUSK, level)

        if (entity.type === EntityTypes.HAPPY_GHAST) (entity as HappyGhast).getAttribute(Attributes.FLYING_SPEED)?.baseValue = .15
        if (entity.type === EntityTypes.CREEPER && level.getRandom().nextFloat() > .7f) entity.getEntityData().set(Creeper.DATA_IS_POWERED, true)
        if (entity.type === EntityTypes.SHEEP) (entity as Sheep).color = DyeColor.byId(level.getRandom().nextInt(15))
        if (entity.type === EntityTypes.SHULKER) (entity as Shulker).variant = Optional.of<DyeColor>(DyeColor.byId(level.getRandom().nextInt(15)))
        if (entity.type === EntityTypes.MOOSHROOM && level.getRandom().nextFloat() > .5f) (entity as MushroomCow).variant = MushroomCow.Variant.BROWN

        for (mob in mobList) {
            if (mob.entityType === entity.type && entity.getRandom().nextFloat() >= mob.entitySpawnRate()) {
                mob.setEntity(entity as LivingEntity)
                break
            }
        }
    }

    @JvmStatic
    fun registerMobs() {
        mobList.add(EyeZombie())
        mobList.add(NickZombie())
        mobList.add(Minotaur())
    }

    private fun <T : Entity> replaceMob(original: Entity, newType: EntityType<T>, level: ServerLevel) {
        val stray = newType.create(level, EntitySpawnReason.TRIGGERED) ?: return
        stray.setPos(original.position())
        level.addFreshEntity(stray)
        original.discard()
    }
}
