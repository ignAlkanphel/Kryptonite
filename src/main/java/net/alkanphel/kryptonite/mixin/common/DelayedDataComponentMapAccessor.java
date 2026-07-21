package net.alkanphel.kryptonite.mixin.common;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.component.DataComponentType;
import net.threetag.palladium.component.DelayedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DelayedDataComponentMap.class)
public interface DelayedDataComponentMapAccessor {
    @Accessor("values")
    Map<DataComponentType<?>, Dynamic<?>> kryptonite$getValues();
}