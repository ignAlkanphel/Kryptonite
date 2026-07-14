package net.alkanphel.kryptonite.power.logic.condition.dimension.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class DimensionConditionSerializer<T extends DimensionCondition> implements Documented<DimensionCondition, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<DimensionCondition, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), DimensionCondition.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<DimensionCondition, T> builder, HolderLookup.Provider provider);
}