package net.alkanphel.kryptonite.power.logic.action.item.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class ItemActionSerializer<T extends ItemAction> implements Documented<ItemAction, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<ItemAction, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), ItemAction.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<ItemAction, T> builder, HolderLookup.Provider provider);
}