package net.alkanphel.kryptonite.power.logic.condition.block.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class BlockConditionSerializer<T extends BlockCondition> implements Documented<BlockCondition, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<BlockCondition, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), BlockCondition.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<BlockCondition, T> builder, HolderLookup.Provider provider);
}