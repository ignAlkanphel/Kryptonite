package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventHealingAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @ModifyVariable(method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(value = "STORE"), name = "naturalRegen")
    private boolean kryptonite$preventHealing(boolean naturalRegen, ServerPlayer player) {
        for (AbilityInstance<PreventHealingAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_HEALING.get())) {
            if (!instance.getAbility().fullPrevention.getAsBoolean(DataContext.forAbility(player, instance))) {
                return false;
            }
        }

        return naturalRegen;
    }

}