package net.alkanphel.kryptonite.power.logic.action.bi.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class BiActionSerializer<T extends BiAction> implements Documented<BiAction, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<BiAction, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), BiAction.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<BiAction, T> builder, HolderLookup.Provider provider);
}