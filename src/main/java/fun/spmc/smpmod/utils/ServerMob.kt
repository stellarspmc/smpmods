package `fun`.spmc.smpmod.utils

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity

interface ServerMob {
    val entityType: EntityType<out LivingEntity>
    fun entitySpawnRate(): Double
    val entityName: Component?

    fun setHead(entity: LivingEntity?)
    fun setChest(entity: LivingEntity?)
    fun setLegs(entity: LivingEntity?)
    fun setBoots(entity: LivingEntity?)

    fun setItems(entity: LivingEntity?)

    fun setEffects(entity: LivingEntity?)
    fun setAttributes(entity: LivingEntity?)

    fun setEntity(entity: LivingEntity) {
        entity.customName = this.entityName

        setHead(entity)
        setChest(entity)
        setLegs(entity)
        setBoots(entity)

        setItems(entity)

        setEffects(entity)
        setAttributes(entity)
    }
}
