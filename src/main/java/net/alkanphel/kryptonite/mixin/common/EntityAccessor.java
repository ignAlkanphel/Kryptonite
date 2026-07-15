package net.alkanphel.kryptonite.mixin.common;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("setSharedFlag")
    void kryptonite$setSharedFlag(int index, boolean value);
}