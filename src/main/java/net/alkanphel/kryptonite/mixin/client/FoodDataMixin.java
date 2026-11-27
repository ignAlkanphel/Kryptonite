package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    // Prevent Slowdown (hunger) ability
    @ModifyExpressionValue(method = "hasEnoughFood()Z", at = @At(value = "CONSTANT", args = "floatValue=6.0f"))
    private float kryptonite$preventSlowdownHunger(float constant) {
        var player = Minecraft.getInstance().player;
        if (player == null) return constant;

        return PreventSlowdownAbility.preventsMode(player, PreventSlowdownAbility.Mode.HUNGER) ? -1 : constant;
    }

}