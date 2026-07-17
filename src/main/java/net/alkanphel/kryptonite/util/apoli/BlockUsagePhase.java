package net.alkanphel.kryptonite.util.apoli;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum BlockUsagePhase implements StringRepresentable {
    BLOCK, ITEM;

    public static final Codec<BlockUsagePhase> CODEC = StringRepresentable.fromEnum(BlockUsagePhase::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

}