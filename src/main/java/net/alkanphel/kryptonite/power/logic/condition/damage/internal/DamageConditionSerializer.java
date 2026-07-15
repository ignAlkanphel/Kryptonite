package net.alkanphel.kryptonite.power.logic.condition.damage.internal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class DamageConditionSerializer<T extends DamageCondition> implements Documented<DamageCondition, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<DamageCondition, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), DamageCondition.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<DamageCondition, T> builder, HolderLookup.Provider provider);
}