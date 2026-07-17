package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.alkanphel.kryptonite.power.ability.ActionOnItemPickupAbility;
import net.alkanphel.kryptonite.power.ability.PreventItemPickupAbility;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow public abstract ItemStack getItem();
    @Shadow public abstract void setItem(ItemStack itemStack);

    @Shadow @Nullable private EntityReference<Entity> thrower;

    // Action On Item Pickup ability & partial Prevent Item Pickup ability
    @WrapOperation(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean kryptonite$onItemPickup(Inventory playerInventory, ItemStack itemStack, Operation<Boolean> original, Player player) {
        if (PreventItemPickupAbility.doesPrevent(kryptonite$thisAsItemEntity(), player)) {
            return false;
        }

        else if (MiscUtil.hasSpaceInInventory(playerInventory, itemStack)) {
            SlotAccess stackReference = MiscUtil.createStackReference(itemStack);
            Entity thrower = EntityReference.getEntity(this.thrower, this.level());

            Prioritized.CallInstance<ActionOnItemPickupAbility> callInstance = ActionOnItemPickupAbility.runItemActions(thrower, stackReference, player);
            this.setItem(stackReference.get());

            boolean result = original.call(playerInventory, this.getItem());
            if (result) {
                ActionOnItemPickupAbility.runBiActions(callInstance, thrower, player);
            }

            return result;
        }

        else {
            return original.call(playerInventory, itemStack);
        }

    }

    @Unique
    private ItemEntity kryptonite$thisAsItemEntity() {
        return (ItemEntity) (Object) this;
    }

}