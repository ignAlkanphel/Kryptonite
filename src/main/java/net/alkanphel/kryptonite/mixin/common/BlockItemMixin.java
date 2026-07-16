package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventItemUseAbility;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    // Prevent Item Use ability
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult kryptonite$preventItemUseBlockItem(BlockItem instance, Level level, Player player, InteractionHand hand, Operation<InteractionResult> original, @Local(argsOnly = true, name = "context") UseOnContext context) {
        ItemStack stack = context.getItemInHand();

        for (AbilityInstance<PreventItemUseAbility> ability : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_ITEM_USE.get())) {
            if (ability.getAbility().doesPrevent(player, stack)) {
                return InteractionResult.FAIL;
            }
        }

        return original.call(instance, level, player, hand);
    }

}