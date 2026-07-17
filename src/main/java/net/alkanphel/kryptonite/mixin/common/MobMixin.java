package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.alkanphel.kryptonite.power.ability.ActionOnItemPickupAbility;
import net.alkanphel.kryptonite.power.ability.PreventItemPickupAbility;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    // Prevent Item Pickup ability
    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;pickUpItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/item/ItemEntity;)V"))
    private boolean kryptonite$preventItemPickup(Mob mobEntity, ServerLevel level, ItemEntity itemEntity) {
        return !PreventItemPickupAbility.doesPrevent(itemEntity, this);
    }

    // Action On Item Pickup ability
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;pickUpItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/item/ItemEntity;)V"))
    private void kryptonite$actionOnItemPickup(Mob mobEntity, ServerLevel level, ItemEntity itemEntity, Operation<Void> original) {
        SlotAccess stackReference = MiscUtil.createStackReference(itemEntity.getItem());

        EntityReference<Entity> throwerReference = ((ItemEntityAccessor) itemEntity).kryptonite$getThrowerUuid();

        Entity thrower = null;
        if (throwerReference != null) thrower = MiscUtil.getEntityByUuid(throwerReference.getUUID(), this.level().getServer());

        Prioritized.CallInstance<ActionOnItemPickupAbility> callInstance = ActionOnItemPickupAbility.runItemActions(thrower, stackReference, this);
        itemEntity.setItem(stackReference.get());

        original.call(mobEntity, level, itemEntity);
        ActionOnItemPickupAbility.runBiActions(callInstance, thrower, this);
    }

}