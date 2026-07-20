package net.alkanphel.kryptonite.client.render;

import com.google.common.reflect.TypeToken;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.PreventEntityRenderAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.threetag.palladium.client.AbilityClientEventHandler;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;

@EventBusSubscriber(modid = Kryptonite.MOD_ID, value = Dist.CLIENT)
public class KryptoniteRenderStateKeys {

    public static ContextKey<Float> PREVENT_RENDER_OPACITY = create("prevent_render_opacity");

    private static <T> ContextKey<T> create(String name) {
        return new ContextKey<>(Kryptonite.id(name));
    }

    @SubscribeEvent
    static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {}, (entity, state) -> {
            LivingEntity viewer = Minecraft.getInstance().player;

            float opacity = 1F;

            if (viewer != null && viewer != entity) {
                opacity = OpacityRenderChanging.getOpacity(viewer, entity, state.partialTick);
            }

            state.setRenderData(PREVENT_RENDER_OPACITY, opacity);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void renderLivingPre(RenderLivingEvent.Pre<?, ?, ?> event) {
        if (event.isCanceled()) return;

        float opacity = event.getRenderState().getRenderDataOrDefault(PREVENT_RENDER_OPACITY, 1F);
        if (opacity >= 1F) return;

        if (opacity <= 0F) {
            event.setCanceled(true);
            AbilityClientEventHandler.resetColorOverrides();
            return;
        }

        AbilityClientEventHandler.OVERRIDDEN_OPACITY *= opacity;
    }

    @SubscribeEvent
    static void onClientTickPost(ClientTickEvent.Post event) {
        LivingEntity viewer = Minecraft.getInstance().player;
        if (viewer == null) return;

        for (AbilityInstance<?> instance : AbilityUtil.getInstances(viewer)) {
            if (instance.getAbility() instanceof PreventEntityRenderAbility preventEntityRender) {
                preventEntityRender.onClientTick(viewer);
            }
        }
    }

}