package net.alkanphel.kryptonite.mixin.common;

import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SlotRanges.class)
public interface SlotRangesAccessor {
    @Accessor("SLOTS")
    static List<SlotRange> kryptonite$getSlotRanges() {
        throw new AssertionError();
    }
}