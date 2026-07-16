package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventItemUseAbility;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {

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