package net.alkanphel.kryptonite.power.logic.condition.item.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class ItemConditionSerializer<T extends ItemCondition> implements Documented<ItemCondition, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<ItemCondition, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), ItemCondition.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<ItemCondition, T> builder, HolderLookup.Provider provider);
}