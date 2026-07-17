package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventHealingAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @Unique private Player player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void playerHook(ServerPlayer player, CallbackInfo ci) {
        this.player = player;
    }

    /**
     * {@link PreventHealingAbility}
     * Prevents food-related healing like the 'naturalRegeneration' game rule
     */
    @ModifyVariable(method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(value = "STORE"), name = "naturalRegen")
    private boolean kryptonite$preventHealing(boolean naturalRegen) {
        for (AbilityInstance<PreventHealingAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_HEALING.get())) {
            if (!instance.getAbility().fullPrevention.getAsBoolean(DataContext.forAbility(player, instance))) {
                return false;
            }
        }
        return naturalRegen;
    }

}