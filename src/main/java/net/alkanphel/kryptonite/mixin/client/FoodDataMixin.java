package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodData;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    // Prevent Slowdown (hunger) ability
    @ModifyExpressionValue(method = "hasEnoughFood()Z", at = @At(value = "CONSTANT", args = "floatValue=6.0f"))
    private float kryptonite$preventSlowdownHunger(float constant) {
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null) return constant;

        if (AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get())
                .stream().anyMatch(i -> i.getAbility().modePrevents(PreventSlowdownAbility.Mode.HUNGER))) {
            return -1;
        }

        return constant;
    }

}