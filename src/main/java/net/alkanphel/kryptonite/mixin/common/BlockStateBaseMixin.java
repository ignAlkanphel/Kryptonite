package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.IntangibilityAbility;
import net.alkanphel.kryptonite.power.ability.PreventBlockSelectionAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    // Intangibility ability
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"), cancellable = true)
    private void kryptonite$getCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        VoxelShape blockShape = cir.getReturnValue();

        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof LivingEntity living && !blockShape.isEmpty()) {
            for (AbilityInstance<IntangibilityAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.INTANGIBILITY.get())) {
                if (instance.getAbility().shouldPhase(living, instance, blockShape, pos)) {
                    cir.setReturnValue(Shapes.empty());
                    return;
                }
            }
        }
    }

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventCollisionWhenPhasing(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            for (AbilityInstance<IntangibilityAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.INTANGIBILITY.get())) {
                if (instance.getAbility().doesApply(living, pos)) {
                    ci.cancel();
                    return;
                }
            }
        }
    }

    // Prevent Block Selection ability
    @Inject(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"), cancellable = true)
    private void kryptonite$preventBlockSelection(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (context == CollisionContext.empty()) return;

        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof LivingEntity living && PreventBlockSelectionAbility.doesPrevent(living, pos)) {
            cir.setReturnValue(Shapes.empty());
        }
    }

}