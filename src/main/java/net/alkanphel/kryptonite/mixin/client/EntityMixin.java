package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.GlowingAbility;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.scores.Team;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    // Prevent Slowdown (soul_sand & honey_block) ability
    @ModifyExpressionValue(method = "getBlockSpeedFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private Block kryptonite$preventSlowdownSoulSandHoneyBlock(Block original) {
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if ((Object) this != player) return original;

        if (original == Blocks.SOUL_SAND && AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get())
                .stream().anyMatch(i -> i.getAbility().modeBlocksPrevents(PreventSlowdownAbility.ModeBlocks.SOUL_SAND))) {
            return Blocks.STONE;
        }

        if (original == Blocks.HONEY_BLOCK && AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get())
                .stream().anyMatch(i -> i.getAbility().modeBlocksPrevents(PreventSlowdownAbility.ModeBlocks.HONEY_BLOCK))) {
            return Blocks.STONE;
        }

        return original;
    }

    // Glowing ability
    @Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
    private void kryptonite$modifyGlowColor(CallbackInfoReturnable<Integer> cir) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        LivingEntity viewer = mc.player;
        Entity renderedEntity = (Entity) (Object) this;

        Team team = renderedEntity.getTeam();
        boolean hasTeamColor = team != null && team.getColor().getColor() != null;

        float red = 0, green = 0, blue = 0;
        int count = 0;

        // OTHER mode
        if (renderedEntity != viewer) {
            for (AbilityInstance<GlowingAbility> instance : AbilityUtil.getEnabledInstances(viewer, KryptoniteAbilitySerializers.GLOWING.get())) {
                var ability = instance.getAbility();
                if (ability.mode != GlowingAbility.Mode.OTHER || (hasTeamColor && ability.useTeams) || !ability.doesApply(viewer, renderedEntity)) continue;

                var ctx = DataContext.forAbility(viewer, instance);

                red += ability.color.red(ctx);
                green += ability.color.green(ctx);
                blue += ability.color.blue(ctx);
                count++;
            }
        }

        // SELF mode
        if (renderedEntity instanceof LivingEntity livingRendered) {
            for (AbilityInstance<GlowingAbility> instance : AbilityUtil.getEnabledInstances(livingRendered, KryptoniteAbilitySerializers.GLOWING.get())) {
                var ability = instance.getAbility();
                if (ability.mode != GlowingAbility.Mode.SELF || (hasTeamColor && ability.useTeams) || !ability.doesApply(livingRendered, viewer)) continue;

                var ctx = DataContext.forAbility(livingRendered, instance);

                red += ability.color.red(ctx);
                green += ability.color.green(ctx);
                blue += ability.color.blue(ctx);
                count++;
            }
        }

        if (count > 0) {
            cir.setReturnValue(ARGB.colorFromFloat(1.0F, red / count, green / count, blue / count));
        }
    }

}