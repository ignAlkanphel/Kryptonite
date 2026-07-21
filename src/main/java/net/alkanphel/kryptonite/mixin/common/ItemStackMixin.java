package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventItemUseAbility;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.ref.WeakReference;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder, EntityLinkedItemStack {

    @Unique @Nullable
    private WeakReference<Entity> kryptonite$holdingEntity;

    @Override
    public Entity kryptonite$getEntity() {
        return kryptonite$getEntity(true);
    }

    @Override // TODO Remove boolean as it is not needed for us
    public Entity kryptonite$getEntity(boolean prioritiseVanillaHolder) {
        if (kryptonite$holdingEntity != null) {
            return kryptonite$holdingEntity.get();
        }
        return null;
    }

    @Override
    public void kryptonite$setEntity(Entity entity) {
        this.kryptonite$holdingEntity = entity != null ? new WeakReference<>(entity) : null;
    }

    @ModifyReturnValue(method = "copy", at = @At("RETURN"))
    private ItemStack kryptonite$copy(ItemStack original) {
        Entity holder = this.kryptonite$getEntity();

        if (holder != null) {
            ((EntityLinkedItemStack) (Object) original).kryptonite$setEntity(holder);
        }

        return original;
    }

    // Prevent Item Use ability
    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult kryptonite$preventItemUseItemStack(Item item, Level level, Player player, InteractionHand hand, Operation<InteractionResult> original) {
        ItemStack stack = (ItemStack) (Object) this;

        for (AbilityInstance<PreventItemUseAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_ITEM_USE.get())) {
            if (instance.getAbility().doesPrevent(player, stack)) {
                return InteractionResult.FAIL;
            }
        }

        return original.call(item, level, player, hand);
    }

}