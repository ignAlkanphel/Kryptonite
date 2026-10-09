package net.alkanphel.kryptonite.util.apoli;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum BlockUsagePhase implements StringRepresentable {
    BLOCK, ITEM;

    public static final Codec<BlockUsagePhase> CODEC = StringRepresentable.fromEnum(BlockUsagePhase::values);

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

}