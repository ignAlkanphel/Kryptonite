package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventEntitySelectionAbility;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    // Prevent Item Slowdown (item) ability
    @ModifyExpressionValue(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean kryptonite$preventSlowdownItem(boolean isUsingItem) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        if (AbilityUtil.getEnabledInstances(self, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get())
                .stream().anyMatch(i -> i.getAbility().modePrevents(PreventSlowdownAbility.Mode.ITEM))) {
            return false;
        }

        return isUsingItem;
    }

    // Prevent Item Slowdown (crouching) ability
    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventSlowdownCrouching(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        if (AbilityUtil.getEnabledInstances(self, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get())
                .stream().anyMatch(i -> i.getAbility().modePrevents(PreventSlowdownAbility.Mode.CROUCHING))) {
            cir.setReturnValue(isVisuallyCrawling());
        }

    }

    // Prevent Entity Selection ability
    @ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 4)
    private static Predicate<Entity> kryptonite$preventEntitySelection(Predicate<Entity> original) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof LivingEntity viewer)) return original;

        return entity -> original.test(entity) && !PreventEntitySelectionAbility.shouldPreventSelection(viewer, entity);
    }

    // Prevent Sprinting ability
    @ModifyReturnValue(method = "canStartSprinting", at = @At("RETURN"))
    private boolean kryptonite$preventSprinting(boolean original) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (original && AbilityUtil.isTypeEnabled(player, KryptoniteAbilitySerializers.PREVENT_SPRINTING.get())) {
            return false;
        }

        return original;
    }

    // Prevent Slowdown (water) ability
    @Inject(at = @At("HEAD"), method = "isUnderWater", cancellable = true)
    private void kryptonite$preventSlowdownAllowSwimming(CallbackInfoReturnable<Boolean> cir)  {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get()).stream().anyMatch(i -> i.getAbility().modePrevents(PreventSlowdownAbility.Mode.WATER))) {
            cir.setReturnValue(false);
        }
    }

}