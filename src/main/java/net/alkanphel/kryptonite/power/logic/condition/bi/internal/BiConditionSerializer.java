package net.alkanphel.kryptonite.power.logic.condition.bi.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class BiConditionSerializer<T extends BiCondition> implements Documented<BiCondition, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<BiCondition, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), BiCondition.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<BiCondition, T> builder, HolderLookup.Provider provider);
}