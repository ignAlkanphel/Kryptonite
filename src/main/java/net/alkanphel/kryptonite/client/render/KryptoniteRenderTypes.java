package net.alkanphel.kryptonite.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.client.renderer.RenderPipelines;

public abstract class KryptoniteRenderTypes {

    public static class Pipelines {
        public static final RenderPipeline ADDITIVE_PARTICLE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                .withLocation(Kryptonite.id("additive_particle"))
                .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                .build();
    }

}