package net.alkanphel.kryptonite.power.logic.action.block.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class BlockActionSerializer<T extends BlockAction> implements Documented<BlockAction, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<BlockAction, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), BlockAction.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<BlockAction, T> builder, HolderLookup.Provider provider);
}