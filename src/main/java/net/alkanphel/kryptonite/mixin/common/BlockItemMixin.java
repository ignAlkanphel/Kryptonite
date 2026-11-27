package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ActionOnBlockPlaceAbility;
import net.alkanphel.kryptonite.power.ability.PreventBlockPlaceAbility;
import net.alkanphel.kryptonite.power.ability.PreventItemUseAbility;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.alkanphel.kryptonite.util.apoli.ability.PriorityPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    // Prevent Block Place ability
    @ModifyReturnValue(method = "canPlace", at = @At("RETURN"))
    private boolean kryptonite$preventBlockPlace(boolean original, BlockPlaceContext placeContext, BlockState stateForPlacement) {
        Player player = placeContext.getPlayer();
        if (player == null) return original;

        Direction direction = placeContext.getClickedFace();
        ItemStack stack = placeContext.getItemInHand();
        InteractionHand hand = placeContext.getHand();

        BlockPos toPos = placeContext.getClickedPos();
        BlockPos onPos = ((UseOnContextAccessor) placeContext).kryptonite$callGetHitResult().getBlockPos();

        Prioritized.CallInstance<PreventBlockPlaceAbility> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(player, PreventBlockPlaceAbility.class, ability -> ability.doesPrevent(player, stack, hand, toPos, onPos, direction));

        if (callInstance.isEmpty()) return original;

        callInstance.forEachByPriority(ability -> ability.runActions(player, hand, toPos, onPos, direction));
        return false;
    }

    // Action On Block Place ability
    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void kryptonite$actionOnBlockPlace(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "player") Player player, @Local(name = "pos") BlockPos pos, @Local(name = "itemStack") ItemStack itemStack, @Share("callInstance") LocalRef<Prioritized.CallInstance<ActionOnBlockPlaceAbility>> callInstanceRef) {
        if (player == null) return;

        Direction direction = placeContext.getClickedFace();
        BlockPos onPos = ((UseOnContextAccessor) placeContext).kryptonite$callGetHitResult().getBlockPos();
        InteractionHand hand = placeContext.getHand();

        DataContext dataContext = DataContext.forEntity(player);
        Prioritized.CallInstance<ActionOnBlockPlaceAbility> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(player, ActionOnBlockPlaceAbility.class, ability -> ability.shouldRun(dataContext, itemStack, hand, pos, onPos, direction) && PriorityPhase.BEFORE.test(ability.getPriority()));

        callInstance.forEachByPriority(ability -> ability.runOtherActions(dataContext, pos, onPos, direction));

        callInstanceRef.set(callInstance);
    }

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("TAIL"))
    private void kryptonite$actionOnBlockPlacePost(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir, @Share("callInstance") LocalRef<Prioritized.CallInstance<ActionOnBlockPlaceAbility>> callInstanceRef) {
        Prioritized.CallInstance<ActionOnBlockPlaceAbility> callInstance = callInstanceRef.get();

        if (callInstance != null && placeContext.getPlayer() != null) {
            var dataContext = DataContext.forEntity(placeContext.getPlayer());
            callInstance.forEachByPriority(ability -> ability.runItemActions(dataContext, placeContext.getHand()));
        }
    }

}